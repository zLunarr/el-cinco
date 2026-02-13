package juegojava;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import online.Client;
import online.ClientListener;
import online.NetManager;

public class MultiplayerLobbyPanel extends JPanel implements NetManager, ClientListener {
    private final JFrame frame;
    private final Client client;
    private final JLabel estado;
    private String[] players = {"Jugador 1", "Jugador 2"};

    public MultiplayerLobbyPanel(JFrame frame, String serverIp, String username) {
        this.frame = frame;
        this.estado = new JLabel("Conectando al servidor...", SwingConstants.CENTER);
        this.estado.setFont(new Font("Arial", Font.BOLD, 36));
        setLayout(new BorderLayout());
        add(estado, BorderLayout.CENTER);

        this.client = new Client(serverIp, 5555, this, this);
        this.client.start();
        this.client.connect(username);
    }

    @Override
    public void connect(boolean connected) {
        SwingUtilities.invokeLater(() -> estado.setText(connected ? "Conectado. Esperando rival..." : "No se pudo conectar"));
    }

    @Override
    public void timeOutEnded() {
        SwingUtilities.invokeLater(() -> estado.setText("Sin respuesta del servidor (timeout)."));
    }

    @Override
    public void onMessage(String message) {
        String[] parts = message.split("\\$");
        switch (parts[0]) {
            case "connected" -> SwingUtilities.invokeLater(() -> estado.setText("Conectado como jugador " + parts[1] + ". Esperando rival..."));
            case "waiting" -> SwingUtilities.invokeLater(() -> estado.setText("Sala de espera: falta 1 jugador..."));
            case "start" -> {
                if (parts.length >= 3) {
                    players = new String[]{parts[1], parts[2]};
                }
                SwingUtilities.invokeLater(this::openGame);
            }
            case "full" -> SwingUtilities.invokeLater(() -> estado.setText("La sala ya está llena."));
            default -> {
            }
        }
    }

    private void openGame() {
        OnlineGamePanel panel = new OnlineGamePanel(client, players);
        client.setListener(panel);
        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
        panel.requestFocusInWindow();
    }
}
