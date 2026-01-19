import java.util.List;

public class Jeu {
    private String nom;
    private Enums.PLATEFORME_EXECUTION plateformeExecution;
    private List<Enums.GENRE> genres;
    private String numeroVersion;
    private boolean versionAnticipe;

    public Jeu(String nom, Enums.PLATEFORME_EXECUTION plateformeExecution,
               List<Enums.GENRE> genres, String numeroVersion, boolean versionAnticipe) {
        this.nom = nom;
        this.plateformeExecution = plateformeExecution;
        this.genres = genres;
        this.numeroVersion = numeroVersion;
        this.versionAnticipe = versionAnticipe;
    }

    // Getters
    public String getNom() {
        return nom;
    }

    public Enums.PLATEFORME_EXECUTION getPlateformeExecution() {
        return plateformeExecution;
    }

    public List<Enums.GENRE> getGenres() {
        return genres;
    }

    public String getNumeroVersion() {
        return numeroVersion;
    }

    public boolean isVersionAnticipe() {
        return versionAnticipe;
    }
}
