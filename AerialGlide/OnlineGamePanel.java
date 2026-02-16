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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import online.Client;

public class OnlineGamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int ANCHO_OBSTACULO = 120;
    private static final int PLAYER_SPAWN_X = 220;
    private static final int PLAYER_SPAWN_Y = 250;

    private final JFrame frame;
    private final Client client;
    private final Timer timer;
    private final Image fondo;

    private Personaje localPlayer;
    private Personaje remotePlayer;
    private final ArrayList<Obstaculos> obstaculos;

    private int localScore;
    private int remoteScore;

    private final String[] players;
    private final boolean hostAutoritativo;
    private boolean roundOver;

    private final JPanel pausaPanel;
    private final JPanel opcionesPanel;
    private final JLabel estadoRonda;
    private JCheckBox sonidoCheckBox;
    private JButton reiniciarButton;
    private final JLabel estadoReady;
    private boolean localReady;
    private boolean remoteReady;

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
        estadoRonda.setFont(new Font("Arial", Font.BOLD, 64));
        estadoRonda.setForeground(Color.WHITE);
        estadoRonda.setVisible(false);

        this.estadoReady = new JLabel("", SwingConstants.CENTER);
        estadoReady.setFont(new Font("Arial", Font.BOLD, 38));
        estadoReady.setForeground(Color.WHITE);
        estadoReady.setVisible(false);

        this.pausaPanel = crearPanelPausa();
        this.opcionesPanel = crearPanelOpciones();

        AudioManager.playMainLoop();

        add(estadoRonda);
        add(estadoReady);
        add(pausaPanel);
        add(opcionesPanel);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                actualizarLayoutMenus();
            }
        });

        reiniciarEstadoLocal();
        actualizarLayoutMenus();
    }

    private JPanel crearPanelPausa() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JButton volverButton = crearBotonConImagen("VOLVER", "Resources/imgvolver.png", 500, 120);
        JButton opcionesButton = crearBotonConImagen("OPCIONES", "Resources/option_button.png", 500, 120);
        reiniciarButton = crearBotonConImagen("REINICIAR", "Resources/imgreiniciar.png", 500, 120);

        volverButton.addActionListener(e -> volverAlMenuPrincipal());
        opcionesButton.addActionListener(e -> {
            if (roundOver) {
                mostrarSoloPanel(opcionesPanel);
            }
        });
        reiniciarButton.addActionListener(e -> marcarListoParaReiniciar());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(12, 12, 12, 12);
        panel.add(volverButton, gbc);

        gbc.gridy = 1;
        panel.add(opcionesButton, gbc);

        gbc.gridy = 2;
        panel.add(reiniciarButton, gbc);

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

        sonidoCheckBox = new JCheckBox("Activar sonido");
        sonidoCheckBox.setOpaque(false);
        sonidoCheckBox.setContentAreaFilled(false);
        sonidoCheckBox.setBorderPainted(false);
        sonidoCheckBox.setForeground(Color.WHITE);
        sonidoCheckBox.setFont(new Font("Arial", Font.BOLD, 28));
        sonidoCheckBox.setSelected(AudioManager.isSoundEnabled());
        sonidoCheckBox.addActionListener(e -> cambiarSonido(sonidoCheckBox.isSelected()));

        JLabel volumenLabel = new JLabel("Volumen");
        volumenLabel.setForeground(new Color(205, 240, 255));
        volumenLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JSlider volumenSlider = new JSlider(0, 100, AudioManager.getVolumePercent());
        volumenSlider.setOpaque(false);
        volumenSlider.setMajorTickSpacing(25);
        volumenSlider.setPaintTicks(false);
        volumenSlider.setPaintLabels(false);
        volumenSlider.setForeground(Color.WHITE);
        volumenSlider.addChangeListener(e -> AudioManager.setVolumePercent(volumenSlider.getValue()));

        JButton volverButton = crearBotonConImagen("VOLVER", "Resources/imgvolver.png", 420, 100);
        volverButton.addActionListener(e -> {
            if (roundOver) {
                mostrarSoloPanel(pausaPanel);
            }
        });

        GridBagConstraints marcoGbc = new GridBagConstraints();
        marcoGbc.gridx = 0;
        marcoGbc.gridy = 0;
        marcoOpciones.add(sonidoCheckBox, marcoGbc);

        marcoGbc.gridy = 1;
        marcoGbc.insets = new Insets(8, 8, 8, 8);
        marcoGbc.anchor = GridBagConstraints.CENTER;
        marcoOpciones.add(volumenLabel, marcoGbc);

        marcoGbc.gridy = 2;
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
        AudioManager.playMainLoop();
        frame.setContentPane(new MenuPanel(frame));
        frame.revalidate();
        frame.repaint();
    }

    private void marcarListoParaReiniciar() {
        if (!roundOver || localReady) {
            return;
        }

        localReady = true;
        actualizarBotonReiniciar();
        actualizarEstadoReady();
        client.sendPlayerReadyRestart();
        mostrarSoloPanel(pausaPanel);
    }

    private void actualizarEstadoReady() {
        if (!roundOver) {
            estadoReady.setVisible(false);
            estadoReady.setText("");
            return;
        }

        if (!localReady && !remoteReady) {
            estadoReady.setVisible(false);
            estadoReady.setText("");
            return;
        }

        if (localReady && remoteReady) {
            estadoReady.setVisible(false);
            estadoReady.setText("");
            return;
        } else if (localReady) {
            estadoReady.setText("Esperando al otro jugador...");
        } else {
            estadoReady.setText("Tu rival está listo para reiniciar.");
        }
        estadoReady.setVisible(true);
    }

    private void actualizarBotonReiniciar() {
        if (reiniciarButton == null) {
            return;
        }
        reiniciarButton.setEnabled(roundOver && !localReady);
    }

    private void actualizarLayoutMenus() {
        int ancho = Math.max(1, getWidth());
        int alto = Math.max(1, getHeight());

        estadoRonda.setBounds(0, Math.max(20, alto / 9), ancho, 80);
        estadoReady.setBounds(0, Math.max(110, (alto / 9) + 80), ancho, 60);

        int pausaAncho = 560;
        int pausaAlto = 500;
        int opcionesAncho = 560;
        int opcionesAlto = 380;

        pausaPanel.setBounds((ancho - pausaAncho) / 2, (alto - pausaAlto) / 2, pausaAncho, pausaAlto);
        opcionesPanel.setBounds((ancho - opcionesAncho) / 2, (alto - opcionesAlto) / 2, opcionesAncho, opcionesAlto);
    }

    private void reiniciarEstadoLocal() {
        obstaculos.clear();
        String skinHost = "Resources/bird.png";
        String skinCliente = "Resources/bird2.png";
        if (hostAutoritativo) {
            localPlayer = new Personaje(PLAYER_SPAWN_X, PLAYER_SPAWN_Y, skinHost);
            remotePlayer = new Personaje(PLAYER_SPAWN_X, PLAYER_SPAWN_Y, skinCliente);
        } else {
            localPlayer = new Personaje(PLAYER_SPAWN_X, PLAYER_SPAWN_Y, skinCliente);
            remotePlayer = new Personaje(PLAYER_SPAWN_X, PLAYER_SPAWN_Y, skinHost);
        }

        localScore = 0;
        remoteScore = 0;
        roundOver = false;
        localReady = false;
        remoteReady = false;

        estadoRonda.setVisible(false);
        actualizarEstadoReady();
        actualizarBotonReiniciar();
        mostrarSoloPanel(null);

        if (!timer.isRunning()) {
            timer.start();
        }

        requestFocusInWindow();
        repaint();
    }

    private void cambiarSonido(boolean activar) {
        if (sonidoCheckBox != null) {
            sonidoCheckBox.setSelected(activar);
        }
        AudioManager.setSoundEnabled(activar);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String message;
        while ((message = client.pollMessage()) != null) {
            processMessage(message);
        }

        repaint();
    }

    private void finalizarRonda(String estado) {
        if (roundOver) {
            return;
        }
        roundOver = true;
        estadoRonda.setText(estado);
        estadoRonda.setVisible(true);
        mostrarSoloPanel(pausaPanel);
        actualizarBotonReiniciar();
    }

    private String construirMensajeDerrota() {
        return "Perdiste. Tu puntuación fue de: " + localScore;
    }

    private String construirMensajeVictoria() {
        return "¡Ganaste! Tu puntuación fue de: " + localScore;
    }

    private String construirMensajeEmpate() {
        return "Empate. Tu puntuación fue de: " + localScore;
    }

    private void processMessage(String message) {
        String[] parts = message.split("\\$");
        switch (parts[0]) {
            case "server_state" -> aplicarEstadoServidor(parts);
            case "restart_game" -> reiniciarEstadoLocal();
            case "disconnect" -> {
                if (!roundOver) {
                    finalizarRonda("Tu rival se desconectó");
                }
            }
            default -> {
            }
        }
    }

    private void aplicarEstadoServidor(String[] parts) {
        if (parts.length < 15) {
            return;
        }

        int remoteWorldHeight = Integer.parseInt(parts[1]);
        boolean serverRoundOver = Boolean.parseBoolean(parts[2]);
        int roundResult = Integer.parseInt(parts[3]);

        int localIndex = hostAutoritativo ? 0 : 1;
        int p1Y = Integer.parseInt(parts[4]);
        int p1Score = Integer.parseInt(parts[7]);

        int p2Y = Integer.parseInt(parts[8]);
        int p2Score = Integer.parseInt(parts[11]);

        int altoLocal = Math.max(1, getHeight());
        int yP1Escalada = escalarY(p1Y, remoteWorldHeight, altoLocal);
        int yP2Escalada = escalarY(p2Y, remoteWorldHeight, altoLocal);

        if (localIndex == 0) {
            localPlayer.setY(yP1Escalada);
            remotePlayer.setY(yP2Escalada);
            localScore = p1Score;
            remoteScore = p2Score;
        } else {
            localPlayer.setY(yP2Escalada);
            remotePlayer.setY(yP1Escalada);
            localScore = p2Score;
            remoteScore = p1Score;
        }

        boolean p1Ready = Boolean.parseBoolean(parts[12]);
        boolean p2Ready = Boolean.parseBoolean(parts[13]);

        if (localIndex == 0) {
            localReady = p1Ready;
            remoteReady = p2Ready;
        } else {
            localReady = p2Ready;
            remoteReady = p1Ready;
        }
        actualizarBotonReiniciar();

        sincronizarObstaculos(parts, 14, remoteWorldHeight, altoLocal);


        if (serverRoundOver && !roundOver) {
            int localResult = localIndex == 0 ? roundResult : (roundResult == 1 ? 2 : (roundResult == 2 ? 1 : roundResult));
            if (localResult == 3) {
                finalizarRonda(construirMensajeEmpate());
            } else if (localResult == 1) {
                finalizarRonda(construirMensajeVictoria());
            } else if (localResult == 2) {
                finalizarRonda(construirMensajeDerrota());
            }
        }

        actualizarEstadoReady();
    }

    private void sincronizarObstaculos(String[] parts, int startIndex, int remoteWorldHeight, int altoLocal) {
        int obstacleCount = Integer.parseInt(parts[startIndex]);
        int expectedLength = startIndex + 1 + (obstacleCount * 4);
        if (parts.length < expectedLength) {
            return;
        }

        obstaculos.clear();
        int idx = startIndex + 1;
        for (int i = 0; i < obstacleCount; i++) {
            int x = Integer.parseInt(parts[idx++]);
            int topHeight = Integer.parseInt(parts[idx++]);
            int bottomY = Integer.parseInt(parts[idx++]);
            int bottomHeight = Integer.parseInt(parts[idx++]);

            int topHeightScaled = escalarDistancia(topHeight, remoteWorldHeight, altoLocal);
            int bottomYScaled = escalarY(bottomY, remoteWorldHeight, altoLocal);
            int bottomHeightScaled = escalarDistancia(bottomHeight, remoteWorldHeight, altoLocal);

            obstaculos.add(new Obstaculos(x, 0, ANCHO_OBSTACULO, Math.max(1, topHeightScaled),
                    "Resources/obstacle - copia.png"));
            obstaculos.add(new Obstaculos(x, bottomYScaled, ANCHO_OBSTACULO, Math.max(1, bottomHeightScaled),
                    "Resources/obstacle.png"));
        }
    }

    private int escalarY(int yRemota, int altoRemoto, int altoLocal) {
        if (altoRemoto <= 0) {
            return yRemota;
        }
        return (int) Math.round((yRemota / (double) altoRemoto) * altoLocal);
    }

    private int escalarDistancia(int distanciaRemota, int altoRemoto, int altoLocal) {
        if (altoRemoto <= 0) {
            return distanciaRemota;
        }
        return (int) Math.round((distanciaRemota / (double) altoRemoto) * altoLocal);
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
