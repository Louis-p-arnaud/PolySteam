import model.Editeur;
import model.Enums;
import model.Jeu;
import model.Patch;

import java.util.*;

public class Main {

    private static Scanner scanner = new Scanner(System.in);
    private static List<Editeur> editeurs = new ArrayList<>();
    private static int compteurPatch = 1;

    public static void main(String[] args) {

        // Editeurs de base
        editeurs.add(new Editeur("Ubisoft", Enums.TYPE_EDITEUR.ENTREPRISE));
        editeurs.add(new Editeur("Valve", Enums.TYPE_EDITEUR.ENTREPRISE));
        editeurs.add(new Editeur("Studio Indie", Enums.TYPE_EDITEUR.INDEPENDANT));

        char choix;

        do {
            afficherMenu();
            choix = scanner.nextLine().charAt(0);

            switch (choix) {
                case '1':
                    publierJeu();
                    break;
                case '2':
                    publierPatch();
                    break;
                case 'q':
                    System.out.println("Au revoir !");
                    break;
                default:
                    System.out.println("Choix incorrect.");
            }

        } while (choix != 'q');
    }

    private static void afficherMenu() {
        System.out.println("\n=== MENU PRINCIPAL ===");
        System.out.println("1. Publier un jeu");
        System.out.println("2. Publier un patch");
        System.out.println("q. Quitter");
        System.out.print("Votre choix : ");
    }

    // -----------------------------
    // 1. PUBLIER UN JEU
    // -----------------------------
    private static void publierJeu() {

        Editeur editeur = choisirEditeur();
        if (editeur == null) return;

        System.out.print("Nom du jeu : ");
        String nom = scanner.nextLine();

        Enums.PLATEFORME_EXECUTION plateforme = choisirPlateforme();

        List<Enums.GENRE> genres = choisirGenres();

        System.out.print("Numéro de version : ");
        String version = scanner.nextLine();

        System.out.print("Version anticipée ? (true/false) : ");
        boolean anticipe = Boolean.parseBoolean(scanner.nextLine());

        Jeu jeu = new Jeu(nom, plateforme, genres, version, anticipe);
        editeur.publierJeu(jeu);
    }

    // -----------------------------
    // 2. PUBLIER UN PATCH
    // -----------------------------
    private static void publierPatch() {

        Editeur editeur = choisirEditeur();
        if (editeur == null) return;

        if (editeur.getJeuxPublies().isEmpty()) {
            System.out.println("Cet éditeur n'a publié aucun jeu.");
            return;
        }

        Jeu jeu = choisirJeu(editeur);

        System.out.print("Commentaire éditeur : ");
        String commentaire = scanner.nextLine();

        System.out.print("Nouvelle version : ");
        String nouvelleVersion = scanner.nextLine();

        List<String> modifications = new ArrayList<>();
        System.out.println("Entrez les modifications (tapez 'fin' pour terminer) :");

        while (true) {
            String modif = scanner.nextLine();
            if (modif.equalsIgnoreCase("fin")) break;
            modifications.add(modif);
        }

        Patch patch = new Patch(jeu, compteurPatch++, commentaire, nouvelleVersion, modifications);
        editeur.publierPatch(patch);
    }

    // -----------------------------
    // OUTILS DE SÉLECTION
    // -----------------------------

    private static Editeur choisirEditeur() {
        System.out.println("\nChoisissez un éditeur :");
        for (int i = 0; i < editeurs.size(); i++) {
            System.out.println((i + 1) + ". " + editeurs.get(i).getNom());
        }

        System.out.print("Numéro : ");
        int choix = Integer.parseInt(scanner.nextLine()) - 1;

        if (choix < 0 || choix >= editeurs.size()) {
            System.out.println("Choix invalide.");
            return null;
        }

        return editeurs.get(choix);
    }

    private static Jeu choisirJeu(Editeur editeur) {
        System.out.println("\nChoisissez un jeu :");
        List<Jeu> jeux = editeur.getJeuxPublies();

        for (int i = 0; i < jeux.size(); i++) {
            System.out.println((i + 1) + ". " + jeux.get(i).getNom());
        }

        System.out.print("Numéro : ");
        int choix = Integer.parseInt(scanner.nextLine()) - 1;

        return jeux.get(choix);
    }

    private static Enums.PLATEFORME_EXECUTION choisirPlateforme() {
        System.out.println("\nPlateforme :");
        Enums.PLATEFORME_EXECUTION[] plateformes = Enums.PLATEFORME_EXECUTION.values();

        for (int i = 0; i < plateformes.length; i++) {
            System.out.println((i + 1) + ". " + plateformes[i]);
        }

        int choix = Integer.parseInt(scanner.nextLine()) - 1;
        return plateformes[choix];
    }

    private static List<Enums.GENRE> choisirGenres() {
        List<Enums.GENRE> genres = new ArrayList<>();

        System.out.println("\nGenres (entrez plusieurs numéros séparés par des espaces) :");
        Enums.GENRE[] tousGenres = Enums.GENRE.values();

        for (int i = 0; i < tousGenres.length; i++) {
            System.out.println((i + 1) + ". " + tousGenres[i]);
        }

        String[] choix = scanner.nextLine().split(" ");

        for (String c : choix) {
            int index = Integer.parseInt(c) - 1;
            if (index >= 0 && index < tousGenres.length) {
                genres.add(tousGenres[index]);
            }
        }

        return genres;
    }
}
