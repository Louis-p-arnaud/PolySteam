package kafka;

import com.polysteam.plateforme.events.RapportIncidentEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.UUID;

/**
 * Producer Kafka pour publier les événements de rapports d'incidents
 * Utilise Avro pour la sérialisation des messages
 */
public class IncidentEventProducer {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";
    private static final String TOPIC_NAME = "plateforme.incidents";

    private final KafkaProducer<String, RapportIncidentEvent> producer;

    public IncidentEventProducer() {
        this.producer = new KafkaProducer<>(createProducerConfig());
        System.out.println("✅ Kafka Producer initialisé pour le topic: " + TOPIC_NAME);
    }

    /**
     * Configuration du producer Kafka avec Avro
     */
    private Properties createProducerConfig() {
        Properties props = new Properties();

        // Configuration Kafka de base
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "plateforme-producer");

        // Sérialisation : String pour la clé, Avro pour la valeur
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

        // Schema Registry pour la gestion des schémas Avro
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);

        // Configuration pour la fiabilité (at-least-once delivery)
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Attendre la confirmation de tous les réplicas
        props.put(ProducerConfig.RETRIES_CONFIG, 3); // Réessayer 3 fois en cas d'échec
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // Garantir l'ordre

        return props;
    }

    /**
     * Publie un événement de rapport d'incident sur Kafka
     * @param jeuId Identifiant du jeu
     * @param titreJeu Titre du jeu
     * @param editeurId Identifiant de l'éditeur
     * @param versionJeu Version du jeu
     * @param plateforme Plateforme (PC, PS5, etc.)
     * @param pseudoJoueur Pseudo du joueur
     * @param description Description de l'incident
     */
    public void publierIncident(
        String jeuId,
        String titreJeu,
        String editeurId,
        String versionJeu,
        String plateforme,
        String pseudoJoueur,
        String description
    ) {
        try {
            // Créer l'événement Avro
            RapportIncidentEvent event = RapportIncidentEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTimestamp(System.currentTimeMillis())
                .setIncidentId(UUID.randomUUID().toString())
                .setJeuId(jeuId)
                .setTitreJeu(titreJeu)
                .setEditeurId(editeurId)
                .setVersionJeu(versionJeu)
                .setPlateforme(plateforme)
                .setPseudoJoueur(pseudoJoueur)
                .setDescriptionErreur(description)
                .build();

            // Clé du message = editeurId (pour que tous les incidents d'un éditeur aillent dans la même partition)
            String key = editeurId;

            // Créer le record Kafka
            ProducerRecord<String, RapportIncidentEvent> record =
                new ProducerRecord<>(TOPIC_NAME, key, event);

            // Publier de manière asynchrone avec callback
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("❌ Erreur lors de la publication de l'événement: " + exception.getMessage());
                    exception.printStackTrace();
                } else {
                    System.out.println("📤 Événement publié sur Kafka:");
                    System.out.println("   Topic: " + metadata.topic());
                    System.out.println("   Partition: " + metadata.partition());
                    System.out.println("   Offset: " + metadata.offset());
                    System.out.println("   Incident: " + titreJeu + " - " + description);
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de l'événement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ferme proprement le producer (à appeler à la fin de l'application)
     */
    public void close() {
        if (producer != null) {
            producer.flush(); // Assurer que tous les messages en attente sont envoyés
            producer.close();
            System.out.println("✅ Kafka Producer fermé");
        }
    }
}

