package juegojava;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import online.Client;

public class OnlineGamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int ANCHO_OBSTACULO = 120;

    private final JFrame frame;
    private final Client client;
    private final Timer timer;
    private final Image fondo;

    private Personaje localPlayer;
    private Personaje remotePlayer;
    private final ArrayList<Obstaculos> obstaculos;

    private int localScore;
    private int remoteScore;
    private int contadorTiempo;

    private final String[] players;
    private final boolean hostAutoritativo;

    private boolean localAlive;
    private boolean remoteAlive;
    private boolean roundOver;
    private boolean localDefeatPending;
    private long deadlineResolverDerrotaMs;

    private final JPanel pausaPanel;
    private final JPanel opcionesPanel;
    private final JLabel estadoRonda;
    private final JLabel esperandoRevanchaLabel;

    private boolean localRematchRequested;
    private boolean remoteRematchRequested;
    private int volumenPorcentaje = 70;

    public OnlineGamePanel(JFrame frame, Client client, String[] players, boolean hostAutoritativo) {
        this.frame = frame;
        this.client = client;
        this.players = players;
        this.hostAutoritativo = hostAutoritativo;
        this.fondo = ResourceLoader.loadImage("Resources/fondo juego.jpg");

        this.obstaculos = new ArrayList<>();
        this.timer = new Timer(20, this);

        setFocusable(true);
        addKeyListener(this);
        setLayout(null);

        this.estadoRonda = new JLabel("", SwingConstants.CENTER);
        estadoRonda.setBounds(0, 120, 1600, 80);
        estadoRonda.setFont(new Font("Arial", Font.BOLD, 64));
        estadoRonda.setForeground(Color.WHITE);
        estadoRonda.setVisible(false);

        this.esperandoRevanchaLabel = new JLabel("Esperando respuesta...", SwingConstants.CENTER);
        esperandoRevanchaLabel.setBounds(0, 220, 1600, 80);
        esperandoRevanchaLabel.setFont(new Font("Arial", Font.BOLD, 46));
        esperandoRevanchaLabel.setForeground(new Color(255, 240, 120));
        esperandoRevanchaLabel.setVisible(false);

        this.pausaPanel = crearPanelPausa();
        this.opcionesPanel = crearPanelOpciones();
        pausaPanel.setBounds(520, 220, 560, 500);
        opcionesPanel.setBounds(520, 220, 560, 380);

        add(estadoRonda);
        add(esperandoRevanchaLabel);
        add(pausaPanel);
        add(opcionesPanel);

        reiniciarRonda();
    }

    private JPanel crearPanelPausa() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JButton volverButton = crearBotonConImagen("VOLVER", "Resources/imgvolver.png", 500, 120);
        JButton opcionesButton = crearBotonConImagen("OPCIONES", "Resources/option_button.png", 500, 120);
        JButton revanchaButton = crearBotonConImagen("REVANCHA", "Resources/imgreiniciar.png", 500, 120);

        volverButton.addActionListener(e -> volverAlMenuPrincipal());
        opcionesButton.addActionListener(e -> {
            if (roundOver) {
                mostrarSoloPanel(opcionesPanel);
            }
        });
        revanchaButton.addActionListener(e -> pedirRevancha());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(12, 12, 12, 12);
        panel.add(volverButton, gbc);

        gbc.gridy = 1;
        panel.add(opcionesButton, gbc);

        gbc.gridy = 2;
        panel.add(revanchaButton, gbc);

        panel.setVisible(false);
        return panel;
    }

    private JPanel crearPanelOpciones() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel marcoOpciones = new JPanel(new GridBagLayout());
        marcoOpciones.setOpaque(false);
        marcoOpciones.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 210), 3),
                BorderFactory.createEmptyBorder(18, 28, 18, 28)));

        JLabel volumenLabel = new JLabel("Volumen");
        volumenLabel.setForeground(new Color(205, 240, 255));
        volumenLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JSlider volumenSlider = new JSlider(0, 100, volumenPorcentaje);
        volumenSlider.setOpaque(false);
        volumenSlider.setMajorTickSpacing(25);
        volumenSlider.setPaintTicks(false);
        volumenSlider.setPaintLabels(false);
        volumenSlider.setForeground(Color.WHITE);
        volumenSlider.addChangeListener(e -> volumenPorcentaje = volumenSlider.getValue());

        JButton volverButton = crearBotonConImagen("VOLVER", "Resources/imgvolver.png", 420, 100);
        volverButton.addActionListener(e -> {
            if (roundOver) {
                mostrarSoloPanel(pausaPanel);
            }
        });

        GridBagConstraints marcoGbc = new GridBagConstraints();
        marcoGbc.gridx = 0;
        marcoGbc.gridy = 0;
        marcoGbc.insets = new Insets(8, 8, 8, 8);
        marcoGbc.anchor = GridBagConstraints.CENTER;
        marcoOpciones.add(volumenLabel, marcoGbc);

        marcoGbc.gridy = 1;
        marcoGbc.fill = GridBagConstraints.HORIZONTAL;
        marcoGbc.weightx = 1.0;
        marcoOpciones.add(volumenSlider, marcoGbc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(12, 12, 12, 12);
        panel.add(marcoOpciones, gbc);

        gbc.gridy = 1;
        panel.add(volverButton, gbc);

        panel.setVisible(false);
        return panel;
    }

    private JButton crearBotonConImagen(String texto, String rutaImagen, int ancho, int alto) {
        JButton boton = new JButton(texto);
        Image image = rutaImagen == null ? null : ResourceLoader.loadImage(rutaImagen);

        if (image != null) {
            boton.setText("");
            boton.setIcon(new ImageIcon(image.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH)));
        }

        boton.setPreferredSize(new Dimension(ancho, alto));
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder());
        return boton;
    }

    private void mostrarSoloPanel(JPanel panelVisible) {
        pausaPanel.setVisible(panelVisible == pausaPanel);
        opcionesPanel.setVisible(panelVisible == opcionesPanel);
        requestFocusInWindow();
        repaint();
    }

    private void volverAlMenuPrincipal() {
        timer.stop();
        client.sendDisconnect();
        client.finish();
        frame.setContentPane(new MenuPanel(frame));
        frame.revalidate();
        frame.repaint();
    }

    private void pedirRevancha() {
        if (!roundOver || localRematchRequested) {
            return;
        }

        localRematchRequested = true;
        client.sendRematchRequest();
        esperandoRevanchaLabel.setText("Esperando respuesta...");
        esperandoRevanchaLabel.setVisible(true);

        if (remoteRematchRequested) {
            reiniciarRonda();
        }
        requestFocusInWindow();
        repaint();
    }

    private void reiniciarRonda() {
        obstaculos.clear();
        String skinHost = "Resources/bird.png";
        String skinCliente = "Resources/bird2.png";
        if (hostAutoritativo) {
            localPlayer = new Personaje(180, 250, skinHost);
            remotePlayer = new Personaje(500, 250, skinCliente);
        } else {
            localPlayer = new Personaje(180, 250, skinCliente);
            remotePlayer = new Personaje(500, 250, skinHost);
        }

        localScore = 0;
        remoteScore = 0;
        contadorTiempo = 0;

        localAlive = true;
        remoteAlive = true;
        roundOver = false;
        localDefeatPending = false;

        localRematchRequested = false;
        remoteRematchRequested = false;

        estadoRonda.setVisible(false);
        esperandoRevanchaLabel.setVisible(false);
        mostrarSoloPanel(null);

        if (!timer.isRunning()) {
            timer.start();
        }

        requestFocusInWindow();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String message;
        while ((message = client.pollMessage()) != null) {
            processMessage(message);
        }

        if (roundOver || getWidth() <= 0 || getHeight() <= 0) {
            repaint();
            return;
        }

        if (localAlive) {
            localPlayer.update(getHeight());
        }

        for (Obstaculos obstaculo : obstaculos) {
            obstaculo.mover(10);
        }
        obstaculos.removeIf(Obstaculos::fueraDePantalla);

        contadorTiempo++;
        if (hostAutoritativo && contadorTiempo % 80 == 0) {
            int alturaObstaculoSuperior = calcularAlturaObstaculoSuperior();
            generarObstaculos(alturaObstaculoSuperior);
            client.sendObstacleSpawn(alturaObstaculoSuperior);
        }

        if (localAlive) {
            for (Obstaculos obstaculo : obstaculos) {
                if (localPlayer.getBounds().intersects(obstaculo.getBounds())) {
                    localAlive = false;
                    localDefeatPending = true;
                    deadlineResolverDerrotaMs = System.currentTimeMillis() + 350;
                    client.sendState(localPlayer.getY(), getHeight(), localScore, false);
                    break;
                }

                if (obstaculo.getY() > 0 && !obstaculo.isPuntuado()
                        && localPlayer.getBounds().x > obstaculo.getX() + obstaculo.getWidth()) {
                    obstaculo.marcarPuntuado();
                    localScore++;
                }
            }
        }

        if (localDefeatPending && !roundOver && System.currentTimeMillis() >= deadlineResolverDerrotaMs) {
            finalizarRonda(remoteAlive ? "Perdiste" : "Empate");
        }

        client.sendState(localPlayer.getY(), getHeight(), localScore, localAlive);
        repaint();
    }

    private int calcularAlturaObstaculoSuperior() {
        int alturaVentana = getHeight();
        int espacioVertical = 210;
        int maxAlturaSuperior = Math.max(1, alturaVentana - espacioVertical - 120);
        return (int) (Math.random() * maxAlturaSuperior) + 60;
    }

    private void generarObstaculos(int alturaObstaculoSuperior) {
        int alturaVentana = getHeight();
        int espacioVertical = 210;
        int alturaInferior = Math.max(1, alturaVentana - alturaObstaculoSuperior - espacioVertical);

        obstaculos.add(new Obstaculos(getWidth(), 0, ANCHO_OBSTACULO, alturaObstaculoSuperior,
                "Resources/obstacle - copia.png"));

        obstaculos.add(new Obstaculos(getWidth(), alturaObstaculoSuperior + espacioVertical, ANCHO_OBSTACULO,
                alturaInferior, "Resources/obstacle.png"));
    }

    private void finalizarRonda(String estado) {
        if (roundOver) {
            return;
        }
        roundOver = true;
        timer.stop();
        estadoRonda.setText(estado);
        estadoRonda.setVisible(true);
        mostrarSoloPanel(pausaPanel);
    }

    private void processMessage(String message) {
        String[] parts = message.split("\\$");
        switch (parts[0]) {
            case "jump" -> {
                if (!roundOver && remoteAlive) {
                    remotePlayer.jump();
                }
            }
            case "state" -> {
                if (parts.length >= 5) {
                    int yRecibida = Integer.parseInt(parts[1]);
                    int altoPanelRemoto = Integer.parseInt(parts[2]);
                    remoteScore = Integer.parseInt(parts[3]);
                    remoteAlive = Boolean.parseBoolean(parts[4]);

                    int altoLocal = Math.max(1, getHeight());
                    int yEscalada = altoPanelRemoto <= 0 ? yRecibida : (int) Math.round((yRecibida / (double) altoPanelRemoto) * altoLocal);
                    remotePlayer.setY(yEscalada);

                    if (!remoteAlive && !roundOver) {
                        if (!localAlive || localDefeatPending) {
                            finalizarRonda("Empate");
                        } else {
                            finalizarRonda("Ganaste");
                        }
                    }
                }
            }
            case "spawn" -> {
                if (!hostAutoritativo && parts.length >= 2 && !roundOver) {
                    generarObstaculos(Integer.parseInt(parts[1]));
                }
            }
            case "rematch_request" -> {
                remoteRematchRequested = true;
                if (roundOver) {
                    esperandoRevanchaLabel.setText(localRematchRequested
                            ? "Esperando respuesta..."
                            : "Tu rival pidió revancha");
                    esperandoRevanchaLabel.setVisible(true);
                }
                if (localRematchRequested && remoteRematchRequested) {
                    reiniciarRonda();
                }
            }
            case "disconnect" -> {
                remoteAlive = false;
                if (!roundOver) {
                    finalizarRonda("Tu rival se desconectó");
                }
            }
            default -> {
            }
        }
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
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !roundOver && timer.isRunning()) {
            localPlayer.jump();
            client.sendJump();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        timer.stop();
    }
}
