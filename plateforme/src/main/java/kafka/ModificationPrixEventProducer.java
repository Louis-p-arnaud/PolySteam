package kafka;

import com.polysteam.plateforme.events.ModificationPrixEvent;
import com.polysteam.plateforme.events.RaisonModification;
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
import java.util.concurrent.ExecutionException;

/**
 * Producer Kafka pour publier les événements de modification de prix
 * Utilise Avro pour la sérialisation des messages
 */
public class ModificationPrixEventProducer {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "86.252.172.215:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://86.252.172.215:8081";
    private static final String TOPIC_NAME = "plateforme.modifications.prix";

    private final KafkaProducer<String, ModificationPrixEvent> producer;

    public ModificationPrixEventProducer() {
        this.producer = new KafkaProducer<>(createProducerConfig());
        System.out.println("✅ [PRICING-KAFKA] Producer de modifications de prix initialisé");

        // Tester la connexion
        testConnection();
    }

    /**
     * Test de connexion à Kafka et Schema Registry
     */
    private void testConnection() {
        // Test 1: Connexion à Kafka
        try {
            producer.partitionsFor(TOPIC_NAME);
        } catch (Exception e) {
            System.err.println("⚠️  [PRICING-KAFKA] Impossible de se connecter à Kafka : " + e.getMessage());
        }

        // Test 2: Connexion au Schema Registry
        try {
            URL url = new URL(SCHEMA_REGISTRY_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ [PRICING-KAFKA] Connexion au Schema Registry réussie");
            }
            connection.disconnect();
        } catch (IOException e) {
            System.err.println("⚠️  [PRICING-KAFKA] Schema Registry inaccessible : " + e.getMessage());
        }
    }

    /**
     * Configuration du producer Kafka avec Avro
     */
    private Properties createProducerConfig() {
        Properties props = new Properties();

        // Configuration Kafka de base
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "plateforme-pricing-producer");

        // Sérialisation : String pour la clé, Avro pour la valeur
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

        // Schema Registry pour la gestion des schémas Avro
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);

        // Configuration pour la fiabilité avec timeouts AUGMENTÉS
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // OBLIGATOIRE avec idempotence
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); // Réessais infinis
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // Éviter les doublons

        // Timeouts augmentés pour connexions longue distance
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "30000"); // 30 secondes
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "30000");
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "120000"); // 2 minutes

        // Compression pour réduire la bande passante
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // Batching pour optimiser les envois
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "10");

        return props;
    }

    /**
     * Publie un événement de modification de prix sur Kafka
     *
     * @param jeuId ID du jeu
     * @param titreJeu Titre du jeu
     * @param editeurId ID de l'éditeur
     * @param prixEditeur Prix de base de l'éditeur
     * @param ancienPrix Prix avant modification
     * @param nouveauPrix Prix après modification
     * @param noteMoyenne Note moyenne actuelle
     * @param nombreEvaluations Nombre total d'évaluations
     * @param plateforme Plateforme concernée
     */
    public void publierModificationPrix(
        String jeuId,
        String titreJeu,
        String editeurId,
        double prixEditeur,
        double ancienPrix,
        double nouveauPrix,
        double noteMoyenne,
        int nombreEvaluations,
        String plateforme
    ) {
        try {
            System.out.println("  💰 [PRICING-KAFKA] Création de l'événement ModificationPrix Avro...");

            // Calculer la variation
            double variation = ((nouveauPrix - ancienPrix) / ancienPrix) * 100.0;
            variation = Math.round(variation * 100.0) / 100.0; // Arrondi à 2 décimales

            // Déterminer la raison de la modification
            RaisonModification raison;
            String description;

            if (noteMoyenne < 0) {
                raison = RaisonModification.RETOUR_PRIX_BASE;
                description = "Aucune évaluation disponible - Retour au prix éditeur";
            } else if (noteMoyenne >= 8.0) {
                raison = RaisonModification.EVALUATION_EXCELLENTE;
                description = String.format("Prix ajusté à la hausse (+15%%) suite à une excellente note moyenne (%.1f/10)", noteMoyenne);
            } else if (noteMoyenne >= 6.5) {
                raison = RaisonModification.EVALUATION_BONNE;
                description = String.format("Prix légèrement augmenté (+5%%) grâce à une bonne note moyenne (%.1f/10)", noteMoyenne);
            } else if (noteMoyenne >= 5.0) {
                raison = RaisonModification.EVALUATION_MOYENNE;
                description = String.format("Prix réduit (-10%%) en raison d'une note moyenne acceptable (%.1f/10)", noteMoyenne);
            } else {
                raison = RaisonModification.EVALUATION_MAUVAISE;
                description = String.format("Prix fortement réduit (-25%%) suite à une mauvaise note moyenne (%.1f/10)", noteMoyenne);
            }

            // Déterminer si c'est une promotion ou une augmentation
            boolean estPromotion = nouveauPrix < prixEditeur;
            boolean estAugmentation = nouveauPrix > prixEditeur;

            // Créer l'événement Avro
            ModificationPrixEvent event = ModificationPrixEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setTimestamp(System.currentTimeMillis())
                .setJeuId(jeuId)
                .setTitreJeu(titreJeu)
                .setEditeurId(editeurId)
                .setPrixEditeur(prixEditeur)
                .setAncienPrix(ancienPrix)
                .setNouveauPrix(nouveauPrix)
                .setPourcentageVariation(variation)
                .setNoteMoyenne(noteMoyenne >= 0 ? noteMoyenne : 0.0)
                .setNombreEvaluations(nombreEvaluations)
                .setRaisonModification(raison)
                .setDescription(description)
                .setPlateforme(plateforme)
                .setEstPromotion(estPromotion)
                .setEstAugmentation(estAugmentation)
                .build();

            System.out.println("  📤 [PRICING-KAFKA] Envoi sur le topic '" + TOPIC_NAME + "'...");

            // Créer l'enregistrement avec la clé = jeuId
            ProducerRecord<String, ModificationPrixEvent> record =
                new ProducerRecord<>(TOPIC_NAME, jeuId, event);

            // Envoyer de manière synchrone pour garantir la livraison
            producer.send(record).get();

            System.out.println("  ✅ [PRICING-KAFKA] Modification de prix publiée avec succès !");
            System.out.println("     📊 " + titreJeu);
            System.out.println("     💵 " + String.format("%.2f€ → %.2f€", ancienPrix, nouveauPrix) +
                             " (" + (variation > 0 ? "+" : "") + String.format("%.1f%%", variation) + ")");
            System.out.println("     ⭐ Note moyenne : " + String.format("%.1f/10", noteMoyenne) +
                             " (" + nombreEvaluations + " évaluation" + (nombreEvaluations > 1 ? "s" : "") + ")");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("  ❌ [PRICING-KAFKA] Interruption lors de l'envoi : " + e.getMessage());
        } catch (ExecutionException e) {
            System.err.println("  ❌ [PRICING-KAFKA] Erreur lors de la publication : " + e.getMessage());
            System.err.println("     Cause : " + e.getCause().getMessage());
        } catch (Exception e) {
            System.err.println("  ❌ [PRICING-KAFKA] Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ferme proprement le producer
     */
    public void close() {
        if (producer != null) {
            producer.close();
            System.out.println("✅ [PRICING-KAFKA] Producer de modifications de prix fermé");
        }
    }
}

