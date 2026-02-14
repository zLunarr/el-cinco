package juegojava;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import online.Client;

public class MultiplayerLobbyPanel extends JPanel {
    private final JFrame frame;
    private final Client client;
    private final JLabel estado;
    private final Timer pollTimer;
    private String[] players = {"Jugador 1", "Jugador 2"};

    public MultiplayerLobbyPanel(JFrame frame, String serverIp, String username) {
        this.frame = frame;
        this.estado = new JLabel("Conectando al servidor...", SwingConstants.CENTER);
        this.estado.setFont(new Font("Arial", Font.BOLD, 36));
        setLayout(new BorderLayout());
        add(estado, BorderLayout.CENTER);

        this.client = new Client(serverIp, 5555);
        this.client.start();
        this.client.connect(username);

        this.pollTimer = new Timer(120, e -> readMessages());
        this.pollTimer.start();
    }

    private void readMessages() {
        String message;
        while ((message = client.pollMessage()) != null) {
            processMessage(message);
        }
    }

    private void processMessage(String message) {
        String[] parts = message.split("\\$");
        switch (parts[0]) {
            case "connected" -> estado.setText("Conectado como jugador " + parts[1] + ". Esperando rival...");
            case "waiting" -> estado.setText("Sala de espera: falta 1 jugador...");
            case "start" -> {
                if (parts.length >= 3) {
                    players = new String[]{parts[1], parts[2]};
                }
                openGame();
            }
            case "full" -> estado.setText("La sala ya está llena.");
            default -> {
            }
        }
    }

    private void openGame() {
        pollTimer.stop();
        OnlineGamePanel panel = new OnlineGamePanel(client, players);
        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
        panel.requestFocusInWindow();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        pollTimer.stop();
    }
}
