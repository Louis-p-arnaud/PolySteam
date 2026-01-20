package dao;

import config.DatabaseConfig;
import model.Evaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour la gestion des évaluations en base de données
 */
public class EvaluationDAO {

    /**
     * Récupère toutes les évaluations d'un jeu
     */
    public List<Evaluation> findByJeuId(String jeuId) {
        List<Evaluation> evaluations = new ArrayList<>();
        String sql = """
            SELECT e.id, e.joueur_pseudo, e.note, e.commentaire, 
                   e.nombre_votes_utile, e.nombre_votes_pas_utile, e.date_publication,
                   j.titre as titre_jeu
            FROM evaluation e
            JOIN jeu_catalogue j ON e.jeu_id = j.id
            WHERE e.jeu_id = ?
            ORDER BY e.date_publication DESC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, jeuId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Evaluation eval = new Evaluation(
                    rs.getString("joueur_pseudo"),
                    rs.getString("titre_jeu"),
                    rs.getInt("note"),
                    rs.getString("commentaire")
                );
                eval.setNombreVotesUtile(rs.getInt("nombre_votes_utile"));
                eval.setNombreVotesPasUtile(rs.getInt("nombre_votes_pas_utile"));
                evaluations.add(eval);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des évaluations : " + e.getMessage());
        }

        return evaluations;
    }

    /**
     * Récupère la note moyenne d'un jeu
     */
    public double getNoteMoyenne(String jeuId) {
        String sql = "SELECT AVG(note) as moyenne FROM evaluation WHERE jeu_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, jeuId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("moyenne");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du calcul de la note moyenne : " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Insère une nouvelle évaluation
     */
    public boolean insert(String joueurPseudo, String jeuId, int note, String commentaire) {
        String sql = """
            INSERT INTO evaluation (joueur_pseudo, jeu_id, extension_id, note, commentaire)
            VALUES (?, ?, NULL, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, joueurPseudo);
            pstmt.setString(2, jeuId);
            pstmt.setInt(3, note);
            pstmt.setString(4, commentaire);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'insertion de l'évaluation : " + e.getMessage());
            return false;
        }
    }

    /**
     * Vote sur une évaluation
     */
    public boolean voterEvaluation(int evaluationId, String votantPseudo, boolean estUtile) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Insérer le vote
            String sqlVote = """
                INSERT INTO votes_evaluation (evaluation_id, votant_pseudo, est_utile)
                VALUES (?, ?, ?)
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlVote)) {
                pstmt.setInt(1, evaluationId);
                pstmt.setString(2, votantPseudo);
                pstmt.setBoolean(3, estUtile);
                pstmt.executeUpdate();
            }

            // Mettre à jour le compteur
            String sqlUpdate = estUtile ?
                "UPDATE evaluation SET nombre_votes_utile = nombre_votes_utile + 1 WHERE id = ?" :
                "UPDATE evaluation SET nombre_votes_pas_utile = nombre_votes_pas_utile + 1 WHERE id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setInt(1, evaluationId);
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
            System.err.println("❌ Erreur lors du vote : " + e.getMessage());
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
     * Compte le nombre d'évaluations d'un jeu
     */
    public int countByJeuId(String jeuId) {
        String sql = "SELECT COUNT(*) as total FROM evaluation WHERE jeu_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, jeuId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des évaluations : " + e.getMessage());
        }

        return 0;
    }
}

