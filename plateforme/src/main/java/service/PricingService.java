package service;

import dao.EvaluationDAO;
import dao.JeuCatalogueDAO;
import kafka.ModificationPrixEventProducer;
import model.JeuCatalogue;

/**
 * Service de gestion des prix dynamiques
 * Ajuste les prix en fonction de la qualité perçue (notes moyennes)
 * Publie des événements Kafka lors des modifications de prix
 */
public class PricingService {

    private static final double SEUIL_EXCELLENT = 8.0;
    private static final double SEUIL_BON = 6.5;
    private static final double SEUIL_MOYEN = 5.0;

    private static final double AUGMENTATION_EXCELLENT = 1.15; // +15%
    private static final double AUGMENTATION_BON = 1.05; // +5%
    private static final double REDUCTION_MOYEN = 0.90; // -10%
    private static final double REDUCTION_MAUVAIS = 0.75; // -25%

    private final EvaluationDAO evaluationDAO;
    private final JeuCatalogueDAO jeuDAO;
    private final ModificationPrixEventProducer prixProducer;

    /**
     * Constructeur avec injection des DAOs et du producer Kafka
     */
    public PricingService(EvaluationDAO evaluationDAO, JeuCatalogueDAO jeuDAO) {
        this.evaluationDAO = evaluationDAO;
        this.jeuDAO = jeuDAO;

        // Initialiser le producer Kafka pour les modifications de prix
        try {
            this.prixProducer = new ModificationPrixEventProducer();
        } catch (Exception e) {
            System.err.println("⚠️  [PRICING] Impossible d'initialiser le producer Kafka : " + e.getMessage());
            throw e; // Remonter l'exception si critique
        }
    }

    /**
     * Recalcule le prix d'un jeu en fonction de sa note moyenne (depuis la BDD)
     * et met à jour le prix en base de données
     * Publie un événement Kafka pour notifier la modification
     *
     * @param jeuId L'ID du jeu dont le prix doit être recalculé
     * @param titreJeu Le titre du jeu (pour l'affichage)
     * @param editeurId L'ID de l'éditeur du jeu
     * @param prixBase Le prix éditeur de base
     * @param ancienPrix Le prix actuel avant recalcul
     * @param plateforme La plateforme concernée
     * @return true si le prix a été mis à jour avec succès, false sinon
     */
    public boolean recalculerPrixDepuisBDD(String jeuId, String titreJeu, String editeurId,
                                           double prixBase, double ancienPrix, String plateforme) {
        // Récupérer la note moyenne et le nombre d'évaluations depuis la BDD
        double noteMoyenne = evaluationDAO.getNoteMoyenne(jeuId);
        int nombreEvaluations = evaluationDAO.countByJeuId(jeuId);

        double nouveauPrix;

        if (noteMoyenne < 0) {
            // Pas d'évaluations, on garde le prix éditeur
            nouveauPrix = prixBase;
            System.out.println("  ℹ️  Aucune évaluation pour '" + titreJeu + "', prix maintenu à " + prixBase + "€");
        } else {
            // Ajustement selon la qualité perçue
            if (noteMoyenne >= SEUIL_EXCELLENT) {
                nouveauPrix = prixBase * AUGMENTATION_EXCELLENT;
                System.out.println("  💰 [PRICING] Prix ajusté (+15%) pour '" + titreJeu + "' (note: " +
                        String.format("%.1f", noteMoyenne) + "/10) → " + String.format("%.2f", nouveauPrix) + "€");
            } else if (noteMoyenne >= SEUIL_BON) {
                nouveauPrix = prixBase * AUGMENTATION_BON;
                System.out.println("  💰 [PRICING] Prix ajusté (+5%) pour '" + titreJeu + "' (note: " +
                        String.format("%.1f", noteMoyenne) + "/10) → " + String.format("%.2f", nouveauPrix) + "€");
            } else if (noteMoyenne >= SEUIL_MOYEN) {
                nouveauPrix = prixBase * REDUCTION_MOYEN;
                System.out.println("  💰 [PRICING] Prix réduit (-10%) pour '" + titreJeu + "' (note: " +
                        String.format("%.1f", noteMoyenne) + "/10) → " + String.format("%.2f", nouveauPrix) + "€");
            } else {
                nouveauPrix = prixBase * REDUCTION_MAUVAIS;
                System.out.println("  💰 [PRICING] Prix fortement réduit (-25%) pour '" + titreJeu + "' (note: " +
                        String.format("%.1f", noteMoyenne) + "/10) → " + String.format("%.2f", nouveauPrix) + "€");
            }
        }

        // Arrondir à 2 décimales
        nouveauPrix = Math.round(nouveauPrix * 100.0) / 100.0;

        // Vérifier si le prix a vraiment changé
        if (Math.abs(nouveauPrix - ancienPrix) < 0.01) {
            System.out.println("  ℹ️  [PRICING] Prix inchangé (" + String.format("%.2f€", nouveauPrix) + ")");
            return true; // Pas d'erreur, mais pas de changement non plus
        }

        // Mettre à jour le prix en base de données
        boolean updateSuccess = jeuDAO.updatePrix(jeuId, nouveauPrix);

        if (updateSuccess) {
            // PUBLIER L'ÉVÉNEMENT KAFKA
            try {
                prixProducer.publierModificationPrix(
                    jeuId,
                    titreJeu,
                    editeurId,
                    prixBase,
                    ancienPrix,
                    nouveauPrix,
                    noteMoyenne,
                    nombreEvaluations,
                    plateforme
                );
            } catch (Exception e) {
                System.err.println("  ⚠️  [PRICING] Erreur lors de la publication Kafka (prix mis à jour en BDD) : " + e.getMessage());

            }
        }

        return updateSuccess;
    }

    /**
     * Ferme proprement le producer Kafka
     */
    public void close() {
        if (prixProducer != null) {
            prixProducer.close();
        }
    }
}

