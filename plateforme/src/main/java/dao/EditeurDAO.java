package dao;

import config.DatabaseConfig;
import model.Editeur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour la gestion des éditeurs en base de données
 */
public class EditeurDAO {

    /**
     * Récupère tous les éditeurs depuis la base de données
     */
    public List<Editeur> findAll() {
        List<Editeur> editeurs = new ArrayList<>();
        String sql = "SELECT id, nom, est_independant FROM editeur ORDER BY nom";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Editeur editeur = new Editeur(
                    rs.getString("id"),
                    rs.getString("nom"),
                    rs.getBoolean("est_independant")
                );
                editeurs.add(editeur);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des éditeurs : " + e.getMessage());
        }

        return editeurs;
    }

    /**
     * Recherche un éditeur par son nom
     */
    public Editeur findByNom(String nom) {
        String sql = "SELECT id, nom, est_independant FROM editeur WHERE nom = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nom);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Editeur(
                    rs.getString("id"),
                    rs.getString("nom"),
                    rs.getBoolean("est_independant")
                );
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche de l'éditeur : " + e.getMessage());
        }

        return null;
    }

    /**
     * Insère un nouvel éditeur dans la base de données
     */
    public boolean insert(Editeur editeur) {
        String sql = "INSERT INTO editeur (id, nom, est_independant) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, editeur.getId());
            pstmt.setString(2, editeur.getNom());
            pstmt.setBoolean(3, editeur.isEstIndependant());

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'insertion de l'éditeur : " + e.getMessage());
            return false;
        }
    }

    /**
     * Compte le nombre total d'éditeurs
     */
    public int count() {
        String sql = "SELECT COUNT(*) as total FROM editeur";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des éditeurs : " + e.getMessage());
        }

        return 0;
    }
}

