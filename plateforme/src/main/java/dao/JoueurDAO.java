package dao;

import config.DatabaseConfig;
import model.Joueur;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour la gestion des joueurs en base de données
 */
public class JoueurDAO {

    /**
     * Récupère tous les joueurs depuis la base de données
     */
    public List<Joueur> findAll() {
        List<Joueur> joueurs = new ArrayList<>();
        String sql = "SELECT pseudo, nom, prenom, date_naissance FROM joueur ORDER BY pseudo";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Joueur joueur = new Joueur(
                    rs.getString("pseudo"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getDate("date_naissance").toLocalDate()
                );
                joueurs.add(joueur);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des joueurs : " + e.getMessage());
        }

        return joueurs;
    }

    /**
     * Recherche un joueur par son pseudo
     */
    public Joueur findByPseudo(String pseudo) {
        String sql = "SELECT pseudo, nom, prenom, date_naissance FROM joueur WHERE pseudo = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pseudo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Joueur(
                    rs.getString("pseudo"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getDate("date_naissance").toLocalDate()
                );
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche du joueur : " + e.getMessage());
        }

        return null;
    }

    /**
     * Insère un nouveau joueur dans la base de données
     */
    public boolean insert(Joueur joueur) {
        String sql = "INSERT INTO joueur (pseudo, nom, prenom, date_naissance) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, joueur.getPseudo());
            pstmt.setString(2, joueur.getNom());
            pstmt.setString(3, joueur.getPrenom());
            pstmt.setDate(4, Date.valueOf(joueur.getDateNaissance()));

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'insertion du joueur : " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère les amis d'un joueur (relations acceptées uniquement)
     */
    public List<Joueur> findAmis(String pseudo) {
        List<Joueur> amis = new ArrayList<>();
        String sql = """
            SELECT j.pseudo, j.nom, j.prenom, j.date_naissance
            FROM joueur j
            JOIN ami a ON j.pseudo = a.ami_pseudo
            WHERE a.joueur_pseudo = ? AND a.statut = 'ACCEPTE'
            ORDER BY j.pseudo
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pseudo);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Joueur ami = new Joueur(
                    rs.getString("pseudo"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getDate("date_naissance").toLocalDate()
                );
                amis.add(ami);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des amis : " + e.getMessage());
        }

        return amis;
    }

    /**
     * Ajoute une relation d'amitié (statut EN_ATTENTE)
     */
    public boolean ajouterDemandeAmi(String joueur, String ami) {
        String sql = "INSERT INTO ami (joueur_pseudo, ami_pseudo, statut) VALUES (?, ?, 'EN_ATTENTE')";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, joueur);
            pstmt.setString(2, ami);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la demande d'ami : " + e.getMessage());
            return false;
        }
    }

    /**
     * Accepte une demande d'amitié (crée la relation bidirectionnelle)
     */
    public boolean accepterDemandeAmi(String joueur, String ami) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // Mettre à jour le statut de la demande
            String sqlUpdate = "UPDATE ami SET statut = 'ACCEPTE' WHERE joueur_pseudo = ? AND ami_pseudo = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setString(1, ami);
                pstmt.setString(2, joueur);
                pstmt.executeUpdate();
            }

            // Créer la relation inverse
            String sqlInsert = "INSERT INTO ami (joueur_pseudo, ami_pseudo, statut) VALUES (?, ?, 'ACCEPTE')";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                pstmt.setString(1, joueur);
                pstmt.setString(2, ami);
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
            System.err.println("❌ Erreur lors de l'acceptation de la demande d'ami : " + e.getMessage());
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
     * Compte le nombre total de joueurs
     */
    public int count() {
        String sql = "SELECT COUNT(*) as total FROM joueur";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des joueurs : " + e.getMessage());
        }

        return 0;
    }
}

