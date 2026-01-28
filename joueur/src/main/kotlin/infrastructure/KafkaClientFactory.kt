package infrastructure

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord // Import indispensable
import java.util.*

object KafkaClientFactory {
    // N'oublie pas de mettre tes vraies adresses IP ici si elles sont différentes de localhost
    private const val BOOTSTRAP_SERVERS = "86.252.172.215:9092"
    private const val SCHEMA_REGISTRY_URL = "http://86.252.172.215:8081"

    private fun getCommonProps(): Properties {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVERS
        props["schema.registry.url"] = SCHEMA_REGISTRY_URL
        return props
    }

    // Fonction générique pour créer n'importe quel Producer Avro
    @JvmStatic
    fun createGenericProducer(): KafkaProducer<String, GenericRecord> {
        val props = getCommonProps()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java.name
        return KafkaProducer(props)
    }

    // Si tu as besoin de garder les noms de fonctions pour ne pas casser le reste du code :

    @JvmStatic
    fun createAchatJeuProducer() = createGenericProducer()

    @JvmStatic
    fun createInscriptionProducer() = createGenericProducer()

    @JvmStatic
    fun createTempsJeuProducer() = createGenericProducer()

    @JvmStatic
    fun createEvaluationProducer() = createGenericProducer()

    @JvmStatic
    fun createRapportIncidentProducer() = createGenericProducer()

    @JvmStatic
    fun createAchatJeuConsumer(groupId: String): KafkaConsumer<String, GenericRecord> {
        val props = getCommonProps()
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java.name
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        // On désactive le specific reader car on utilise GenericRecord
        props["specific.avro.reader"] = "false"
        return KafkaConsumer(props)
    }
}