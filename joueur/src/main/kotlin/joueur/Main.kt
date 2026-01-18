import model.Jeux
import model.Joueur
import service.Evenement
import java.time.LocalDate
import com.projet.joueur.AchatJeuEvent
import infrastructure.KafkaClientFactory
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Duration
import java.util.*


fun main() {/*
    val j = Joueur("Sniper99", "Dupont", "Jean", LocalDate.of(2000, 5, 15))
    val engine = Evenement(j)
    val elden = Jeux("Elden Ring", 60, listOf("RPG"))

    engine.inscriptionUtilisateurPlateforme()
    engine.achatJeu(elden, "PS5")

    j.mapTempsDeJeux[elden.nomJeux] = 2.5f // Simule le temps passé [cite: 50]
    engine.creerCommentaireJeu(elden)

    try {
        val event = AchatJeuEvent.newBuilder()
            .setPseudo("German")           // Pos 0
            .setNomJeu("Elden Ring")       // Pos 1
            .setSupport("PS5")          // Pos 2 (C'est probablement celui-ci qui manque !)
            .setPrixPaye(50)
            .setTimestamp(System.currentTimeMillis())
            .build()

        println("✅ Succès Avro : Objet créé pour le joueur ${event.getPseudo()}")

        // Dans ton bloc try, après la création de l'event :
        val producer = KafkaClientFactory.createAchatJeuProducer()
        val record = ProducerRecord<String, AchatJeuEvent>("achats-jeux", event.getPseudo().toString(), event)

        producer.send(record) { metadata, exception ->
            if (exception == null) {
                println("🚀 Kafka : Message envoyé dans le topic ${metadata.topic()} (offset: ${metadata.offset()})")
            } else {
                println("❌ Erreur d'envoi Kafka : ${exception.message}")
            }
        }
        producer.flush() // Force l'envoi
        producer.close() // Ferme proprement
    } catch (e: Exception) {
        println("❌ Erreur Avro : ${e.message}")
    }*/

    // --- PARTIE 1 : ENVOI (PRODUCER) ---
    val event = AchatJeuEvent.newBuilder()
        .setPseudo("GermainTest")
        .setNomJeu("Cyberpunk 2077")
        .setSupport("PC")
        .setPrixPaye(30)
        .setTimestamp(System.currentTimeMillis())
        .build()

    val producer = KafkaClientFactory.createAchatJeuProducer()
    val record = ProducerRecord("achats-jeux", event.getPseudo().toString(), event)

    producer.send(record) { metadata, ex ->
        if (ex == null) {
            println("🚀 Envoyé ! Topic: ${metadata.topic()} | Offset: ${metadata.offset()}")
        }
    }
    producer.flush()

    // --- PARTIE 2 : LECTURE (CONSUMER) ---
    println("\n🔍 Tentative de lecture du message...")

    // On crée le consumer avec un Group ID unique pour ce test
    val consumer = KafkaClientFactory.createAchatJeuConsumer("test-group-${UUID.randomUUID()}")

    // On s'abonne au topic
    consumer.subscribe(listOf("achats-jeux"))

    // On fait une petite boucle pour essayer de lire le message
    val records = consumer.poll(Duration.ofSeconds(10)) // On attend max 10s

    if (records.isEmpty) {
        println("⚠️ Aucun message trouvé. Kafka est peut-être encore en train de traiter.")
    } else {
        for (rec in records) {
            val recu = rec.value()
            println("✅ Message reçu de Kafka !")
            println("Joueur : ${recu.getPseudo()} | Jeu : ${recu.getNomJeu()} | Prix : ${recu.getPrixPaye()}€")
        }
    }

    consumer.close()
    producer.close()

}