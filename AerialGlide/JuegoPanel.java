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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class JuegoPanel extends JPanel implements ActionListener, KeyListener {
    private Timer tiempo;
    private Image fondo;
    private Personaje pajaro;
    private ArrayList<Obstaculos> obstaculos;
    private int contadorTiempo;
    private int puntuacion;
    private int highScore;
    private static final String HIGH_SCORE_FILE = "highscore.txt";
    private static final int ANCHO_OBSTACULO = 120;

    private JLabel mensajePerder;
    private JButton botonReiniciar;

    private Clip musicaFondo;
    private boolean musicaActivada;

    public JuegoPanel(boolean musicaActivada) {
        this.musicaActivada = musicaActivada;
        fondo = ResourceLoader.loadImage("Resources/fondo juego.jpg");
        pajaro = new Personaje(200, 300);
        obstaculos = new ArrayList<>();
        tiempo = new Timer(20, this);
        tiempo.start();

        setFocusable(true);
        addKeyListener(this);
        requestFocusInWindow();

        mensajePerder = new JLabel("¡Perdiste! Tu puntuación es: 0");
        mensajePerder.setFont(new Font("Arial", Font.BOLD, 60));
        mensajePerder.setForeground(Color.WHITE);
        mensajePerder.setHorizontalAlignment(SwingConstants.CENTER);
        mensajePerder.setVisible(false);

        botonReiniciar = new JButton("Reiniciar Juego");
        botonReiniciar.setFont(new Font("Arial", Font.BOLD, 40));
        botonReiniciar.setBackground(Color.WHITE);
        botonReiniciar.setForeground(Color.BLACK);
        botonReiniciar.addActionListener(e -> reiniciarJuego());
        botonReiniciar.setVisible(false);

        setLayout(new BorderLayout());
        add(mensajePerder, BorderLayout.CENTER);
        add(botonReiniciar, BorderLayout.SOUTH);

        puntuacion = 0;

        if (musicaActivada) {
            cargarMusicaFondo();
        }

        cargarHighScore();
    }

    private void cargarMusicaFondo() {
        try {
            File musicaArchivo = ResourceLoader.findFile("Resources/Juego 35.wav");
            if (musicaArchivo == null) {
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicaArchivo);
            musicaFondo = AudioSystem.getClip();
            musicaFondo.open(audioStream);
            musicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ignored) {
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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (getWidth() <= 0 || getHeight() <= 0) {
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
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            pajaro.jump();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    private void perder() {
        tiempo.stop();

        if (puntuacion > highScore) {
            highScore = puntuacion;
            guardarHighScore();
            mensajePerder.setText("¡Nuevo récord! Puntuación: " + puntuacion);
        } else {
            mensajePerder.setText("¡Perdiste! Tu puntuación es: " + puntuacion);
        }

        mensajePerder.setVisible(true);
        botonReiniciar.setVisible(true);

        if (musicaFondo != null && musicaFondo.isRunning()) {
            musicaFondo.stop();
        }
    }

    private void reiniciarJuego() {
        contadorTiempo = 0;
        puntuacion = 0;
        obstaculos.clear();
        pajaro = new Personaje(100, 300);

        mensajePerder.setVisible(false);
        botonReiniciar.setVisible(false);

        tiempo.start();
        repaint();

        if (musicaActivada) {
            if (musicaFondo != null && musicaFondo.isRunning()) {
                musicaFondo.stop();
            }
            cargarMusicaFondo();
        }
    }
}
