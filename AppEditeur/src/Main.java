import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import kafka.CommentairesConsumer;
import kafka.IncidentRoutingStream;
import kafka.PublicationJeuEventProducer;
import model.Editeur;
import model.Enums;
import model.Jeu;
import model.Patch;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public class Main {

    private static Scanner scanner = new Scanner(System.in);
    private static List<Editeur> editeurs = new ArrayList<>();
    private static int compteurPatch = 1;

    // Configuration Base de données PostgreSQL
    private static final String DB_URL = "jdbc:postgresql://86.252.172.215:5432/polysteam";
    private static final String DB_USER = "polysteam_user";
    private static final String DB_PASS = "PolySteam2026!";
    private static Connection pgConnection;

    // Configuration Kafka
    private static KafkaProducer<String, GenericRecord> producer;
    private static final String BOOTSTRAP_SERVERS = "86.252.172.215:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://86.252.172.215:8081";


    public static void main(String[] args) {
        // 1. Désactiver les logs techniques Kafka
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");

        // 2. Initialiser les connexions (Postgres & Kafka Producer)
        initConnections();

        // 3. Lancer le Dashboard et les services de fond
        SwingUtilities.invokeLater(ui.EditeurDashboard::createAndShow);
        startBackgroundThreads();

        // 4. Hook de fermeture propre
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (pgConnection != null) pgConnection.close();
                if (producer != null) { producer.flush(); producer.close(); }
                System.out.println("\n[System] Connexions fermées proprement.");
            } catch (SQLException e) { e.printStackTrace(); }
        }));

        // Attente pour laisser les services démarrer proprement
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        // Initialisation des éditeurs
        editeurs.add(new Editeur("Ubisoft", Enums.TYPE_EDITEUR.ENTREPRISE));
        editeurs.add(new Editeur("Valve", Enums.TYPE_EDITEUR.ENTREPRISE));
        editeurs.add(new Editeur("Studio Indie", Enums.TYPE_EDITEUR.INDEPENDANT));

        //Envoi des éditeurs à la BDD
        synchroniserEditeurs();

        runMenu();
    }

    private static void initConnections() {
        try {
            // Connexion Postgres
            pgConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            // Connexion Kafka Producer Avro
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
            props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);

            producer = new KafkaProducer<>(props);
            System.out.println("✅ Connexions Postgres et Kafka OK.");
        } catch (Exception e) {
            System.err.println("❌ Erreur d'initialisation : " + e.getMessage());
        }
    }

    private static void runMenu() {
        char choix;
        do {
            afficherMenu();
            String input = scanner.nextLine();
            choix = (input.isEmpty()) ? ' ' : input.charAt(0);

            switch (choix) {
                case '1': publierJeu(); break;
                case '2': publierPatch(); break;
                case 'q': System.out.println("Au revoir !"); break;
                default: System.out.println("Choix incorrect.");
            }
        } while (choix != 'q');
    }

    private static void afficherMenu() {
        System.out.println("\n=== MENU PRINCIPAL (POSTGRES + KAFKA) ===");
        System.out.println("1. Publier un nouveau jeu");
        System.out.println("2. Publier un correctif (Patch)");
        System.out.println("q. Quitter");
        System.out.print("Votre choix : ");
    }

    // ---------------------------------------------------------
    // LOGIQUE 1 : PUBLICATION JEU (INSERT SQL + KAFKA)
    // ---------------------------------------------------------
    private static void publierJeu() {
        Editeur editeur = choisirEditeur();
        if (editeur == null) return;

        System.out.print("Nom du jeu : ");
        String nom = scanner.nextLine();
        Enums.PLATEFORME_EXECUTION plateforme = choisirPlateforme();
        List<Enums.GENRE> genres = choisirGenres();
        System.out.print("Version : ");
        String version = scanner.nextLine();
        System.out.print("Accès anticipé (true/false) : ");
        boolean anticipe = Boolean.parseBoolean(scanner.nextLine());
        System.out.print("Prix éditeur : ");
        double prix = scanner.nextDouble();
        scanner.nextLine(); // Consommer le retour à la ligne après nextDouble()

        Jeu jeuAPublier = new Jeu(nom, editeur, plateforme,genres, version, anticipe, prix);

        try {
            // 1. Sauvegarde PostgreSQL, conforme à la table dans PGAdmin
            String sql = "INSERT INTO public.jeu_catalogue (id, titre, editeur_id, plateforme, version_actuelle, est_version_anticipee, prix_editeur, prix_actuel) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = pgConnection.prepareStatement(sql);
            ps.setString(1, jeuAPublier.getId().toString());
            ps.setString(2, nom);
            ps.setString(3, editeur.getId().toString());// ID de l'Editeur (La Clé Étrangère)
            ps.setString(4, plateforme.toString());
            ps.setString(5, version);
            ps.setBoolean(6, anticipe);
            ps.setDouble(7, prix);
            ps.setDouble(8, prix);
            ps.executeUpdate();

            // 2. Envoi vers Kafka
            editeur.publierJeu(jeuAPublier);

            ui.EditeurDashboard.log("📦 JEU CRÉÉ : " + nom + " par " + editeur.getNom() + " (v" + version + ")");
            System.out.println("✅ Jeu enregistré en base et envoyé sur Kafka.");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL : " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // LOGIQUE 2 : PUBLICATION PATCH (VERIF EXISTENCE + UPDATE SQL + KAFKA)
    // ---------------------------------------------------------
    private static void publierPatch() {
        Editeur editeur = choisirEditeur();
        if (editeur == null) return;

        System.out.print("Nom du jeu à patcher : (veillez à entrer le nom exact) ");
        String jeuNom = scanner.nextLine();

        // VÉRIFICATION CRITIQUE : Le jeu existe-t-il dans Postgres ?
        if (!verifierExistenceJeu(jeuNom, editeur.getId().toString())) {
            System.out.println("⚠️ ACTION ANNULÉE : Le jeu '" + jeuNom + "' n'existe pas en base pour l'éditeur " + editeur.getNom());
            return;
        }

        System.out.print("Nouvelle version : ");
        String nouvelleVersion = scanner.nextLine();
        System.out.print("Commentaire : ");
        String commentaire = scanner.nextLine();

        List<String> modifs = new ArrayList<>();
        System.out.println("Modifications (tapez 'fin') :");
        while (true) {
            String m = scanner.nextLine();
            if (m.equalsIgnoreCase("fin")) break;
            modifs.add(m);
        }

        //on créé un objet patch qu'on publiera ensuite
        Patch patchAPublier = new Patch(jeuNom, commentaire, nouvelleVersion, modifs);

        try {
            // 1. Mise à jour PostgreSQL (On met à jour la version actuelle du jeu)
            String sql = "UPDATE public.jeu_catalogue SET version_actuelle = ? WHERE titre = ? AND editeur_id = ?";
            PreparedStatement ps = pgConnection.prepareStatement(sql);
            ps.setString(1, nouvelleVersion);
            ps.setString(2, jeuNom);
            ps.setString(3, editeur.getId().toString());
            ps.executeUpdate();

            // 2. Envoi Kafka
            editeur.publierPatch(patchAPublier);

            ui.EditeurDashboard.log("🔧 PATCH APPLIQUÉ : " + jeuNom + " passe en v" + nouvelleVersion);
            System.out.println("✅ Patch validé en base et publié.");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL Patch : " + e.getMessage());
        }
    }

    // --- HELPERS DB & KAFKA ---

    private static boolean verifierExistenceJeu(String nom, String editeurId) {
        try {
            String sql = "SELECT COUNT(*) FROM public.jeu_catalogue WHERE titre = ? AND editeur_id = ?";
            PreparedStatement ps = pgConnection.prepareStatement(sql);
            ps.setString(1, nom);
            ps.setString(2, editeurId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }


    private static void startBackgroundThreads() {
        // Router Kafka Streams
        new Thread(() -> IncidentRoutingStream.main(new String[]{})).start();

        // Consumer SQLite
        new Thread(() -> {
            CommentairesConsumer c = new CommentairesConsumer("app-editeur-group", "plateforme.evaluations", "plateforme.incidents");
            c.runLoop();
        }).start();
    }

    // --- OUTILS DE SÉLECTION ---

    private static Editeur choisirEditeur() {
        System.out.println("\nChoisissez un éditeur :");
        for (int i = 0; i < editeurs.size(); i++) System.out.println((i + 1) + ". " + editeurs.get(i).getNom());
        try {
            int choix = Integer.parseInt(scanner.nextLine()) - 1;
            return (choix >= 0 && choix < editeurs.size()) ? editeurs.get(choix) : null;
        } catch (Exception e) { return null; }
    }

    private static Enums.PLATEFORME_EXECUTION choisirPlateforme() {
        Enums.PLATEFORME_EXECUTION[] values = Enums.PLATEFORME_EXECUTION.values();
        System.out.println("Plateforme :");
        for (int i = 0; i < values.length; i++) System.out.println((i + 1) + ". " + values[i]);
        return values[Integer.parseInt(scanner.nextLine()) - 1];
    }

    private static List<Enums.GENRE> choisirGenres() {
        List<Enums.GENRE> res = new ArrayList<>();
        Enums.GENRE[] all = Enums.GENRE.values();
        System.out.println("Genres (ex: 1 3) :");
        for (int i = 0; i < all.length; i++) System.out.println((i + 1) + ". " + all[i]);
        String[] ids = scanner.nextLine().split(" ");
        for (String id : ids) res.add(all[Integer.parseInt(id) - 1]);
        return res;
    }

    /**
     * Insère les éditeurs dans la table 'editeur' s'ils n'existent pas encore.
     * Aligné sur la structure : id (varchar 36), nom (varchar 255), est_independant (bool)
     */
    private static void synchroniserEditeurs() {
        System.out.println("🔄 Synchronisation et récupération des IDs éditeurs...");

        // Requêtes SQL
        String selectSql = "SELECT id FROM public.editeur WHERE nom = ?";
        String insertSql = "INSERT INTO public.editeur (id, nom, est_independant, date_creation) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        for (Editeur e : editeurs) {
            try {
                // 1. Vérifier si l'éditeur existe déjà par son NOM
                PreparedStatement selectPs = pgConnection.prepareStatement(selectSql);
                selectPs.setString(1, e.getNom());
                ResultSet rs = selectPs.executeQuery();

                if (rs.next()) {
                    // L'éditeur existe : on récupère l'ID de la BDD et on l'injecte dans l'objet Java
                    String dbId = rs.getString("id");
                    e.setId(UUID.fromString(dbId));
                    System.out.println("  🔗 Editeur '" + e.getNom() + "' trouvé. ID synchronisé : " + dbId);
                } else {
                    // L'éditeur n'existe pas : on l'insère avec l'UUID généré à l'instanciation
                    PreparedStatement insertPs = pgConnection.prepareStatement(insertSql);
                    insertPs.setString(1, e.getId().toString());
                    insertPs.setString(2, e.getNom());
                    insertPs.setBoolean(3, e.getType() == Enums.TYPE_EDITEUR.INDEPENDANT);
                    insertPs.executeUpdate();
                    System.out.println("  🆕 Editeur '" + e.getNom() + "' créé en base.");
                }
            } catch (SQLException ex) {
                System.err.println("❌ Erreur synchro pour " + e.getNom() + " : " + ex.getMessage());
            }
        }
    }

}


