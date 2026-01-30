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
 * Producer Kafka pour publier les événements de publication d'un patch
 * Utilise Avro (GenericRecord) et KafkaAvroSerializer pour sérialiser la valeur.
 */
public class PublicationPatchEventProducer {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "86.252.172.215:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://86.252.172.215:8081";
    private static final String DEFAULT_TOPIC = "editeur.publications.patchs";

    private final KafkaProducer<String, GenericRecord> producer;
    private final Schema schema;
    private final String topicName;

    public PublicationPatchEventProducer() {
        this(createDefaultProperties());
    }

    public PublicationPatchEventProducer(Properties props) {
        this.topicName = props.getProperty("publication.topic", DEFAULT_TOPIC);

        Schema parsedSchema = null;
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("kafka/avro/PublicationPatchEvent.avsc")) {
            if (in != null) {
                try (Scanner s = new Scanner(in, StandardCharsets.UTF_8.name())) {
                    String schemaJson = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                    parsedSchema = new Schema.Parser().parse(schemaJson);
                }
            } else {
                System.err.println("⚠️ Ressource kafka/avro/PublicationPatchEvent.avsc introuvable dans le classpath — fallback au schéma minimal embarqué");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du schéma Avro PublicationPatch : " + e.getMessage());
        }

        this.schema = parsedSchema;

        props.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());

        this.producer = new KafkaProducer<>(props);

        System.out.println("✅ Kafka Producer (PublicationPatch) initialisé, topic=" + this.topicName);
    }

    private static Properties createDefaultProperties() {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        p.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        p.put(ProducerConfig.ACKS_CONFIG, "all");
        p.put(ProducerConfig.RETRIES_CONFIG, Integer.toString(3));
        p.put(ProducerConfig.CLIENT_ID_CONFIG, "appediteur-publication-patch-producer");
        p.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Long.toString(TimeUnit.SECONDS.toMillis(60)));
        p.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, Long.toString((int)TimeUnit.SECONDS.toMillis(60)));
        p.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        return p;
    }

    public void publierPublicationPatch(
            UUID editeurId,
            String jeuNom,
            UUID idPatch,
            String commentaireEditeur,
            String nouvelleVersion,
            List<String> modifications
    ) {
        try {
            System.out.println("  🔨 Création de l'événement PublicationPatch Avro...");

            GenericRecordBuilder builder = new GenericRecordBuilder(schema);
            builder.set("eventId", UUID.randomUUID().toString());
            builder.set("timestamp", System.currentTimeMillis());
            builder.set("editeurId", editeurId != null ? editeurId.toString() : "");
            builder.set("jeuNom", jeuNom != null ? jeuNom : "");
            builder.set("idPatch", idPatch.toString());
            builder.set("commentaireEditeur", commentaireEditeur);

            GenericData.Array<String> avroMods = new GenericData.Array<>(
                    modifications != null ? modifications.size() : 0, schema.getField("modifications").schema());
            if (modifications != null) {
                for (String m : modifications) {
                    avroMods.add(m);
                }
            }
            builder.set("modifications", avroMods);

            builder.set("nouvelleVersion", nouvelleVersion != null ? nouvelleVersion : "");

            GenericRecord event = builder.build();

            System.out.println("  ✅ Événement PublicationPatch Avro créé");

            ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(this.topicName, editeurId.toString(), event);

            var future = producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("  ❌ Erreur callback PublicationPatch : " + exception.getMessage());
                } else {
                    System.out.println("  ✅ PublicationPatch reçue par Kafka (partition=" + metadata.partition() + ", offset=" + metadata.offset() + ")");
                }
            });

            System.out.println("  ⏳ Attente de la confirmation Kafka...");
            future.get();

            System.out.println("✅ Publication du patch publiée avec succès pour : " + jeuNom);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'événement PublicationPatch pour le jeu: " + jeuNom);
            System.err.println("    Message : " + e.getMessage());
            if (e.getCause() != null) System.err.println("    Cause : " + e.getCause().getMessage());
        }
    }

    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
            System.out.println("✅ Kafka Producer (PublicationPatch) fermé");
        }
    }
}
