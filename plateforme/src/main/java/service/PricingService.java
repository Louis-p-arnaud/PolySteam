package service;

import model.Evaluation;
import model.JeuCatalogue;

import java.util.List;

/**
 * Service de gestion des prix dynamiques
 * Ajuste les prix en fonction de la qualité perçue (notes moyennes)
 */
public class PricingService {

    private static final double SEUIL_EXCELLENT = 8.0;
    private static final double SEUIL_BON = 6.5;
    private static final double SEUIL_MOYEN = 5.0;

    private static final double AUGMENTATION_EXCELLENT = 1.15; // +15%
    private static final double AUGMENTATION_BON = 1.05; // +5%
    private static final double REDUCTION_MOYEN = 0.90; // -10%
    private static final double REDUCTION_MAUVAIS = 0.75; // -25%

    /**
     * Recalcule le prix d'un jeu en fonction de sa note moyenne
     * @param jeu Le jeu dont le prix doit être recalculé
     */
    public void recalculerPrix(JeuCatalogue jeu) {
        double prixBase = jeu.getPrixEditeur();
        double noteMoyenne = calculerNoteMoyenne(jeu);

        if (noteMoyenne < 0) {
            // Pas d'évaluations, on garde le prix éditeur
            jeu.setPrixActuel(prixBase);
            return;
        }

        double nouveauPrix = prixBase;

        // Ajustement selon la qualité perçue
        if (noteMoyenne >= SEUIL_EXCELLENT) {
            nouveauPrix = prixBase * AUGMENTATION_EXCELLENT;
            System.out.println("📈 Prix ajusté (+15%) pour '" + jeu.getTitre() + "' (note: " +
                String.format("%.1f", noteMoyenne) + "/10)");
        } else if (noteMoyenne >= SEUIL_BON) {
            nouveauPrix = prixBase * AUGMENTATION_BON;
            System.out.println("📈 Prix ajusté (+5%) pour '" + jeu.getTitre() + "' (note: " +
                String.format("%.1f", noteMoyenne) + "/10)");
        } else if (noteMoyenne >= SEUIL_MOYEN) {
            nouveauPrix = prixBase * REDUCTION_MOYEN;
            System.out.println("📉 Prix réduit (-10%) pour '" + jeu.getTitre() + "' (note: " +
                String.format("%.1f", noteMoyenne) + "/10)");
        } else {
            nouveauPrix = prixBase * REDUCTION_MAUVAIS;
            System.out.println("📉 Prix fortement réduit (-25%) pour '" + jeu.getTitre() + "' (note: " +
                String.format("%.1f", noteMoyenne) + "/10)");
        }

        // Arrondir à 2 décimales
        nouveauPrix = Math.round(nouveauPrix * 100.0) / 100.0;
        jeu.setPrixActuel(nouveauPrix);
    }

    /**
     * Calcule la note moyenne d'un jeu
     * @param jeu Le jeu concerné
     * @return La note moyenne, ou -1 si aucune évaluation
     */
    private double calculerNoteMoyenne(JeuCatalogue jeu) {
        List<Evaluation> evaluations = jeu.getEvaluationsJoueurs();

        if (evaluations == null || evaluations.isEmpty()) {
            return -1.0;
        }

        double somme = 0;
        for (Evaluation eval : evaluations) {
            somme += eval.getNote();
        }

        return somme / evaluations.size();
    }

    /**
     * Obtient la note moyenne d'un jeu (méthode publique)
     */
    public double getNoteMoyenne(JeuCatalogue jeu) {
        return calculerNoteMoyenne(jeu);
    }
}