package dao;

import config.DatabaseConfig;
import model.RapportIncident;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour la gestion des rapports d'incidents en base de données
 */
public class RapportIncidentDAO {

    /**
     * Récupère tous les incidents d'un jeu
     */
    public List<RapportIncident> findByJeuId(String jeuId) {
        List<RapportIncident> incidents = new ArrayList<>();
        String sql = """
            SELECT id, joueur_pseudo, version_jeu, plateforme, description_erreur, date_survenue
            FROM rapport_incident
            WHERE jeu_id = ?
            ORDER BY date_survenue DESC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, jeuId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                RapportIncident incident = new RapportIncident(
                    rs.getString("joueur_pseudo"),
                    jeuId,
                    rs.getString("version_jeu"),
                    rs.getString("plateforme"),
                    rs.getString("description_erreur")
                );
                incident.setId(rs.getString("id"));
                incidents.add(incident);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des incidents : " + e.getMessage());
        }

        return incidents;
    }

    /**
     * Insère un nouveau rapport d'incident
     */
    public boolean insert(RapportIncident incident) {
        String sql = """
            INSERT INTO rapport_incident (id, jeu_id, joueur_pseudo, version_jeu, plateforme, description_erreur)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, incident.getId());
            pstmt.setString(2, incident.getJeuId());
            pstmt.setString(3, incident.getJoueurPseudo());
            pstmt.setString(4, incident.getVersionJeu());
            pstmt.setString(5, incident.getPlateforme());
            pstmt.setString(6, incident.getDescriptionErreur());

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'insertion de l'incident : " + e.getMessage());
            return false;
        }
    }

    /**
     * Compte le nombre d'incidents d'un jeu
     */
    public int countByJeuId(String jeuId) {
        String sql = "SELECT COUNT(*) as total FROM rapport_incident WHERE jeu_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, jeuId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des incidents : " + e.getMessage());
        }

        return 0;
    }

    /**
     * Compte le nombre total d'incidents
     */
    public int count() {
        String sql = "SELECT COUNT(*) as total FROM rapport_incident";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des incidents : " + e.getMessage());
        }

        return 0;
    }
}

