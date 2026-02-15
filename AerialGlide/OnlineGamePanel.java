package juegojava;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import online.Client;

public class OnlineGamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int ANCHO_OBSTACULO = 120;

    private final Client client;
    private final Timer timer;

    private final Personaje localPlayer;
    private final Personaje remotePlayer;
    private final ArrayList<Obstaculos> obstaculos;

    private int localScore;
    private int remoteScore;
    private int contadorTiempo;
    private boolean remoteAlive;
    private boolean sessionEnded;
    private final String[] players;

    private final JLabel estadoLabel;
    private final JButton volverMenuButton;

    public OnlineGamePanel(JFrame frame, Client client, String[] players) {
        this.frame = frame;
        this.client = client;
        this.players = players;
        this.localPlayer = new Personaje(180, 250);
        this.remotePlayer = new Personaje(500, 250);
        this.obstaculos = new ArrayList<>();
        this.remoteAlive = true;
        this.sessionEnded = false;
        this.fondo = ResourceLoader.loadImage("Resources/fondo juego.jpg");

        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        setLayout(new BorderLayout());

        this.estadoLabel = new JLabel("", SwingConstants.CENTER);
        estadoLabel.setFont(new Font("Arial", Font.BOLD, 36));
        estadoLabel.setForeground(Color.WHITE);
        estadoLabel.setVisible(false);

        this.volverMenuButton = new JButton("Volver al menú");
        volverMenuButton.setFont(new Font("Arial", Font.BOLD, 24));
        volverMenuButton.setVisible(false);
        volverMenuButton.addActionListener(e -> volverAlMenu());

        add(estadoLabel, BorderLayout.CENTER);
        add(volverMenuButton, BorderLayout.SOUTH);

        this.timer = new Timer(20, this);
        this.timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        localPlayer.update(getHeight());

        for (Obstaculos obstaculo : obstaculos) {
            obstaculo.mover(10);
        }
        obstaculos.removeIf(Obstaculos::fueraDePantalla);

        contadorTiempo++;
        if (contadorTiempo % 80 == 0) {
            generarObstaculos();
        }

        for (Obstaculos obstaculo : obstaculos) {
            if (localPlayer.getBounds().intersects(obstaculo.getBounds())) {
                timer.stop();
                client.sendState(localPlayer.getY(), localScore, false);
                repaint();
                return;
            }

            if (obstaculo.getY() > 0 && !obstaculo.isPuntuado()
                    && localPlayer.getBounds().x > obstaculo.getX() + obstaculo.getWidth()) {
                obstaculo.marcarPuntuado();
                localScore++;
            }
        }

        client.sendState(localPlayer.getY(), localScore, true);

        String message;
        while ((message = client.pollMessage()) != null) {
            processMessage(message);
        }

        repaint();
    }

    private void generarObstaculos() {
        int alturaVentana = getHeight();
        int espacioVertical = 210;
        int maxAlturaSuperior = Math.max(1, alturaVentana - espacioVertical - 120);
        int alturaObstaculoSuperior = (int) (Math.random() * maxAlturaSuperior) + 60;

        obstaculos.add(new Obstaculos(getWidth(), 0, ANCHO_OBSTACULO, alturaObstaculoSuperior,
                "Resources/obstacle - copia.png"));

        obstaculos.add(new Obstaculos(getWidth(), alturaObstaculoSuperior + espacioVertical, ANCHO_OBSTACULO,
                alturaVentana - alturaObstaculoSuperior - espacioVertical,
                "Resources/obstacle.png"));
    }

    private void processMessage(String message) {
        String[] parts = message.split("\\$");
        switch (parts[0]) {
            case "jump" -> {
                if (!sessionEnded) {
                    remotePlayer.jump();
                }
            }
            case "state" -> {
                if (!sessionEnded && parts.length >= 4) {
                    remotePlayer.setY(Integer.parseInt(parts[1]));
                    remoteScore = Integer.parseInt(parts[2]);
                    remoteAlive = Boolean.parseBoolean(parts[3]);
                }
            }
            case "disconnect", "waiting", "serverClosed" -> onRemoteDisconnected();
            default -> {
            }
        }
    }

    private void onRemoteDisconnected() {
        if (sessionEnded) {
            return;
        }
        sessionEnded = true;
        remoteAlive = false;
        timer.stop();

        estadoLabel.setText("El otro jugador se desconectó");
        estadoLabel.setVisible(true);
        volverMenuButton.setVisible(true);
    }

    private void volverAlMenu() {
        client.finish();
        frame.setContentPane(new MenuPanel(frame));
        frame.revalidate();
        frame.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (fondo != null) {
            g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(10, 20, 40));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        for (Obstaculos obstaculo : obstaculos) {
            obstaculo.dibujar(g);
        }

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

        if (!timer.isRunning()) {
            g.setColor(Color.RED);
            g.drawString("Perdiste", 20, 200);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && timer.isRunning()) {
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
        if (!sessionEnded) {
            client.sendDisconnect();
        }
        client.finish();
    }
}
