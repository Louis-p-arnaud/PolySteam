import config.DatabaseConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Classe de test pour vérifier la connexion à la base de données PostgreSQL
 */
public class TestDatabase {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("🧪 TEST DE CONNEXION À LA BASE DE DONNÉES POLYSTEAM");
        System.out.println("=".repeat(60));
        System.out.println();

        // Test 1 : Connexion basique
        System.out.println("📝 Test 1 : Connexion à la base de données");
        System.out.println("-".repeat(60));
        boolean connected = DatabaseConfig.testConnection();
        System.out.println();

        if (!connected) {
            System.err.println("❌ Impossible de continuer les tests sans connexion");
            System.err.println("\n💡 Vérifiez que :");
            System.err.println("   1. Le conteneur Docker est démarré : docker ps");
            System.err.println("   2. PostgreSQL écoute sur le port 5432");
            System.err.println("   3. Les identifiants sont corrects dans DatabaseConfig.java");
            System.err.println("   4. Le firewall autorise la connexion");
            return;
        }

        // Test 2 : Compter les tables
        System.out.println("📝 Test 2 : Vérification des tables");
        System.out.println("-".repeat(60));
        testTables();
        System.out.println();

        // Test 3 : Compter les données
        System.out.println("📝 Test 3 : Comptage des données");
        System.out.println("-".repeat(60));
        testData();
        System.out.println();

        // Test 4 : Requête sur les jeux
        System.out.println("📝 Test 4 : Requête sur les jeux");
        System.out.println("-".repeat(60));
        testJeux();
        System.out.println();

        // Fermeture
        DatabaseConfig.closeConnection();

        System.out.println("=".repeat(60));
        System.out.println("✅ TOUS LES TESTS SONT TERMINÉS !");
        System.out.println("=".repeat(60));
    }

    private static void testTables() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = """
                SELECT table_name 
                FROM information_schema.tables 
                WHERE table_schema = 'public' 
                  AND table_type = 'BASE TABLE'
                ORDER BY table_name;
                """;

            ResultSet rs = stmt.executeQuery(query);

            int count = 0;
            System.out.println("📋 Tables trouvées :");
            while (rs.next()) {
                count++;
                System.out.println("   " + count + ". " + rs.getString("table_name"));
            }

            System.out.println("\n✅ Total : " + count + " tables");

        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }

    private static void testData() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = """
                SELECT 'Éditeurs' AS type, COUNT(*) AS nombre FROM editeur
                UNION ALL
                SELECT 'Jeux', COUNT(*) FROM jeu_catalogue
                UNION ALL
                SELECT 'Extensions', COUNT(*) FROM extension
                UNION ALL
                SELECT 'Joueurs', COUNT(*) FROM joueur
                UNION ALL
                SELECT 'Évaluations', COUNT(*) FROM evaluation
                UNION ALL
                SELECT 'Incidents', COUNT(*) FROM rapport_incident
                UNION ALL
                SELECT 'Patches', COUNT(*) FROM patch;
                """;

            ResultSet rs = stmt.executeQuery(query);

            System.out.println("📊 Données dans la base :");
            while (rs.next()) {
                String type = rs.getString("type");
                int nombre = rs.getInt("nombre");
                System.out.printf("   %-20s : %d\n", type, nombre);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }

    private static void testJeux() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = """
                SELECT j.titre, e.nom as editeur, j.plateforme, j.prix_actuel
                FROM jeu_catalogue j
                JOIN editeur e ON j.editeur_id = e.id
                ORDER BY j.titre
                LIMIT 5;
                """;

            ResultSet rs = stmt.executeQuery(query);

            System.out.println("🎮 Quelques jeux du catalogue :");
            System.out.println();
            while (rs.next()) {
                String titre = rs.getString("titre");
                String editeur = rs.getString("editeur");
                String plateforme = rs.getString("plateforme");
                double prix = rs.getDouble("prix_actuel");

                System.out.printf("   📦 %s\n", titre);
                System.out.printf("      Éditeur    : %s\n", editeur);
                System.out.printf("      Plateforme : %s\n", plateforme);
                System.out.printf("      Prix       : %.2f €\n", prix);
                System.out.println();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }
}

