package kafka;

import com.polysteam.plateforme.events.EvaluationEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Producer Kafka pour publier les événements d'évaluation de jeux
 * Utilise Avro pour la sérialisation des messages
 */
public class EvaluationEventProducer {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "86.252.172.215:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://86.252.172.215:8081";
    private static final String TOPIC_NAME = "plateforme.evaluations";

    private final KafkaProducer<String, EvaluationEvent> producer;

    public EvaluationEventProducer() {
        this.producer = new KafkaProducer<>(createProducerConfig());
        System.out.println("✅ Kafka Producer (Évaluations) initialisé");
    }

    /**
     * Configuration du producer Kafka avec Avro
     */
    private Properties createProducerConfig() {
        Properties props = new Properties();

        // Configuration Kafka de base
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "plateforme-evaluation-producer");

        // Sérialisation : String pour la clé, Avro pour la valeur
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

        // Schema Registry pour la gestion des schémas Avro
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);

        // Configuration pour la fiabilité
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Timeouts AUGMENTÉS pour réseau distant
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        // Configuration réseau
        props.put(ProducerConfig.LINGER_MS_CONFIG, 100);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        // Compression
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        return props;
    }

    /**
     * Publie un événement d'évaluation sur Kafka
     * @param jeuId Identifiant du jeu
     * @param titreJeu Titre du jeu
     * @param editeurId Identifiant de l'éditeur
     * @param pseudoJoueur Pseudo du joueur
     * @param note Note attribuée (ex: sur 10)
     * @param commentaire Commentaire textuel (peut être null)
     * @param tempsDeJeuMinutes Temps de jeu en minutes
     * @param versionJeu Version du jeu évaluée
     * @param recommande Si le joueur recommande le jeu
     * @param aspectsPositifs Liste des aspects positifs
     * @param aspectsNegatifs Liste des aspects négatifs
     */
    public void publierEvaluation(
        String jeuId,
        String titreJeu,
        String editeurId,
        String pseudoJoueur,
        int note,
        String commentaire,
        long tempsDeJeuMinutes,
        String versionJeu,
        boolean recommande,
        List<String> aspectsPositifs,
        List<String> aspectsNegatifs
    ) {
        try {
            System.out.println("  🔨 Création de l'événement Évaluation Avro...");

            // Créer l'événement Avro
            EvaluationEvent event = EvaluationEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTimestamp(System.currentTimeMillis())
                .setEvaluationId(UUID.randomUUID().toString())
                .setJeuId(jeuId)
                .setTitreJeu(titreJeu)
                .setEditeurId(editeurId)
                .setPseudoJoueur(pseudoJoueur)
                .setNote(note)
                .setCommentaire(commentaire)
                .setTempsDeJeuEnMinutes(tempsDeJeuMinutes)
                .setVersionJeuEvaluee(versionJeu)
                .setDatePublication(System.currentTimeMillis())
                .setRecommande(recommande)
                .setAspectsPositifs(aspectsPositifs)
                .setAspectsNegatifs(aspectsNegatifs)
                .build();

            System.out.println("  ✅ Événement Évaluation Avro créé avec succès");
            System.out.println("  📤 Envoi vers Kafka (topic: " + TOPIC_NAME + ")...");

            // Créer le record Kafka (clé = editeurId pour routage)
            ProducerRecord<String, EvaluationEvent> record =
                new ProducerRecord<>(TOPIC_NAME, editeurId, event);

            // Publier de manière SYNCHRONE avec callback
            var future = producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("  ❌ Erreur callback : " + exception.getMessage());
                } else {
                    System.out.println("  ✅ Évaluation reçue par Kafka ! (partition: " + metadata.partition() +
                        ", offset: " + metadata.offset() + ")");
                }
            });

            // Attendre la réponse (bloquant)
            System.out.println("  ⏳ Attente de la confirmation Kafka...");
            future.get();

            System.out.println("✅ Évaluation publiée avec succès : " + titreJeu + " (" + note + "/10 par " + pseudoJoueur + ")");

        } catch (org.apache.kafka.common.errors.TimeoutException e) {
            System.err.println("❌ KAFKA TIMEOUT : Expiration du message");
            System.err.println("    Jeu : " + titreJeu);
            System.err.println("    Détails : " + e.getMessage());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'évaluation : " + titreJeu);
            System.err.println("    Type d'erreur : " + e.getClass().getName());
            System.err.println("    Message : " + e.getMessage());

            if (e.getCause() != null) {
                System.err.println("    Cause racine : " + e.getCause().getClass().getName());
                System.err.println("    Détails cause : " + e.getCause().getMessage());
            }
        }
    }

    /**
     * Version simplifiée pour publier une évaluation rapide
     */
    public void publierEvaluationSimple(
        String jeuId,
        String titreJeu,
        String editeurId,
        String pseudoJoueur,
        int note,
        String commentaire,
        boolean recommande
    ) {
        publierEvaluation(
            jeuId,
            titreJeu,
            editeurId,
            pseudoJoueur,
            note,
            commentaire,
            0, // Temps de jeu inconnu
            "1.0.0", // Version par défaut
            recommande,
            Arrays.asList(), // Pas d'aspects positifs
            Arrays.asList()  // Pas d'aspects négatifs
        );
    }

    /**
     * Ferme proprement le producer
     */
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
            System.out.println("✅ Kafka Producer (Évaluations) fermé");
        }
    }
}

