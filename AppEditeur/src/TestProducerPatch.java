import java.util.Arrays;

public class TestProducerPatch {
    public static void main(String[] args) {
        kafka.PublicationPatchEventProducer producer = new kafka.PublicationPatchEventProducer();
        try {
            producer.publierPublicationPatch(
                    "editeur-test",
                    "JeuTest",
                    42,
                    "Correctifs mineurs",
                    "1.0.1",
                    Arrays.asList("Correction bug A", "Amélioration performance B")
            );
        } finally {
            producer.close();
        }
    }
}
