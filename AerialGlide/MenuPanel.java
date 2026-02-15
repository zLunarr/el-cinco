package juegojava;

import java.awt.Color;
import java.awt.Dimension;
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
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import online.Server;

class MenuPanel extends JPanel {
    private final Image fondo;
    private final Image titulo;
    private Clip musicaFondo;
    private boolean musicaActivada = true;
    private int volumenPorcentaje = 70;

    public MenuPanel(JFrame frame) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        fondo = ResourceLoader.loadImage("Resources/background.png");
        titulo = ResourceLoader.loadImage("Resources/menu_title.png");

        cargarMusicaFondo();

        JButton jugarButton = crearBotonConImagen("Jugar", "Resources/play_button.png", 420, 120);
        JButton multijugadorButton = crearBotonConImagen("Multijugador", "Resources/imgonline.png", 420, 120);
        JButton opcionesButton = crearBotonConImagen("Opciones", "Resources/option_button.png", 420, 120);

        JButton[] botones = {jugarButton, multijugadorButton, opcionesButton};
        agregarEventos(frame, botones);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 20, 12, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;

        if (titulo != null) {
            JLabel tituloLabel = new JLabel(new ImageIcon(titulo.getScaledInstance(480, 220, Image.SCALE_SMOOTH)));
            add(tituloLabel, gbc);
            gbc.gridy++;
        }

        for (JButton boton : botones) {
            add(boton, gbc);
            gbc.gridy++;
        }
    }

    private JButton crearBotonConImagen(String texto, String rutaImagen, int ancho, int alto) {
        JButton boton = new JButton(texto);
        Image image = ResourceLoader.loadImage(rutaImagen);

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

    private JButton crearBotonSecundario(String texto) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(360, 60));
        boton.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        boton.setForeground(Color.WHITE);
        boton.setBackground(new Color(0, 75, 120, 190));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createLineBorder(new Color(190, 235, 255, 220), 2));
        return boton;
    }

    private Image escalarImagen(Image image, int ancho, int alto) {
        return image.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
    }

    private void agregarEventos(JFrame frame, JButton[] botones) {
        botones[0].addActionListener(e -> iniciarJuego(frame));
        botones[1].addActionListener(e -> mostrarMenuMultijugador(frame));
        botones[2].addActionListener(e -> mostrarOpciones(frame));
    }

    private void iniciarJuego(JFrame frame) {
        pausarMusicaMenu();
        JuegoPanel juego = new JuegoPanel(frame, musicaActivada);
        frame.setContentPane(juego);
        frame.revalidate();
        frame.repaint();
        juego.requestFocusInWindow();
    }

    private void mostrarMenuMultijugador(JFrame frame) {
        JPanel panel = crearPanelConFondo();
        panel.setLayout(new GridBagLayout());

        JPanel marco = new JPanel(new GridBagLayout());
        marco.setOpaque(false);
        marco.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 220), 3),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)));

        JButton host = crearBotonSecundario("Crear sala");
        JButton join = crearBotonSecundario("Unirse a sala");
        JButton back = crearBotonConImagen("VOLVER", "Resources/imgvolver.png", 320, 100);

        host.addActionListener(e -> iniciarSalaComoHost(frame));
        join.addActionListener(e -> iniciarSalaComoCliente(frame));
        back.addActionListener(e -> volverAlMenuPrincipal(frame));

        GridBagConstraints marcoGbc = new GridBagConstraints();
        marcoGbc.insets = new Insets(8, 8, 8, 8);
        marcoGbc.gridx = 0;
        marcoGbc.gridy = 0;
        marcoGbc.fill = GridBagConstraints.HORIZONTAL;
        marcoGbc.weightx = 1.0;
        marco.add(host, marcoGbc);
        marcoGbc.gridy++;
        marco.add(join, marcoGbc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(marco, gbc);
        gbc.gridy = 1;
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

    private void mostrarOpciones(JFrame frame) {
        JPanel panelOpciones = crearPanelConFondo();
        panelOpciones.setLayout(new GridBagLayout());

        JPanel marcoOpciones = new JPanel(new GridBagLayout());
        marcoOpciones.setOpaque(false);
        marcoOpciones.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 210), 3),
                BorderFactory.createEmptyBorder(18, 28, 18, 28)));

        JCheckBox musicaCheckBox = new JCheckBox("Activar música");
        musicaCheckBox.setOpaque(false);
        musicaCheckBox.setContentAreaFilled(false);
        musicaCheckBox.setBorderPainted(false);
        musicaCheckBox.setForeground(Color.WHITE);
        musicaCheckBox.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        musicaCheckBox.setSelected(musicaActivada);
        musicaCheckBox.addItemListener(e -> {
            if (musicaCheckBox.isSelected()) {
                activarMusica();
            } else {
                detenerMusica();
            }
        });

        JLabel volumenLabel = new JLabel("Volumen");
        volumenLabel.setForeground(new Color(205, 240, 255));
        volumenLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));

        JSlider volumenSlider = new JSlider(0, 100, volumenPorcentaje);
        volumenSlider.setOpaque(false);
        volumenSlider.setMajorTickSpacing(25);
        volumenSlider.setPaintTicks(false);
        volumenSlider.setPaintLabels(false);
        volumenSlider.setForeground(Color.WHITE);
        volumenSlider.addChangeListener(e -> {
            volumenPorcentaje = volumenSlider.getValue();
            aplicarVolumen();
        });

        JButton volverButton = crearBotonConImagen("VOLVER", "Resources/imgvolver.png", 320, 100);
        volverButton.addActionListener(e -> volverAlMenuPrincipal(frame));

        GridBagConstraints marcoGbc = new GridBagConstraints();
        marcoGbc.gridx = 0;
        marcoGbc.gridy = 0;
        marcoGbc.insets = new Insets(8, 8, 8, 8);
        marcoGbc.anchor = GridBagConstraints.CENTER;
        marcoOpciones.add(musicaCheckBox, marcoGbc);

        marcoGbc.gridy = 1;
        marcoOpciones.add(volumenLabel, marcoGbc);

        marcoGbc.gridy = 2;
        marcoGbc.fill = GridBagConstraints.HORIZONTAL;
        marcoGbc.weightx = 1.0;
        marcoOpciones.add(volumenSlider, marcoGbc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        panelOpciones.add(marcoOpciones, gbc);

        gbc.gridy = 1;
        panelOpciones.add(volverButton, gbc);

        frame.setContentPane(panelOpciones);
        frame.revalidate();
        frame.repaint();
        if (musicaActivada && (musicaFondo == null || !musicaFondo.isRunning())) {
            cargarMusicaFondo();
        }
        aplicarVolumen();
    }

    private void volverAlMenuPrincipal(JFrame frame) {
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
            aplicarVolumen();
            musicaActivada = true;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            musicaActivada = false;
        }
    }


    private void aplicarVolumen() {
        if (musicaFondo == null || !musicaFondo.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl control = (FloatControl) musicaFondo.getControl(FloatControl.Type.MASTER_GAIN);
        if (!musicaActivada || volumenPorcentaje <= 0) {
            control.setValue(control.getMinimum());
            return;
        }
        float min = control.getMinimum();
        float max = control.getMaximum();
        float gain = min + (max - min) * (volumenPorcentaje / 100f);
        control.setValue(Math.max(min, Math.min(max, gain)));
    }

    private void pausarMusicaMenu() {
        if (musicaFondo != null && musicaFondo.isRunning()) {
            musicaFondo.stop();
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
