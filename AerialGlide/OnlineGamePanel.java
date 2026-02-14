package juegojava;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JPanel;
import javax.swing.Timer;
import online.Client;

public class OnlineGamePanel extends JPanel implements ActionListener, KeyListener {
    private final Client client;
    private final Timer timer;

    private final Personaje localPlayer;
    private final Personaje remotePlayer;

    private int localScore;
    private int remoteScore;
    private boolean remoteAlive;
    private final String[] players;

    public OnlineGamePanel(Client client, String[] players) {
        this.client = client;
        this.players = players;
        this.localPlayer = new Personaje(180, 250);
        this.remotePlayer = new Personaje(500, 250);
        this.remoteAlive = true;

        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        this.timer = new Timer(20, this);
        this.timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        localPlayer.update(getHeight());
        localScore++;
        client.sendState(localPlayer.getY(), localScore, true);

        String message;
        while ((message = client.pollMessage()) != null) {
            processMessage(message);
        }

        repaint();
    }

    private void processMessage(String message) {
        String[] parts = message.split("\\$");
        switch (parts[0]) {
            case "jump" -> remotePlayer.jump();
            case "state" -> {
                if (parts.length >= 4) {
                    remotePlayer.setY(Integer.parseInt(parts[1]));
                    remoteScore = Integer.parseInt(parts[2]);
                    remoteAlive = Boolean.parseBoolean(parts[3]);
                }
            }
            case "disconnect" -> remoteAlive = false;
            default -> {
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(10, 20, 40));
        g.fillRect(0, 0, getWidth(), getHeight());

        localPlayer.draw(g);
        remotePlayer.draw(g);

        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        g.drawString(players[0] + ": " + localScore, 20, 40);
        g.drawString(players[1] + ": " + remoteScore, 20, 80);
        g.drawString("Espacio = saltar", 20, 120);

        if (!remoteAlive) {
            g.setColor(Color.YELLOW);
            g.drawString("Tu rival se desconectó", 20, 160);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            localPlayer.jump();
            client.sendJump();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void removeNotify() {
        super.removeNotify();
        timer.stop();
        client.sendDisconnect();
        client.finish();
    }
}
