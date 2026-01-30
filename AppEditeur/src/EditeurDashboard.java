package ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EditeurDashboard {
    private static JTextArea logArea;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void createAndShow() {
        JFrame frame = new JFrame("🛠️ Dashboard Éditeur - Alertes Stabilité");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Empêche de fermer le dashboard par erreur
        frame.setSize(600, 400);

        logArea = new JTextArea();
        logArea.setBackground(new Color(20, 20, 20)); // Fond sombre
        logArea.setForeground(new Color(50, 255, 50)); // Texte vert "Matrix"
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(logArea);
        frame.add(scrollPane);

        // Positionner la fenêtre à droite de l'écran pour ne pas gêner l'IDE/Console
        GraphicsConfiguration config = frame.getGraphicsConfiguration();
        Rectangle bounds = config.getBounds();
        frame.setLocation(bounds.width - 620, 50);

        frame.setVisible(true);
        log("Système de monitoring activé. En attente de flux Kafka...");
    }

    // Dans ui.EditeurDashboard.java
    public static void log(String message) {
        if (logArea != null) {
            SwingUtilities.invokeLater(() -> {
                if (message.contains("URGENT")) {
                    logArea.append("----------------------------------------------------------\n");
                }
                logArea.append("[" + dtf.format(LocalTime.now()) + "] " + message + "\n");
                if (message.contains("URGENT")) {
                    logArea.append("----------------------------------------------------------\n");
                }
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }
    }
}