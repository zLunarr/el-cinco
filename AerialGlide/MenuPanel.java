package juegojava;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import online.Server;

class MenuPanel extends JPanel {
    private final Image fondo;
    private final Image titulo;

    public MenuPanel(JFrame frame) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        fondo = ResourceLoader.loadImage("Resources/background.png");
        titulo = ResourceLoader.loadImage("Resources/menu_title.png");

        registrarSalidaConEsc(frame);

        AudioManager.playMainLoop();

        JButton jugarButton = crearBotonConImagen("Jugar", "Resources/play_button.png", 420, 120);
        Dimension tamanoOnline = calcularTamanoPorTexto("ONLINE", "OPCIONES", 420, 120);
        JButton multijugadorButton = crearBotonConImagen("Multijugador", "Resources/imgonline.png", tamanoOnline.width,
                tamanoOnline.height);
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

    private void registrarSalidaConEsc(JFrame frame) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "salirJuego");
        getActionMap().put("salirJuego", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (frame.getContentPane() == MenuPanel.this) {
                    System.exit(0);
                }
            }
        });
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
        JuegoPanel juego = new JuegoPanel(frame);
        frame.setContentPane(juego);
        frame.revalidate();
        frame.repaint();
        juego.requestFocusInWindow();
    }

    private void mostrarMenuMultijugador(JFrame frame) {
        JPanel panel = crearPanelConFondo();
        panel.setLayout(new GridBagLayout());
        Color azulMenuMultijugador = new Color(0, 170, 255);

        JPanel marco = new JPanel(new GridBagLayout());
        marco.setOpaque(true);
        marco.setBackground(azulMenuMultijugador);
        marco.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(azulMenuMultijugador, 3),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)));

        JButton host = crearBotonSecundario("Crear sala");
        JButton join = crearBotonSecundario("Unirse a sala");
        Dimension tamanoVolver = calcularTamanoPorTexto("VOLVER", "OPCIONES", 420, 120);
        JButton back = crearBotonConImagen("VOLVER", "Resources/imgvolver.png", tamanoVolver.width, tamanoVolver.height);

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

    static void mostrarOnlineMenu(JFrame frame) {
        MenuPanel menu = new MenuPanel(frame);
        menu.mostrarMenuMultijugador(frame);
    }

    private void iniciarSalaComoHost(JFrame frame) {
        String username = pedirUsername();
        if (username == null) {
            return;
        }

        Server.ensureRunning();
        String localIp = obtenerIpLocal();
        JOptionPane.showMessageDialog(frame, "Servidor creado. Pasale esta IP al otro jugador: " + localIp + "\nPuerto UDP: 5555");
        MultiplayerLobbyPanel lobby = new MultiplayerLobbyPanel(frame, "127.0.0.1", username, true);
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
        if (username == null) {
            return;
        }

        MultiplayerLobbyPanel lobby = new MultiplayerLobbyPanel(frame, ip.trim(), username, false);
        frame.setContentPane(lobby);
        frame.revalidate();
        frame.repaint();
    }

    private String pedirUsername() {
        String username = JOptionPane.showInputDialog(this, "Tu nombre:", "Jugador");
        if (username == null) {
            return null;
        }

        if (username.isBlank()) {
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

        JCheckBox musicaCheckBox = new JCheckBox("Activar sonido");
        musicaCheckBox.setOpaque(false);
        musicaCheckBox.setContentAreaFilled(false);
        musicaCheckBox.setBorderPainted(false);
        musicaCheckBox.setForeground(Color.WHITE);
        musicaCheckBox.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        musicaCheckBox.setSelected(AudioManager.isSoundEnabled());
        musicaCheckBox.addItemListener(e -> {
            if (musicaCheckBox.isSelected()) {
                AudioManager.setSoundEnabled(true);
            } else {
                AudioManager.setSoundEnabled(false);
            }
        });

        JLabel volumenLabel = new JLabel("Volumen");
        volumenLabel.setForeground(new Color(205, 240, 255));
        volumenLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));

        JSlider volumenSlider = new JSlider(0, 100, AudioManager.getVolumePercent());
        volumenSlider.setOpaque(false);
        volumenSlider.setMajorTickSpacing(25);
        volumenSlider.setPaintTicks(false);
        volumenSlider.setPaintLabels(false);
        volumenSlider.setForeground(Color.WHITE);
        volumenSlider.addChangeListener(e -> {
            AudioManager.setVolumePercent(volumenSlider.getValue());
        });

        Dimension tamanoVolver = calcularTamanoPorTexto("VOLVER", "OPCIONES", 420, 120);
        JButton volverButton = crearBotonConImagen("VOLVER", "Resources/imgvolver.png", tamanoVolver.width, tamanoVolver.height);
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
        AudioManager.playMainLoop();
    }

    private void volverAlMenuPrincipal(JFrame frame) {
        frame.setContentPane(this);
        frame.revalidate();
        frame.repaint();
        AudioManager.playMainLoop();
    }

    private Dimension calcularTamanoPorTexto(String textoObjetivo, String textoReferencia, int anchoBase, int altoBase) {
        if (textoObjetivo == null || textoObjetivo.isBlank() || textoReferencia == null || textoReferencia.isBlank()) {
            return new Dimension(anchoBase, altoBase);
        }
        double proporcion = (double) textoObjetivo.length() / textoReferencia.length();
        int ancho = (int) Math.round(anchoBase * proporcion);
        int alto = (int) Math.round(altoBase * proporcion);
        return new Dimension(ancho, alto);
    }

    private Dimension calcularTamanoPorTexto(String textoObjetivo, String textoReferencia, int anchoBase, int altoBase) {
        if (textoObjetivo == null || textoObjetivo.isBlank() || textoReferencia == null || textoReferencia.isBlank()) {
            return new Dimension(anchoBase, altoBase);
        }
        double proporcion = (double) textoObjetivo.length() / textoReferencia.length();
        int ancho = (int) Math.round(anchoBase * proporcion);
        int alto = (int) Math.round(altoBase * proporcion);
        return new Dimension(ancho, alto);
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
        double proporcion = (double) textoObjetivo.length() / textoReferencia.length();
        int ancho = (int) Math.round(anchoBase * proporcion);
        int alto = (int) Math.round(altoBase * proporcion);
        return new Dimension(ancho, alto);
    }

}
