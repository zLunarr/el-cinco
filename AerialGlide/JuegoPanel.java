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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class JuegoPanel extends JPanel implements ActionListener, KeyListener {
    private final JFrame frame;
    private final Timer tiempo;
    private final Image fondo;
    private Personaje pajaro;
    private final ArrayList<Obstaculos> obstaculos;
    private int contadorTiempo;
    private int puntuacion;
    private int highScore;
    private static final String HIGH_SCORE_FILE = "highscore.txt";
    private static final int ANCHO_OBSTACULO = 120;

    private final JLabel mensajePerder;
    private final JButton botonReiniciar;

    private final JPanel pausaPanel;
    private final JPanel opcionesPanel;
    private final JCheckBox musicaCheckBox;

    private Clip musicaFondo;
    private boolean musicaActivada;
    private boolean enPausa;

    public JuegoPanel(JFrame frame, boolean musicaActivada) {
        this.frame = frame;
        this.musicaActivada = musicaActivada;
        this.fondo = ResourceLoader.loadImage("Resources/fondo juego.jpg");
        this.pajaro = new Personaje(200, 300);
        this.obstaculos = new ArrayList<>();
        this.tiempo = new Timer(20, this);

        setFocusable(true);
        addKeyListener(this);
        requestFocusInWindow();

        setLayout(null);

        mensajePerder = new JLabel("¡Perdiste! Tu puntuación es: 0");
        mensajePerder.setFont(new Font("Arial", Font.BOLD, 60));
        mensajePerder.setForeground(Color.WHITE);
        mensajePerder.setHorizontalAlignment(SwingConstants.CENTER);
        mensajePerder.setBounds(0, 120, 1600, 80);
        mensajePerder.setVisible(false);

        botonReiniciar = new JButton("Reiniciar Juego");
        botonReiniciar.setFont(new Font("Arial", Font.BOLD, 40));
        botonReiniciar.setBackground(Color.WHITE);
        botonReiniciar.setForeground(Color.BLACK);
        botonReiniciar.addActionListener(e -> reiniciarJuego());
        botonReiniciar.setBounds(560, 230, 500, 80);
        botonReiniciar.setVisible(false);

        pausaPanel = crearPanelPausa();
        opcionesPanel = crearPanelOpciones();
        musicaCheckBox = (JCheckBox) opcionesPanel.getComponent(0);

        add(mensajePerder);
        add(botonReiniciar);
        add(pausaPanel);
        add(opcionesPanel);

        puntuacion = 0;

        if (musicaActivada) {
            cargarMusicaFondo();
        }

        cargarHighScore();
        tiempo.start();
    }

    private JPanel crearPanelPausa() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JButton volverButton = crearBotonPausa("VOLVER", "Resources/volver_button.png", 500, 120);
        JButton opcionesButton = crearBotonPausa("OPCIONES", "Resources/option_button.png", 500, 120);

        volverButton.addActionListener(e -> volverAlMenu());
        opcionesButton.addActionListener(e -> mostrarOpcionesPausa());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(12, 12, 12, 12);
        panel.add(volverButton, gbc);
        gbc.gridy = 1;
        panel.add(opcionesButton, gbc);

        panel.setVisible(false);
        return panel;
    }

    private JPanel crearPanelOpciones() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0, 0, 0, 180));

        JCheckBox musicaCheck = new JCheckBox("Activar música");
        musicaCheck.setOpaque(false);
        musicaCheck.setForeground(Color.WHITE);
        musicaCheck.setFont(new Font("Arial", Font.BOLD, 28));
        musicaCheck.setSelected(musicaActivada);
        musicaCheck.addActionListener(e -> cambiarMusica(musicaCheck.isSelected()));

        JButton volverPausa = crearBotonPausa("VOLVER", "Resources/volver_button.png", 420, 100);
        volverPausa.addActionListener(e -> {
            opcionesPanel.setVisible(false);
            pausaPanel.setVisible(true);
            requestFocusInWindow();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(12, 12, 12, 12);
        panel.add(musicaCheck, gbc);
        gbc.gridy = 1;
        panel.add(volverPausa, gbc);

        panel.setVisible(false);
        return panel;
    }

    private JButton crearBotonPausa(String texto, String rutaImagen, int ancho, int alto) {
        JButton boton = new JButton(texto);
        Image image = ResourceLoader.loadImage(rutaImagen);

        if (image != null) {
            boton.setText("");
            boton.setIcon(new ImageIcon(image.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH)));
            boton.setContentAreaFilled(false);
            boton.setBorderPainted(false);
            boton.setFocusPainted(false);
            boton.setOpaque(false);
            boton.setBorder(BorderFactory.createEmptyBorder());
        } else {
            boton.setBackground(new Color(80, 0, 0));
            boton.setForeground(Color.WHITE);
            boton.setFont(new Font("Arial", Font.BOLD, 42));
        }

        boton.setPreferredSize(new Dimension(ancho, alto));
        return boton;
    }

    private void mostrarOpcionesPausa() {
        pausaPanel.setVisible(false);
        opcionesPanel.setVisible(true);
        requestFocusInWindow();
    }

    private void cambiarMusica(boolean activar) {
        musicaActivada = activar;
        if (activar) {
            cargarMusicaFondo();
        } else {
            detenerMusica();
        }
        requestFocusInWindow();
    }

    private void volverAlMenu() {
        tiempo.stop();
        detenerMusica();
        frame.setContentPane(new MenuPanel(frame));
        frame.revalidate();
        frame.repaint();
    }

    private void togglePausa() {
        if (mensajePerder.isVisible()) {
            return;
        }

        enPausa = !enPausa;
        pausaPanel.setVisible(enPausa);
        opcionesPanel.setVisible(false);

        if (enPausa) {
            tiempo.stop();
            pausarMusica();
        } else {
            tiempo.start();
            if (musicaActivada) {
                cargarMusicaFondo();
            }
        }
        requestFocusInWindow();
        repaint();
    }

    private void cargarMusicaFondo() {
        try {
            File musicaArchivo = ResourceLoader.findFile("Resources/Juego 35.wav");
            if (musicaArchivo == null) {
                return;
            }
            if (musicaFondo == null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicaArchivo);
                musicaFondo = AudioSystem.getClip();
                musicaFondo.open(audioStream);
            }
            if (!musicaFondo.isRunning()) {
                musicaFondo.setFramePosition(0);
                musicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ignored) {
        }
    }

    private void pausarMusica() {
        if (musicaFondo != null && musicaFondo.isRunning()) {
            musicaFondo.stop();
        }
    }

    private void detenerMusica() {
        if (musicaFondo != null) {
            musicaFondo.stop();
        }
    }

    private void cargarHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            highScore = Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }
    }

    private void guardarHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException ignored) {
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (fondo != null) {
            g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(20, 30, 50));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        pajaro.draw(g);

        for (Obstaculos obstaculo : obstaculos) {
            obstaculo.dibujar(g);
        }

        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.setColor(Color.WHITE);
        g.drawString("Puntuación: " + puntuacion, 20, 40);
        g.drawString("Récord: " + highScore, 20, 80);

        if (enPausa) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        pausaPanel.setBounds(0, 0, getWidth(), getHeight());
        opcionesPanel.setBounds(0, 0, getWidth(), getHeight());

        int centerX = getWidth() / 2;
        mensajePerder.setBounds(centerX - 700, 120, 1400, 80);
        botonReiniciar.setBounds(centerX - 250, 230, 500, 80);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (enPausa || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        pajaro.update(getHeight());

        for (Obstaculos obstaculo : obstaculos) {
            obstaculo.mover(10);
        }
        obstaculos.removeIf(Obstaculos::fueraDePantalla);

        contadorTiempo++;
        if (contadorTiempo % 80 == 0) {
            generarObstaculos();
        }

        for (Obstaculos obstaculo : obstaculos) {
            if (pajaro.getBounds().intersects(obstaculo.getBounds())) {
                perder();
                return;
            }

            if (obstaculo.getY() > 0 && !obstaculo.isPuntuado()
                    && pajaro.getBounds().x > obstaculo.getX() + obstaculo.getWidth()) {
                obstaculo.marcarPuntuado();
                puntuacion++;
            }
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

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            togglePausa();
            return;
        }

        if (!enPausa && e.getKeyCode() == KeyEvent.VK_SPACE) {
            pajaro.jump();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    private void perder() {
        tiempo.stop();
        enPausa = false;
        pausaPanel.setVisible(false);
        opcionesPanel.setVisible(false);

        if (puntuacion > highScore) {
            highScore = puntuacion;
            guardarHighScore();
            mensajePerder.setText("¡Nuevo récord! Puntuación: " + puntuacion);
        } else {
            mensajePerder.setText("¡Perdiste! Tu puntuación es: " + puntuacion);
        }

        mensajePerder.setVisible(true);
        botonReiniciar.setVisible(true);
        pausarMusica();
    }

    private void reiniciarJuego() {
        contadorTiempo = 0;
        puntuacion = 0;
        obstaculos.clear();
        pajaro = new Personaje(100, 300);

        enPausa = false;
        pausaPanel.setVisible(false);
        opcionesPanel.setVisible(false);
        mensajePerder.setVisible(false);
        botonReiniciar.setVisible(false);

        tiempo.start();
        repaint();

        if (musicaCheckBox != null) {
            musicaCheckBox.setSelected(musicaActivada);
        }

        if (musicaActivada) {
            cargarMusicaFondo();
        } else {
            detenerMusica();
        }
    }
}
