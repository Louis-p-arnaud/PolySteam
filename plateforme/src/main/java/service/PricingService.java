package service;

import model.JeuCatalogue;

public class PricingService {

    public void recalculerPrix(JeuCatalogue jeu) {
        double nouveauPrix = jeu.getPrixEditeur();

        // Exemple de règle métier inspirée du document :
        // Si beaucoup d'évaluations positives -> Augmentation ou Maintien
        // Si demande faible -> Baisse

        // Logique à implémenter ici...

        jeu.setPrixActuel(nouveauPrix);
    }
}