package config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration de la connexion à la base de données PostgreSQL
 */
public class DatabaseConfig {

    // Configuration de connexion (chargée depuis .env)
    private static String DB_HOST;
    private static String DB_PORT;
    private static String DB_NAME;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_URL;

    // Bloc d'initialisation statique pour charger les variables d'environnement
    static {
        loadEnvFile();
        DB_URL = String.format(
            "jdbc:postgresql://%s:%s/%s",
            DB_HOST, DB_PORT, DB_NAME
        );
    }

    // Singleton pour la connexion
    private static Connection connection = null;

    /**
     * Charge les variables d'environnement depuis le fichier .env
     */
    private static void loadEnvFile() {
        Map<String, String> envVars = new HashMap<>();

        // Essayer de charger depuis le fichier .env
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Ignorer les commentaires et lignes vides
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    envVars.put(parts[0].trim(), parts[1].trim());
                }
            }
            System.out.println("✅ Fichier .env chargé avec succès");
        } catch (IOException e) {
            System.err.println("⚠️ Impossible de charger le fichier .env : " + e.getMessage());
            System.err.println("Utilisation des variables d'environnement système...");
        }

        // Charger les variables (priorité aux variables système, sinon .env)
        DB_HOST = System.getenv("DB_HOST");
        if (DB_HOST == null) DB_HOST = envVars.get("DB_HOST");

        DB_PORT = System.getenv("DB_PORT");
        if (DB_PORT == null) DB_PORT = envVars.get("DB_PORT");

        DB_NAME = System.getenv("DB_NAME");
        if (DB_NAME == null) DB_NAME = envVars.get("DB_NAME");

        DB_USER = System.getenv("DB_USER");
        if (DB_USER == null) DB_USER = envVars.get("DB_USER");

        DB_PASSWORD = System.getenv("DB_PASSWORD");
        if (DB_PASSWORD == null) DB_PASSWORD = envVars.get("DB_PASSWORD");

        // Vérifier que toutes les variables sont définies
        if (DB_HOST == null || DB_PORT == null || DB_NAME == null ||
            DB_USER == null || DB_PASSWORD == null) {
            throw new RuntimeException(
                "❌ Configuration incomplète ! Vérifiez que toutes les variables " +
                "sont définies dans le fichier .env ou les variables d'environnement système."
            );
        }
    }

    /**
     * Obtenir une connexion à la base de données
     * @return Connection active
     * @throws SQLException en cas d'erreur de connexion
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Charger le driver PostgreSQL
                Class.forName("org.postgresql.Driver");

                // Créer la connexion
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                // Message de connexion uniquement au tout premier appel (optionnel)
                // System.out.println("✅ Connexion à la base de données établie");

            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver PostgreSQL non trouvé !");
                System.err.println("Ajoutez la dépendance PostgreSQL dans pom.xml :");
                System.err.println("<dependency>");
                System.err.println("    <groupId>org.postgresql</groupId>");
                System.err.println("    <artifactId>postgresql</artifactId>");
                System.err.println("    <version>42.7.1</version>");
                System.err.println("</dependency>");
                throw new SQLException("Driver PostgreSQL non disponible", e);
            }
        }
        return connection;
    }

    /**
     * Fermer la connexion à la base de données
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("🔌 Connexion à la base de données fermée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }

    /**
     * Tester la connexion à la base de données
     * @return true si la connexion fonctionne
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            boolean isValid = conn.isValid(5); // Timeout de 5 secondes

            if (isValid) {
                System.out.println("✅ Test de connexion réussi !");

                // Afficher des infos sur la base
                var metaData = conn.getMetaData();
                System.out.println("📊 Base de données : " + metaData.getDatabaseProductName());
                System.out.println("📊 Version : " + metaData.getDatabaseProductVersion());
                System.out.println("📊 URL : " + metaData.getURL());
                System.out.println("📊 Utilisateur : " + metaData.getUserName());
            } else {
                System.err.println("❌ Test de connexion échoué !");
            }

            return isValid;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du test de connexion : " + e.getMessage());
            return false;
        }
    }

    /**
     * Configuration pour environnement de production
     * Permet de changer facilement les paramètres sans recompiler
     */
    public static void configureFromEnvironment() {
        loadEnvFile();
        DB_URL = String.format(
            "jdbc:postgresql://%s:%s/%s",
            DB_HOST, DB_PORT, DB_NAME
        );
        System.out.println("🔄 Configuration rechargée depuis les variables d'environnement");
    }

    // Getters pour accéder aux infos de configuration
    public static String getDbUrl() {
        return DB_URL;
    }

    public static String getDbName() {
        return DB_NAME;
    }

    public static String getDbUser() {
        return DB_USER;
    }
}

