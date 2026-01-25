package kafka;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

/**
 * Consumer qui lit les événements d'évaluation et de rapport d'incident et les enregistre
 * dans une base SQLite locale `AppEditeur/ressources/commentaires.db` en utilisant le
 * schéma défini dans `AppEditeur/ressources/db_commentaires.sql`.
 *
 * Simplifications / choix :
 * - Utilise GenericRecord pour lire les valeurs Avro (KafkaAvroDeserializer)
 * - Detecte le type d'événement en regardant le nom du schema (record.getSchema().getName())
 * - Stocke les champs pertinents dans la table 'commentaires'
 */
public class CommentairesConsumer {
    private static final String KAFKA_BOOTSTRAP_SERVERS = "86.252.172.215:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://86.252.172.215:8081";

    private static final String SQLITE_DB_PATH = "ressources/commentaires.db";

    private final KafkaConsumer<String, Object> consumer;

    public CommentairesConsumer(String groupId, String... topics) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        // Le consumer lira GenericRecord (ou specific si vous générez des classes Avro)
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, false);

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Arrays.asList(topics));

        System.out.println("✅ CommentairesConsumer initialisé, topics=" + Arrays.toString(topics));
    }

    public void runLoop() {
        // Créer la base SQLite si nécessaire
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + SQLITE_DB_PATH)) {
            // Ensure DB created by executing the SQL script manually outside or rely on user to create it
            System.out.println("✅ Connecté à la base SQLite: " + SQLITE_DB_PATH);

            while (true) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, Object> rec : records) {
                    try {
                        Object value = rec.value();
                        if (value == null) continue;

                        if (value instanceof GenericRecord) {
                            GenericRecord r = (GenericRecord) value;
                            String schemaName = r.getSchema().getName();

                            if ("EvaluationEvent".equals(schemaName)) {
                                persistEvaluation(conn, r, rec.topic());
                            } else if ("RapportIncidentEvent".equals(schemaName) || "PublicationPatchEvent".equals(schemaName)) {
                                // Selon le schéma, ici on gère RapportIncidentEvent ou PublicationPatchEvent
                                persistIncident(conn, r, rec.topic());
                            } else {
                                System.out.println("Message non géré (schema=" + schemaName + ")");
                            }
                        } else {
                            System.out.println("Valeur non-GenericRecord reçue: " + value.getClass().getName());
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur traitement record: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur DB/consumer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }

    private void persistEvaluation(Connection conn, GenericRecord r, String topic) {
        final String sql = "INSERT OR IGNORE INTO commentaires(event_id,event_type,ts,jeu_id,titre_jeu,editeur_id,pseudo_joueur,note,commentaire,temps_de_jeu_minutes,version_jeu_evaluee,date_publication,recommande,aspects_positifs,aspects_negatifs,raw_payload) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, (String) r.get("eventId"));
            ps.setString(2, "EVALUATION");
            ps.setLong(3, (Long) r.get("timestamp"));

            ps.setString(4, (String) r.get("jeuId"));
            ps.setString(5, (String) r.get("titreJeu"));
            ps.setString(6, (String) r.get("editeurId"));

            ps.setString(7, (String) r.get("pseudoJoueur"));
            Integer note = r.get("note") != null ? (Integer) r.get("note") : null;
            if (note != null) ps.setInt(8, note);
            else ps.setNull(8, java.sql.Types.INTEGER);

            ps.setString(9, r.get("commentaire") != null ? r.get("commentaire").toString() : null);
            ps.setLong(10, r.get("tempsDeJeuEnMinutes") != null ? (Long) r.get("tempsDeJeuEnMinutes") : 0L);
            ps.setString(11, r.get("versionJeuEvaluee") != null ? r.get("versionJeuEvaluee").toString() : null);
            ps.setLong(12, r.get("datePublication") != null ? (Long) r.get("datePublication") : System.currentTimeMillis());
            ps.setInt(13, r.get("recommande") != null && (Boolean) r.get("recommande") ? 1 : 0);

            // aspects arrays -> JSON-like string
            ps.setString(14, r.get("aspectsPositifs") != null ? r.get("aspectsPositifs").toString() : "[]");
            ps.setString(15, r.get("aspectsNegatifs") != null ? r.get("aspectsNegatifs").toString() : "[]");

            ps.setString(16, r.toString());

            ps.executeUpdate();
            System.out.println("✅ Évaluation persistée (eventId=" + r.get("eventId") + ")");
        } catch (Exception e) {
            System.err.println("Erreur insertion évaluation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void persistIncident(Connection conn, GenericRecord r, String topic) {
        final String sql = "INSERT OR IGNORE INTO commentaires(event_id,event_type,ts,jeu_id,titre_jeu,editeur_id,incident_id,version_jeu,plateforme,pseudo_joueur,type_incident,description_erreur,date_survenue,contexte,raw_payload) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, (String) r.get("eventId"));
            ps.setString(2, "INCIDENT");
            ps.setLong(3, (Long) r.get("timestamp"));

            ps.setString(4, r.get("jeuId") != null ? r.get("jeuId").toString() : null);
            ps.setString(5, r.get("titreJeu") != null ? r.get("titreJeu").toString() : null);
            ps.setString(6, r.get("editeurId") != null ? r.get("editeurId").toString() : null);

            ps.setString(7, r.get("incidentId") != null ? r.get("incidentId").toString() : null);
            ps.setString(8, r.get("versionJeu") != null ? r.get("versionJeu").toString() : null);
            ps.setString(9, r.get("plateforme") != null ? r.get("plateforme").toString() : null);
            ps.setString(10, r.get("pseudoJoueur") != null ? r.get("pseudoJoueur").toString() : null);

            ps.setString(11, r.get("typeIncident") != null ? r.get("typeIncident").toString() : null);
            ps.setString(12, r.get("descriptionErreur") != null ? r.get("descriptionErreur").toString() : null);
            ps.setLong(13, r.get("dateSurvenue") != null ? (Long) r.get("dateSurvenue") : 0L);
            ps.setString(14, r.get("contexte") != null ? r.get("contexte").toString() : null);

            ps.setString(15, r.toString());

            ps.executeUpdate();
            System.out.println("✅ Incident persisté (eventId=" + r.get("eventId") + ")");
        } catch (Exception e) {
            System.err.println("Erreur insertion incident: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Petit wrapper pour config supplémentaire du KafkaAvroDeserializer
    public static final class KafkaAvroDeserializerConfig {
        public static final String SPECIFIC_AVRO_READER_CONFIG = "specific.avro.reader";
    }

    // Mini runner
    public static void main(String[] args) {
        // Topics utilisés dans le projet plate-forme
        String[] topics = new String[] {"plateforme.evaluations", "plateforme.incidents"};
        CommentairesConsumer c = new CommentairesConsumer("commentaires-consumer-group", topics);
        c.runLoop();
    }
}
