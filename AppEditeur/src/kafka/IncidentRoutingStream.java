package kafka;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Application Kafka Streams qui filtre les incidents critiques pour les router
 * vers un topic prioritaire destiné aux développeurs de l'éditeur.
 */


public class IncidentRoutingStream {

    private static final String BOOTSTRAP_SERVERS = "86.252.172.215:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://86.252.172.215:8081";

    // Topics
    private static final String INPUT_TOPIC = "plateforme.incidents";
    private static final String OUTPUT_TOPIC_URGENT = "editeur.incidents.urgents";
    // Optionnel : un topic pour le reste (archivage froid)
    private static final String OUTPUT_TOPIC_STANDARD = "editeur.incidents.standard";

    public static void main(String[] args) {
        // 1. Configuration de Kafka Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "incident-router-app-v1");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        // Optimisation : garantir le traitement "exactly_once" ou "at_least_once" (requis par le sujet [cite: 24])
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.AT_LEAST_ONCE);

        // Configuration pour la sérialisation/désérialisation Avro (GenericRecord)
        // C'est nécessaire car Streams doit lire le contenu pour filtrer
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);

        // Configuration spécifique pour le Serde Avro utilisé dans le code
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", SCHEMA_REGISTRY_URL);
        final GenericAvroSerde valueSerde = new GenericAvroSerde();
        valueSerde.configure(serdeConfig, false); // false = c'est pour la value, pas la key

        // 2. Construction de la Topologie (Le graphe de traitement)
        StreamsBuilder builder = new StreamsBuilder();

        // Lire le flux source
        KStream<String, GenericRecord> incidentsStream = builder.stream(INPUT_TOPIC);

        // --- BRANCHE 1 : FILTRAGE DES URGENCES ---
        // On filtre pour ne garder que les CRASHS ou erreurs CRITIQUES
        KStream<String, GenericRecord> criticalStream = incidentsStream.filter((key, value) -> {
            if (value == null) return false;

            // Récupération du champ 'type_incident' défini dans le shéma Avro
            Object typeObj = value.get("typeIncident"); // Attention au nommage camelCase vs snake_case dans ton Avro
            if (typeObj == null) return false;

            String type = typeObj.toString().toUpperCase();

            // Logique métier : Les messages urgents pour l'éditeur
            return type.contains("CRASH") ||
                    type.contains("FATAL") ||
                    type.contains("BLOCKING") ||
                    type.contains("CORRUPTION");
        });

        // Écrire les urgences dans le topic dédié
        criticalStream.to(OUTPUT_TOPIC_URGENT, Produced.with(Serdes.String(), valueSerde));

        // Log pour le débug console
        criticalStream.peek((key, value) -> {
            // Extraction des champs du record Avro
            String jeu = value.get("titreJeu") != null ? value.get("titreJeu").toString() : "Inconnu";
            String editeur = value.get("editeurId") != null ? value.get("editeurId").toString() : "Inconnu";
            String typePropbleme = value.get("typeIncident") != null ? value.get("typeIncident").toString() : "CRASH";

            // Formatage spécial pour le Dashboard
            String alerte = String.format(
                    "🚨 [URGENT - %s] Editeur: %s | Jeu: %s | Problème: %s",
                    typePropbleme.toUpperCase(),
                    editeur,
                    jeu,
                    typePropbleme
            );

            // Envoi au dashboard (UI Swing)
            ui.EditeurDashboard.log(alerte);
        });


        // --- BRANCHE 2 (Optionnelle) : LE RESTE ---
        // On peut envoyer le reste ailleurs, ou laisser le consommateur actuel gérer la base globale
        // KStream<String, GenericRecord> standardStream = incidentsStream.filterNot(... same predicate ...);
        // standardStream.to(OUTPUT_TOPIC_STANDARD, ...);

        // 3. Démarrage
        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        // Gestion propre de l'arrêt (Ctrl+C)
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        System.out.println("🚀 Router d'incidents démarré...");
        streams.start();
    }
}