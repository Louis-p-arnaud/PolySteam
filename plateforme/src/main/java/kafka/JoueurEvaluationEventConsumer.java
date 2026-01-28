package kafka;

import config.DatabaseConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Properties;

/**
 * Consumer Kafka pour récupérer les événements d'évaluations
 * envoyés par l'application Joueur sur le topic "joueur.notifications.evaluations"
 */
public class JoueurEvaluationEventConsumer {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "86.252.172.215:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://86.252.172.215:8081";
    private static final String TOPIC_NAME = "joueur.notifications.evaluations";
    private static final String CONSUMER_GROUP_ID = "plateforme-evaluation-consumer-group";

    private final KafkaConsumer<String, GenericRecord> consumer;
    private final EvaluationEventProducer evaluationProducer;
    private volatile boolean running = false;

    public JoueurEvaluationEventConsumer() {
        this.consumer = new KafkaConsumer<>(createConsumerConfig());
        this.evaluationProducer = new EvaluationEventProducer();
        System.out.println("✅ Kafka Consumer initialisé pour le topic: " + TOPIC_NAME);
    }

    /**
     * Configuration du consumer Kafka avec Avro
     */
    private Properties createConsumerConfig() {
        Properties props = new Properties();

        // Configuration Kafka de base
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "plateforme-evaluation-consumer");

