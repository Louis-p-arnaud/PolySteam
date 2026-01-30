package kafka;

import com.polysteam.plateforme.events.RapportIncidentEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

/**
 * Producer Kafka pour publier les événements de rapports d'incidents
 * Utilise Avro pour la sérialisation des messages
 */
public class IncidentEventProducer {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "86.252.172.215:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://86.252.172.215:8081";
    private static final String TOPIC_NAME = "plateforme.incidents";

    private final KafkaProducer<String, RapportIncidentEvent> producer;

    public IncidentEventProducer() {
        this.producer = new KafkaProducer<>(createProducerConfig());
        System.out.println("✅ Kafka Producer initialisé");

        // Tester la connexion
        testConnection();
    }

    /**
     * Test de connexion à Kafka et Schema Registry
     */
    private void testConnection() {
        // Test 1: Connexion à Kafka
        try {
            System.out.println("🔍 Test de connexion à Kafka...");
            producer.partitionsFor(TOPIC_NAME);
            System.out.println("✅ Connexion à Kafka réussie !");
        } catch (Exception e) {
            System.err.println("⚠️  Attention : Impossible de se connecter à Kafka");
            System.err.println("    Erreur : " + e.getMessage());
            System.err.println("    Vérifiez que Kafka est accessible sur " + KAFKA_BOOTSTRAP_SERVERS);
        }

        // Test 2: Connexion au Schema Registry
        try {
            System.out.println("🔍 Test de connexion au Schema Registry...");
            URL url = new URL(SCHEMA_REGISTRY_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ Connexion au Schema Registry réussie !");
            } else {
                System.err.println("⚠️  Schema Registry répond avec le code: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            System.err.println("❌ ERREUR CRITIQUE : Schema Registry inaccessible !");
            System.err.println("    URL testée : " + SCHEMA_REGISTRY_URL);
            System.err.println("    Erreur : " + e.getMessage());
            System.err.println("    ⚠️  Les messages Kafka avec Avro ne pourront PAS être envoyés !");
            System.err.println("    Solutions possibles :");
            System.err.println("    1. Vérifiez que Schema Registry est démarré sur le serveur");
            System.err.println("    2. Vérifiez les règles de firewall (port 8081)");
            System.err.println("    3. Utilisez un tunnel SSH si nécessaire");
        }

        System.out.println();
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

        // Configuration pour la fiabilité avec timeouts AUGMENTÉS
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // OBLIGATOIRE avec idempotence
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); // Réessais infinis
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // Éviter les doublons
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5); // Garder l'ordre

        // Timeouts AUGMENTÉS pour réseau distant/lent
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60000); // 60 secondes
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000); // 60 secondes
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000); // 120 secondes

        // Configuration réseau pour connexions distantes
        props.put(ProducerConfig.LINGER_MS_CONFIG, 100); // Attendre 100ms avant d'envoyer (batching)
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Taille du batch
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB de buffer

        // Compression pour réduire la bande passante
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // Désactiver la résolution DNS automatique (forcer l'utilisation de l'IP donnée)
        props.put("metadata.max.age.ms", 300000); // 5 minutes
        props.put("connections.max.idle.ms", 540000); // 9 minutes

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
     * @param typeIncident Type d'incident (CRASH, FREEZE, etc.)
     * @param description Description de l'incident
     */
    public void publierIncident(
        String jeuId,
        String titreJeu,
        String editeurId,
        String versionJeu,
        String plateforme,
        String pseudoJoueur,
        String typeIncident,
        String description
    ) {
        try {
            System.out.println("  🔨 Création de l'événement Avro...");

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
                .setTypeIncident(com.polysteam.plateforme.events.TypeIncident.valueOf(typeIncident))
                .setDescriptionErreur(description)
                .setDateSurvenue(System.currentTimeMillis())
                .setContexte(null) // Optionnel
                .build();

            System.out.println("  ✅ Événement Avro créé avec succès");
            System.out.println("  📤 Envoi vers Kafka (topic: " + TOPIC_NAME + ")...");

            // Créer le record Kafka
            ProducerRecord<String, RapportIncidentEvent> record =
                new ProducerRecord<>(TOPIC_NAME, editeurId, event);

            // Publier de manière SYNCHRONE avec callback pour plus de détails
            var future = producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("  ❌ Erreur callback : " + exception.getMessage());
                    exception.printStackTrace();
                } else {
                    System.out.println("  ✅ Message reçu par Kafka ! (partition: " + metadata.partition() +
                        ", offset: " + metadata.offset() + ")");
                }
            });

            // Attendre la réponse (bloquant)
            System.out.println("  ⏳ Attente de la confirmation Kafka...");
            future.get(); // ← SYNCHRONE : attend la réponse

            System.out.println("✅ Incident publié avec succès : " + titreJeu);

        } catch (org.apache.kafka.common.errors.TimeoutException e) {
            System.err.println("❌ KAFKA TIMEOUT : Expiration du message");
            System.err.println("    Jeu : " + titreJeu);
            System.err.println("    Détails : " + e.getMessage());
            System.err.println("    Le broker Kafka ne répond pas assez vite");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'incident : " + titreJeu);
            System.err.println("    Type d'erreur : " + e.getClass().getName());
            System.err.println("    Message : " + e.getMessage());

            // Afficher la cause racine si elle existe
            if (e.getCause() != null) {
                System.err.println("    Cause racine : " + e.getCause().getClass().getName());
                System.err.println("    Détails cause : " + e.getCause().getMessage());
            }
        }
    }

    /**
     * Version ASYNCHRONE de la publication (fire-and-forget)
     * Plus rapide mais sans garantie immédiate
     */
    public void publierIncidentAsync(
        String jeuId,
        String titreJeu,
        String editeurId,
        String versionJeu,
        String plateforme,
        String pseudoJoueur,
        String typeIncident,
        String description
    ) {
        try {
            System.out.println("  🔨 Création de l'événement Avro (mode ASYNC)...");

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
                .setTypeIncident(com.polysteam.plateforme.events.TypeIncident.valueOf(typeIncident))
                .setDescriptionErreur(description)
                .setDateSurvenue(System.currentTimeMillis())
                .setContexte(null)
                .build();

            System.out.println("  📤 Envoi asynchrone vers Kafka...");

            // Créer le record Kafka
            ProducerRecord<String, RapportIncidentEvent> record =
                new ProducerRecord<>(TOPIC_NAME, editeurId, event);

            // Publier en mode ASYNCHRONE avec callback
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("  ❌ Erreur async : " + titreJeu);
                    System.err.println("     " + exception.getMessage());
                } else {
                    System.out.println("  ✅ Publié : " + titreJeu +
                        " (partition: " + metadata.partition() +
                        ", offset: " + metadata.offset() + ")");
                }
            });

            System.out.println("  ⏩ Message envoyé en mode asynchrone (vérifiez les callbacks)");

        } catch (Exception e) {
            System.err.println("❌ Erreur création événement : " + titreJeu);
            System.err.println("    " + e.getMessage());
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

