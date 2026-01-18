package infrastructure
import com.projet.joueur.AchatJeuEvent

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig

import java.util.Properties

object KafkaClientFactory {

    fun createAchatProducer(): KafkaProducer<String, AchatJeuEvent> {
        val props = Properties()
        // Configuration de base de Kafka [cite: 82]
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"

        // Configuration spécifique Avro et Schema Registry [cite: 79, 84]
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = "io.confluent.kafka.serializers.KafkaAvroSerializer"
        props["schema.registry.url"] = "http://localhost:8081"

        // Garantie "at-least-once delivery" exigée par le sujet [cite: 24]
        props[ProducerConfig.ACKS_CONFIG] = "all"

        return KafkaProducer<String, AchatJeuEvent>(props)
    }
}