        // Désérialisation : String pour la clé, Avro pour la valeur
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());

        // Schema Registry
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        props.put("specific.avro.reader", "false"); // Utilise GenericRecord au lieu de classes générées

        // Comportement de lecture
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Lire depuis le début si nouveau consommateur
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        // Timeouts
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "40000");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");

        return props;
    }

    /**
     * Démarre l'écoute du topic en mode asynchrone (en arrière-plan)
     */
    public void demarrerEcoute() {
        consumer.subscribe(Collections.singletonList(TOPIC_NAME));
        running = true;

        System.out.println("🎧 Démarrage du listener Kafka en arrière-plan...");
        System.out.println("📩 Écoute du topic '" + TOPIC_NAME + "' activée\n");

        Thread consumerThread = new Thread(() -> {
            try {
                while (running) {
                    ConsumerRecords<String, GenericRecord> records = consumer.poll(Duration.ofMillis(1000));

                    for (ConsumerRecord<String, GenericRecord> record : records) {
                        traiterEvaluation(record);
                    }
                }
            } catch (Exception e) {
                System.err.println("\n❌ [KAFKA] Erreur lors de la consommation des messages: " + e.getMessage());
                e.printStackTrace();
            } finally {
                consumer.close();
                System.out.println("\n🔌 [KAFKA] Consumer fermé");
            }
        }, "kafka-evaluation-consumer-thread");

        // Thread daemon pour qu'il s'arrête automatiquement quand l'appli se termine
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    /**
     * Démarre l'écoute du topic en mode synchrone (pour tests)
     * @param maxMessages Nombre maximum de messages à lire (0 = infini)
     * @param timeoutSeconds Timeout en secondes
     */
    public void demarrerEcouteSynchrone(int maxMessages, int timeoutSeconds) {
        consumer.subscribe(Collections.singletonList(TOPIC_NAME));
        running = true;

        System.out.println("🎧 Début de l'écoute du topic '" + TOPIC_NAME + "' (mode synchrone)...");
        if (maxMessages > 0) {
            System.out.println("📊 Lecture de maximum " + maxMessages + " message(s)");
        }
        if (timeoutSeconds > 0) {
            System.out.println("⏱️  Timeout: " + timeoutSeconds + " secondes\n");
        }

        int messagesLus = 0;
        long startTime = System.currentTimeMillis();

        try {
            while (running) {
                // Vérifier le timeout
                if (timeoutSeconds > 0 && (System.currentTimeMillis() - startTime) > timeoutSeconds * 1000L) {
                    System.out.println("\n⏱️  Timeout atteint, arrêt de l'écoute");
                    break;
                }

                ConsumerRecords<String, GenericRecord> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, GenericRecord> record : records) {
                    traiterEvaluation(record);
                    messagesLus++;

                    // Vérifier si on a atteint le nombre max de messages
                    if (maxMessages > 0 && messagesLus >= maxMessages) {
                        System.out.println("\n✅ " + maxMessages + " message(s) lu(s), arrêt de l'écoute");
                        running = false;
                        break;
                    }
                }

                // Si aucun message reçu et qu'on a déjà lu des messages, on peut s'arrêter
                if (records.isEmpty() && messagesLus > 0 && maxMessages > 0) {
                    System.out.println("\n✅ Aucun nouveau message, arrêt de l'écoute");
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la consommation des messages: " + e.getMessage());
            e.printStackTrace();
        } finally {
            consumer.close();
            System.out.println("🔌 Consumer fermé");
        }
    }

    /**
     * Traite une évaluation reçue depuis Kafka
     */
    private void traiterEvaluation(ConsumerRecord<String, GenericRecord> record) {
        try {
            GenericRecord evaluationRecord = record.value();

            // Extraction des champs du message
            long timestamp = (Long) evaluationRecord.get("timestamp");
            String jeuId = evaluationRecord.get("jeuId").toString();
            String titreJeu = evaluationRecord.get("titreJeu").toString();
            String pseudoJoueur = evaluationRecord.get("pseudoJoueur").toString();
            int note = (Integer) evaluationRecord.get("note");
            String commentaire = evaluationRecord.get("commentaire").toString();
            int tempsDeJeuEnMinutes = (Integer) evaluationRecord.get("tempsDeJeuEnMinutes");

            // Formatage de la date
            String dateFormatee = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            // Affichage du message reçu
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║    ⭐ [KAFKA] NOUVELLE ÉVALUATION REÇUE D'UN JOUEUR ⭐    ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("  🎮 Jeu           : " + titreJeu);
            System.out.println("  🆔 Jeu ID        : " + jeuId);
            System.out.println("  👤 Joueur        : " + pseudoJoueur);
            System.out.println("  ⭐ Note          : " + note + "/10");
            System.out.println("  💬 Commentaire   : " + commentaire);
            System.out.println("  ⏱️  Temps de jeu : " + tempsDeJeuEnMinutes + " minutes");
            System.out.println("  📅 Date          : " + dateFormatee);
            System.out.println("╚════════════════════════════════════════════════════════════╝");

            // 🔄 REPUBLICATION : Envoyer l'évaluation sur le topic plateforme.evaluations
            System.out.println("🔄 [KAFKA] Republication de l'évaluation vers 'plateforme.evaluations'...");

            try {
                // Récupérer les informations du jeu depuis la BDD
                InfoJeu infoJeu = recupererInfoJeu(jeuId);

                String editeurId = infoJeu != null ? infoJeu.editeurId : "EDITEUR_INCONNU";
                String versionJeu = infoJeu != null ? infoJeu.versionActuelle : "1.0.0";

                if (infoJeu == null) {
                    System.err.println("  ⚠️  [KAFKA] Avertissement : Jeu non trouvé dans la BDD, utilisation de valeurs par défaut");
                }

                // Déterminer si le jeu est recommandé (note > 5)
                boolean recommande = note > 5;

                // Préparer les listes d'aspects (simplifiées ici, car non fournies par le message du joueur)
                java.util.List<String> aspectsPositifs = recommande
                    ? java.util.Collections.singletonList("Note positive du joueur")
                    : java.util.Collections.emptyList();
                java.util.List<String> aspectsNegatifs = !recommande
                    ? java.util.Collections.singletonList("Note négative du joueur")
                    : java.util.Collections.emptyList();

                // Appel du producer pour republier l'évaluation
                evaluationProducer.publierEvaluation(
                    jeuId,                  // jeuId
                    titreJeu,               // titreJeu
                    editeurId,              // editeurId
                    pseudoJoueur,           // pseudoJoueur
                    note,                   // note
                    commentaire,            // commentaire
                    (long) tempsDeJeuEnMinutes, // tempsDeJeuMinutes
                    versionJeu,             // versionJeu (récupérée depuis la BDD)
                    recommande,             // recommande (true si note > 5)
                    aspectsPositifs,        // aspectsPositifs
                    aspectsNegatifs         // aspectsNegatifs
                );

                System.out.println("✅ [KAFKA] Évaluation republiée avec succès sur 'plateforme.evaluations'");
                System.out.println("  📊 Recommandation : " + (recommande ? "✅ OUI" : "❌ NON") + " (note " + (recommande ? ">" : "≤") + " 5)\n");

            } catch (Exception e) {
                System.err.println("❌ [KAFKA] Erreur lors de la republication de l'évaluation: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("❌ [KAFKA] Erreur lors du traitement de l'évaluation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Récupère les informations d'un jeu depuis la base de données
     * @param jeuId ID du jeu
     * @return InfoJeu contenant editeurId, plateforme et version, ou null si non trouvé
     */
    private InfoJeu recupererInfoJeu(String jeuId) {
        String query = "SELECT editeur_id, plateforme, version_actuelle FROM jeu_catalogue WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, jeuId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                InfoJeu info = new InfoJeu();
                info.editeurId = rs.getString("editeur_id");
                info.plateformesPrincipale = rs.getString("plateforme");
                info.versionActuelle = rs.getString("version_actuelle");
                return info;
            }

        } catch (Exception e) {
            System.err.println("  ⚠️  Erreur lors de la récupération des infos du jeu : " + e.getMessage());
        }

        return null;
    }

    /**
     * Classe interne pour stocker les informations d'un jeu
     */
    private static class InfoJeu {
        String editeurId;
        String plateformesPrincipale;
        String versionActuelle;
    }

    /**
     * Arrête l'écoute du consumer
     */
    public void arreterEcoute() {
        System.out.println("⏹️  Arrêt du consumer d'évaluations...");
        running = false;
    }

    /**
     * Vérifie si le consumer est en cours d'exécution
     */
    public boolean estEnCoursExecution() {
        return running;
    }

    /**
     * Ferme le consumer (à appeler en fin d'application)
     */
    public void fermer() {
        arreterEcoute();
        consumer.close();
        evaluationProducer.close();
        System.out.println("✅ Consumer d'évaluations fermé proprement");
    }
}

