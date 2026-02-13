package juegojava;
import java.awt.*; // Importa clases para manejar gráficos y diseño.
import java.awt.event.*; // Importa clases para manejar eventos.
import javax.swing.*; // Importa clases para interfaces gráficas.
import javax.sound.sampled.*; // Importa clases para manejar audio.
import java.io.*; // Importa clases para manejar archivos.

class MenuPanel extends JPanel {
    private final Image fondo; // Imagen de fondo para el menú.
    private Clip musicaFondo; // Clip de audio para la música de fondo.
    private boolean musicaActivada = true; // Indica si la música está activada o no.
    
    public MenuPanel(JFrame frame) {
        setLayout(new GridBagLayout()); // Usa un diseño de cuadrícula para centrar los componentes.
        fondo = new ImageIcon("C:/JAVA/juegojava/src/Resources/background.png").getImage(); // Carga la imagen de fondo.

        // Carga y reproduce la música de fondo.
        cargarMusicaFondo();

        // Crea los botones para el menú.
        JButton jugarButton = crearBoton("Jugar", "C:/JAVA/juegojava/src/Resources/play_button.png");
        JButton opcionesButton = crearBoton("Opciones", "C:/JAVA/juegojava/src/Resources/option_button.png");

        JButton[] botones = {jugarButton, opcionesButton};
        agregarEventos(frame, botones); // Asocia eventos a los botones.

        // Añade los botones al panel con espaciado vertical.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 30, 30, 30); // Margen entre botones.
        gbc.gridx = 0; // Alinea los botones en el eje X.
        gbc.gridy = 0; // Comienza en la primera fila.
        for (JButton boton : botones) {
            add(boton, gbc); // Añade el botón al panel.
            gbc.gridy++; // Incrementa la posición en el eje Y para el siguiente botón.
        }
    }

     //Crea un botón con texto e imagen personalizada.
     //rutaImagen Ruta de la imagen del botón.
    private JButton crearBoton(String texto, String rutaImagen) {
        JButton boton = new JButton(texto, new ImageIcon(rutaImagen)); // Crea el botón con texto e imagen.
        boton.setPreferredSize(new Dimension(300, 65)); // Tamaño del botón.
        boton.setFont(new Font("Arial", Font.BOLD, 10)); // Fuente del texto.
        boton.setBackground(Color.CYAN); // Color de fondo del botón.
        boton.setForeground(Color.BLACK); // Color del texto.	
        boton.setHorizontalTextPosition(SwingConstants.CENTER); // Centra el texto horizontalmente.
        boton.setVerticalTextPosition(SwingConstants.BOTTOM); // Coloca el texto debajo de la imagen.
        return boton;
    }

    private void agregarEventos(JFrame frame, JButton[] botones) {
        botones[0].addActionListener(e -> iniciarJuego(frame)); // Inicia el juego al hacer clic en "Jugar".
        botones[1].addActionListener(e -> mostrarOpciones(frame)); // Muestra las opciones al hacer clic en "Opciones".
    }

    private void iniciarJuego(JFrame frame) {
        if (musicaFondo != null && musicaFondo.isRunning()) { // Detiene la música si está sonando.
            musicaFondo.stop();
        }

        // Crea y muestra el panel del juego.
        JuegoPanel juego = new JuegoPanel(musicaActivada); // Pasa el estado de la música al panel del juego.
        frame.setContentPane(juego); // Cambia el contenido de la ventana.
        frame.revalidate(); // Refresca la ventana.
        frame.repaint(); // Redibuja la ventana.
        juego.requestFocusInWindow(); // Asegura el enfoque en el panel del juego.
    }

    //Muestra el panel de opciones para activar/desactivar la música.
    private void mostrarOpciones(JFrame frame) {
        JPanel panelOpciones = new JPanel() { // Crea un panel de opciones.
            protected void paintComponent(Graphics g) { // Dibuja el fondo.
                super.paintComponent(g);
                g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panelOpciones.setLayout(new GridBagLayout()); // Centra los componentes.

        JCheckBox musicaCheckBox = new JCheckBox("Activar música"); // Casilla para controlar la música.
        musicaCheckBox.setSelected(musicaActivada); // Establece el estado inicial.
        musicaCheckBox.addItemListener(e -> { // Controla el estado de la música.
            if (musicaCheckBox.isSelected()) activarMusica();
            else detenerMusica();
        });
        musicaCheckBox.setFont(new Font("Arial", Font.BOLD, 30)); // Texto más grande
        GridBagConstraints gbc = new GridBagConstraints(); // Configura el diseño del panel.
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelOpciones.add(musicaCheckBox, gbc); // Añade la casilla al panel.

        JButton cerrarButton = new JButton("Cerrar Opciones"); // Botón para volver al menú.
        cerrarButton.setPreferredSize(new Dimension(200, 60)); // Dimensiones más grandes
        cerrarButton.setFont(new Font("Arial", Font.BOLD, 20)); // Texto más grande
        cerrarButton.addActionListener(e -> {
            if (musicaActivada && (musicaFondo != null && !musicaFondo.isRunning())) {
                activarMusica(); // Reactiva la música si está activada.
            }
            frame.setContentPane(this); // Vuelve al menú principal.
            frame.revalidate();
            frame.repaint();
        });

        gbc.gridy = 1; // Posiciona el botón debajo de la casilla.
        panelOpciones.add(cerrarButton, gbc);

        frame.setContentPane(panelOpciones); // Cambia al panel de opciones.
        frame.revalidate();
        frame.repaint();
    }

    //Carga la música de fondo del archivo.
    private void cargarMusicaFondo() {
        try {
            if (musicaFondo == null) { // Carga la música solo si no ha sido cargada.
                File musicaArchivo = new File("C:/JAVA/juegojava/src/Resources/Juego 35.wav");
                if (!musicaArchivo.exists()) { // Verifica si el archivo existe.
                    JOptionPane.showMessageDialog(this, "El archivo de música no se encuentra.", "Error de música", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicaArchivo);
                musicaFondo = AudioSystem.getClip();
                musicaFondo.open(audioStream);

                if (musicaActivada) activarMusica(); // Activa la música si está activada.
            }
        } catch (Exception e) { // Maneja errores al cargar la música.
            JOptionPane.showMessageDialog(this, "Error al cargar la música: " + e.getMessage(), "Error de música", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    //Activa y reproduce la música de fondo en bucle.
    private void activarMusica() {
        if (musicaFondo != null && !musicaFondo.isRunning()) {
            musicaFondo.setFramePosition(0); // Reinicia el audio.
            musicaFondo.start();
            musicaFondo.loop(Clip.LOOP_CONTINUOUSLY); // Reproduce en bucle.
            musicaActivada = true; // Marca la música como activada.
        }
    }

    // Detiene la música de fondo.
    private void detenerMusica() {
        if (musicaFondo != null && musicaFondo.isRunning()) musicaFondo.stop();
        musicaActivada = false; // Marca la música como desactivada.
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
    }
}
