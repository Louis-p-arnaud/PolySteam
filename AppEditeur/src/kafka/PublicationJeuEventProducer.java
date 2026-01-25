package kafka;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.apache.kafka.common.serialization.StringSerializer;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Producer Kafka pour publier les événements de publication d'un jeu
 * Utilise Avro (GenericRecord) et KafkaAvroSerializer pour sérialiser la valeur.
 */
public class PublicationJeuEventProducer {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "86.252.172.215:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://86.252.172.215:8081";
    private static final String DEFAULT_TOPIC = "polysteam.publication.jeu";

    private final KafkaProducer<String, GenericRecord> producer;
    private final Schema schema;
    private final String topicName;

    /**
     * Constructeur avec configuration par défaut (localhost). Pour production,
     * passez des propriétés personnalisées via createWithProperties(...)
     */
    public PublicationJeuEventProducer() {
        this(createDefaultProperties());
    }

    /**
     * Constructeur principal prenant des properties Kafka (bootstrap + schema.registry.url)
     */
    public PublicationJeuEventProducer(Properties props) {
        this.topicName = props.getProperty("publication.topic", DEFAULT_TOPIC);

        // Charger le schéma Avro depuis la ressource classpath kafka/avro/PublicationJeuEvent.avsc
        Schema parsedSchema = null;
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("kafka/avro/PublicationJeuEvent.avsc")) {
            if (in != null) {
                try (Scanner s = new Scanner(in, StandardCharsets.UTF_8.name())) {
                    String schemaJson = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                    parsedSchema = new Schema.Parser().parse(schemaJson);
                }
            } else {
                System.err.println("⚠️ Ressource kafka/avro/PublicationJeuEvent.avsc introuvable dans le classpath — fallback au schéma minimal embarqué");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du schéma Avro depuis ressource : " + e.getMessage());
        }

        this.schema = parsedSchema;

        // Forcer les sérializers attendus
        props.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

        this.producer = new KafkaProducer<>(props);

        System.out.println("✅ Kafka Producer (PublicationJeu) initialisé, topic=" + this.topicName);
    }

    private static Properties createDefaultProperties() {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        p.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        p.put(ProducerConfig.ACKS_CONFIG, "all");
        p.put(ProducerConfig.RETRIES_CONFIG, Integer.toString(3));
        p.put(ProducerConfig.CLIENT_ID_CONFIG, "appediteur-publication-producer");
        // timeouts et tuning raisonnable
        p.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Long.toString(TimeUnit.SECONDS.toMillis(60)));
        p.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, Long.toString((int)TimeUnit.SECONDS.toMillis(60)));
        p.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        return p;
    }

    /**
     * Publie un événement de publication d'un jeu. La méthode construit un GenericRecord
     * conforme au schéma Avro chargé puis envoie le message sur Kafka.
     */
    public void publierPublication(
            String editeurId,
            String nom,
            String plateforme,
            List<String> genres,
            String numeroVersion,
            boolean versionAnticipe
    ) {
        try {
            System.out.println("  🔨 Création de l'événement PublicationJeu Avro...");

            GenericRecordBuilder builder = new GenericRecordBuilder(schema);
            builder.set("eventId", UUID.randomUUID().toString());
            builder.set("timestamp", System.currentTimeMillis());
            builder.set("editeurId", editeurId != null ? editeurId : "");
            builder.set("nom", nom != null ? nom : "");
            builder.set("plateforme", plateforme != null ? plateforme : "");

            // Convertir la liste en org.apache.avro.generic.GenericData.Array
            GenericData.Array<String> avroGenres = new GenericData.Array<>(
                    genres != null ? genres.size() : 0, schema.getField("genres").schema());
            if (genres != null) {
                for (String g : genres) {
                    avroGenres.add(g);
                }
            }
            builder.set("genres", avroGenres);

            builder.set("numeroVersion", numeroVersion != null ? numeroVersion : "");
            builder.set("versionAnticipe", versionAnticipe);

            GenericRecord event = builder.build();

            System.out.println("  ✅ Événement PublicationJeu Avro créé");

            ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(this.topicName, editeurId, event);

            var future = producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("  ❌ Erreur callback PublicationJeu : " + exception.getMessage());
                } else {
                    System.out.println("  ✅ PublicationJeu reçue par Kafka (partition=" + metadata.partition() + ", offset=" + metadata.offset() + ")");
                }
            });

            // Attendre confirmation (bloquant)
            System.out.println("  ⏳ Attente de la confirmation Kafka...");
            future.get();

            System.out.println("✅ Publication de jeu publiée avec succès : " + nom);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'événement PublicationJeu pour le jeu: " + nom);
            System.err.println("    Message : " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("    Cause : " + e.getCause().getMessage());
            }
        }
    }

    /**
     * Ferme proprement le producer
     */
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
            System.out.println("✅ Kafka Producer (PublicationJeu) fermé");
        }
    }
}
