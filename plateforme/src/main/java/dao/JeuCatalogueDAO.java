package dao;

import config.DatabaseConfig;
import model.JeuCatalogue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour la gestion des jeux du catalogue en base de données
 */
public class JeuCatalogueDAO {

    /**
     * Récupère tous les jeux du catalogue
     */
    public List<JeuCatalogue> findAll() {
        List<JeuCatalogue> jeux = new ArrayList<>();
        String sql = """
            SELECT j.id, j.titre, e.nom as editeur_nom, j.plateforme, 
                   j.version_actuelle, j.est_version_anticipee, j.prix_actuel
            FROM jeu_catalogue j
            JOIN editeur e ON j.editeur_id = e.id
            ORDER BY j.titre
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String jeuId = rs.getString("id");
                JeuCatalogue jeu = new JeuCatalogue(
                    jeuId,
                    rs.getString("titre"),
                    rs.getString("editeur_nom"),
                    rs.getString("plateforme"),
                    getGenres(jeuId),
                    rs.getString("version_actuelle"),
                    rs.getBoolean("est_version_anticipee"),
                    rs.getDouble("prix_actuel"), // prix_editeur = prix_actuel pour simplifier
                    rs.getDouble("prix_actuel")
                );

                jeux.add(jeu);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des jeux : " + e.getMessage());
        }

        return jeux;
    }

    /**
     * Récupère les genres d'un jeu
     */
    private List<String> getGenres(String jeuId) {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT genre FROM jeu_genre WHERE jeu_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, jeuId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                genres.add(rs.getString("genre"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des genres : " + e.getMessage());
        }

        return genres;
    }

    /**
     * Recherche un jeu par son ID
     */
    public JeuCatalogue findById(String id) {
        String sql = """
            SELECT j.id, j.titre, e.nom as editeur_nom, j.plateforme, 
                   j.version_actuelle, j.est_version_anticipee, j.prix_actuel
            FROM jeu_catalogue j
            JOIN editeur e ON j.editeur_id = e.id
            WHERE j.id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                JeuCatalogue jeu = new JeuCatalogue(
                    id,
                    rs.getString("titre"),
                    rs.getString("editeur_nom"),
                    rs.getString("plateforme"),
                    getGenres(id),
                    rs.getString("version_actuelle"),
                    rs.getBoolean("est_version_anticipee"),
                    rs.getDouble("prix_actuel"),
                    rs.getDouble("prix_actuel")
                );
                return jeu;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche du jeu : " + e.getMessage());
        }

        return null;
    }

    /**
     * Recherche des jeux par titre (partiel)
     */
    public List<JeuCatalogue> findByTitre(String titre) {
        List<JeuCatalogue> jeux = new ArrayList<>();
        String sql = """
            SELECT j.id, j.titre, e.nom as editeur_nom, j.plateforme, 
                   j.version_actuelle, j.est_version_anticipee, j.prix_actuel
            FROM jeu_catalogue j
            JOIN editeur e ON j.editeur_id = e.id
            WHERE LOWER(j.titre) LIKE LOWER(?)
            ORDER BY j.titre
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + titre + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String jeuId = rs.getString("id");
                JeuCatalogue jeu = new JeuCatalogue(
                    jeuId,
                    rs.getString("titre"),
                    rs.getString("editeur_nom"),
                    rs.getString("plateforme"),
                    getGenres(jeuId),
                    rs.getString("version_actuelle"),
                    rs.getBoolean("est_version_anticipee"),
                    rs.getDouble("prix_actuel"),
                    rs.getDouble("prix_actuel")
                );

                jeux.add(jeu);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche des jeux : " + e.getMessage());
        }

        return jeux;
    }

    /**
     * Insère un nouveau jeu dans le catalogue
     */
    public boolean insert(JeuCatalogue jeu, String editeurId) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Insérer le jeu
            String sqlJeu = """
                INSERT INTO jeu_catalogue (id, titre, editeur_id, plateforme, 
                                          version_actuelle, est_version_anticipee, prix_editeur, prix_actuel)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement pstmt = conn.prepareStatement(sqlJeu)) {
                pstmt.setString(1, jeu.getId());
                pstmt.setString(2, jeu.getTitre());
                pstmt.setString(3, editeurId);
                pstmt.setString(4, jeu.getPlateforme());
                pstmt.setString(5, jeu.getVersionActuelle());
                pstmt.setBoolean(6, jeu.isVersionAnticipee());
                pstmt.setDouble(7, jeu.getPrixEditeur());
                pstmt.setDouble(8, jeu.getPrixActuel());
                pstmt.executeUpdate();
            }

            // Insérer les genres
            String sqlGenre = "INSERT INTO jeu_genre (jeu_id, genre) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlGenre)) {
                for (String genre : jeu.getGenres()) {
                    pstmt.setString(1, jeu.getId());
                    pstmt.setString(2, genre);
                    pstmt.executeUpdate();
                }
            }

            // Insérer la relation éditeur-jeu
            String sqlEditeurJeu = "INSERT INTO editeur_jeu (editeur_id, jeu_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlEditeurJeu)) {
                pstmt.setString(1, editeurId);
                pstmt.setString(2, jeu.getId());
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("❌ Erreur lors du rollback : " + ex.getMessage());
                }
            }
            System.err.println("❌ Erreur lors de l'insertion du jeu : " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("❌ Erreur lors de la fermeture de la connexion : " + e.getMessage());
                }
            }
        }
    }

    /**
     * Met à jour le prix d'un jeu
     */
    public boolean updatePrix(String jeuId, double nouveauPrix) {
        String sql = "UPDATE jeu_catalogue SET prix_actuel = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, nouveauPrix);
            pstmt.setString(2, jeuId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du prix : " + e.getMessage());
            return false;
        }
    }

    /**
     * Compte le nombre total de jeux
     */
    public int count() {
        String sql = "SELECT COUNT(*) as total FROM jeu_catalogue";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des jeux : " + e.getMessage());
        }

        return 0;
    }
}

