package service;

public class CatalogueService {

    // Appelé quand on reçoit un événement "Nouveau Jeu" via Kafka
    public void referencerNouveauJeu(String titre, String editeur, double prixInitial) {
        // Créer un JeuCatalogue et le sauvegarder en BDD
    }

    // Appelé quand on reçoit un événement "Patch" via Kafka
    public void appliquerPatch(String titreJeu, String version, String typeModif) {
        // Mettre à jour la version du jeu dans le catalogue
        // Notifier les joueurs qui possèdent ce jeu (optionnel mais conseillé)
    }
}