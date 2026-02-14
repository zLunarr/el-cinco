package juegojava;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import online.Server;

class MenuPanel extends JPanel {
    private final Image fondo;
    private Clip musicaFondo;
    private boolean musicaActivada = true;

    public MenuPanel(JFrame frame) {
        setLayout(new GridBagLayout());
        fondo = ResourceLoader.loadImage("Resources/background.png");

        cargarMusicaFondo();

        JButton jugarButton = crearBoton("Jugar", "Resources/play_button.png");
        JButton multijugadorButton = crearBoton("Multijugador", "Resources/select_character.png");
        JButton opcionesButton = crearBoton("Opciones", "Resources/option_button.png");

        JButton[] botones = {jugarButton, multijugadorButton, opcionesButton};
        agregarEventos(frame, botones);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        for (JButton boton : botones) {
            add(boton, gbc);
            gbc.gridy++;
        }
    }

    private JButton crearBoton(String texto, String rutaImagen) {
        Image image = ResourceLoader.loadImage(rutaImagen);
        JButton boton = image != null ? new JButton(texto, new ImageIcon(image)) : new JButton(texto);
        boton.setPreferredSize(new Dimension(300, 70));
        boton.setFont(new Font("Arial", Font.BOLD, 20));
        boton.setBackground(Color.CYAN);
        boton.setForeground(Color.BLACK);
        boton.setHorizontalTextPosition(SwingConstants.CENTER);
        boton.setVerticalTextPosition(SwingConstants.BOTTOM);
        return boton;
    }

    private void agregarEventos(JFrame frame, JButton[] botones) {
        botones[0].addActionListener(e -> iniciarJuego(frame));
        botones[1].addActionListener(e -> mostrarMenuMultijugador(frame));
        botones[2].addActionListener(e -> mostrarOpciones(frame));
    }

    private void iniciarJuego(JFrame frame) {
        detenerMusica();
        JuegoPanel juego = new JuegoPanel(musicaActivada);
        frame.setContentPane(juego);
        frame.revalidate();
        frame.repaint();
        juego.requestFocusInWindow();
    }

    private void mostrarMenuMultijugador(JFrame frame) {
        JPanel panel = crearPanelConFondo();
        panel.setLayout(new GridBagLayout());

        JButton host = new JButton("Crear sala (Servidor + Cliente)");
        JButton join = new JButton("Unirse a sala (Solo Cliente)");
        JButton back = new JButton("Volver");

        host.addActionListener(e -> iniciarSalaComoHost(frame));
        join.addActionListener(e -> iniciarSalaComoCliente(frame));
        back.addActionListener(e -> volverAlMenu(frame));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(host, gbc);
        gbc.gridy++;
        panel.add(join, gbc);
        gbc.gridy++;
        panel.add(back, gbc);

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void iniciarSalaComoHost(JFrame frame) {
        Server.ensureRunning();
        String username = pedirUsername();
        String localIp = obtenerIpLocal();
        JOptionPane.showMessageDialog(frame, "Servidor creado. Pasale esta IP al otro jugador: " + localIp + "\nPuerto UDP: 5555");
        MultiplayerLobbyPanel lobby = new MultiplayerLobbyPanel(frame, "127.0.0.1", username);
        frame.setContentPane(lobby);
        frame.revalidate();
        frame.repaint();
    }

    private void iniciarSalaComoCliente(JFrame frame) {
        String ip = JOptionPane.showInputDialog(frame, "IP del servidor (ej: 192.168.1.20):", "");
        if (ip == null || ip.isBlank()) {
            return;
        }

        String username = pedirUsername();
        MultiplayerLobbyPanel lobby = new MultiplayerLobbyPanel(frame, ip.trim(), username);
        frame.setContentPane(lobby);
        frame.revalidate();
        frame.repaint();
    }

    private String pedirUsername() {
        String username = JOptionPane.showInputDialog(this, "Tu nombre:", "Jugador");
        if (username == null || username.isBlank()) {
            return "Jugador" + (int) (Math.random() * 1000);
        }
        return username.trim();
    }

    private String obtenerIpLocal() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "No disponible";
        }
    }

    private JPanel crearPanelConFondo() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (fondo != null) {
                    g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(30, 30, 40));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
    }

    private void mostrarOpciones(JFrame frame) {
        JPanel panelOpciones = crearPanelConFondo();
        panelOpciones.setLayout(new GridBagLayout());

        JCheckBox musicaCheckBox = new JCheckBox("Activar música");
        musicaCheckBox.setSelected(musicaActivada);
        musicaCheckBox.addItemListener(e -> {
            if (musicaCheckBox.isSelected()) {
                activarMusica();
            } else {
                detenerMusica();
            }
        });

        JButton cerrarButton = new JButton("Cerrar Opciones");
        cerrarButton.addActionListener(e -> volverAlMenu(frame));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelOpciones.add(musicaCheckBox, gbc);
        gbc.gridy = 1;
        panelOpciones.add(cerrarButton, gbc);

        frame.setContentPane(panelOpciones);
        frame.revalidate();
        frame.repaint();
        if (musicaActivada && (musicaFondo == null || !musicaFondo.isRunning())) {
            cargarMusicaFondo();
        }
    }

    private void volverAlMenu(JFrame frame) {
        frame.setContentPane(this);
        frame.revalidate();
        frame.repaint();
        if (musicaActivada && (musicaFondo == null || !musicaFondo.isRunning())) {
            cargarMusicaFondo();
        }
    }

    private void cargarMusicaFondo() {
        try {
            if (musicaFondo != null && musicaFondo.isRunning()) {
                musicaFondo.stop();
            }
            File musicaArchivo = ResourceLoader.findFile("Resources/Juego 35.wav");
            if (musicaArchivo == null) {
                musicaActivada = false;
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicaArchivo);
            musicaFondo = AudioSystem.getClip();
            musicaFondo.open(audioStream);
            musicaFondo.loop(Clip.LOOP_CONTINUOUSLY);
            musicaActivada = true;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            musicaActivada = false;
        }
    }

    private void activarMusica() {
        musicaActivada = true;
        cargarMusicaFondo();
    }

    private void detenerMusica() {
        musicaActivada = false;
        if (musicaFondo != null && musicaFondo.isRunning()) {
            musicaFondo.stop();
        }
    }
}
