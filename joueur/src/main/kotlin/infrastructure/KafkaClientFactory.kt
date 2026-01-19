package infrastructure
import com.projet.joueur.AchatJeuEvent

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig

import java.util.Properties
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import java.util.*
import com.projet.joueur.TempsJeuEvent


object KafkaClientFactory {
    private const val BOOTSTRAP_SERVERS = "localhost:9092"
    private const val SCHEMA_REGISTRY_URL = "http://localhost:8081"

    private fun getCommonProps(): Properties {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVERS
        props["schema.registry.url"] = SCHEMA_REGISTRY_URL
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = org.apache.kafka.common.serialization.StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = io.confluent.kafka.serializers.KafkaAvroSerializer::class.java.name
        return props
    }

    @JvmStatic
    fun createAchatJeuProducer(): KafkaProducer<String, AchatJeuEvent> {
        val props = getCommonProps()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        return KafkaProducer(props)
    }

    @JvmStatic
    fun createAchatJeuConsumer(groupId: String): KafkaConsumer<String, AchatJeuEvent> {
        val props = getCommonProps()
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        // Pour que Java reconnaisse les classes Avro spécifiques
        props["specific.avro.reader"] = "true"
        return KafkaConsumer(props)
    }

    @JvmStatic
    fun createInscriptionProducer(): KafkaProducer<String, com.projet.joueur.InscriptionEvent> {
        val props = getCommonProps()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        return KafkaProducer(props)
    }

    @JvmStatic
    fun createTempsJeuProducer(): KafkaProducer<String, TempsJeuEvent> {
        return KafkaProducer<String, TempsJeuEvent>(getCommonProps())
    }
}