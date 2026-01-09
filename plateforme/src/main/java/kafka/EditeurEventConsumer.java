package kafka;

import service.CatalogueService;

public class EditeurEventConsumer {

    private final CatalogueService catalogueService;

    public EditeurEventConsumer(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    public void demarrerEcoute() {
        // Configuration Kafka Consumer
        // Subscribe au topic "editeur-events"

        while (true) {
            // Consumer.poll()...
            // Pour chaque enregistrement :
            // Si type = CREATION_JEU -> catalogueService.referencerNouveauJeu(...)
            // Si type = PATCH -> catalogueService.appliquerPatch(...)
        }
    }
}