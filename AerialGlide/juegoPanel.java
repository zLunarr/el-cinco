package juegojava;
import javax.swing.*; // Importa los componentes de interfaz gráfica (como JPanel, JLabel, JButton)
import java.awt.*; // Importa herramientas de dibujo y configuración gráfica
import java.awt.event.*; // Importa clases para manejar eventos
import javax.sound.sampled.*; // Importa clases para manejar audio
import java.io.*; // Importa clases para entrada/salida de archivos
import java.util.ArrayList; // Importa la clase ArrayList para manejar listas dinámicas

public class JuegoPanel extends JPanel implements ActionListener, KeyListener {
    // Declaración de variables principales
    private Timer tiempo; // Temporizador para actualizar el juego
    private Image fondo; // Imagen de fondo del juego
    private Personaje pajaro; // Objeto que representa el personaje del juego
    private ArrayList<Obstaculos> obstaculos; // Lista dinámica de obstáculos
    private int contadorTiempo; // Contador para generar obstáculos periódicamente
    private int contadorSaltos; // Contador para la puntuación
    private int highScore; // Mayor puntuación lograda
    private static final String HIGH_SCORE_FILE = "highscore.txt"; // Archivo donde se guarda el récord
    private static final int ESPACIO_ENTRE_OBSTACULOS = 200; // Espacio vertical entre obstáculos
    private static final int ANCHO_OBSTACULO = 120; // Ancho de cada obstáculo

    // Componentes para el mensaje de perder
    private JLabel mensajePerder; // Muestra un mensaje cuando se pierde
    private JButton botonReiniciar; // Botón para reiniciar el juego

    // Reproductor de música
    private Clip musicaFondo; // Controla el audio de fondo
    private boolean musicaActivada; // Controla si la música está activada o no

    // Constructor principal
    public JuegoPanel(boolean musicaActivada) {
        this.musicaActivada = musicaActivada; // Recibe si la música debe estar activada
        fondo = new ImageIcon("C:/JAVA/juegojava/src/Resources/fondo juego.jpg").getImage(); // Carga la imagen de fondo
        pajaro = new Personaje(200, 300); // Inicializa el personaje en una posición fija
        obstaculos = new ArrayList<>(); // Inicializa la lista de obstáculos
        tiempo = new Timer(20, this); // Configura el temporizador para actualizar cada 20 ms
        tiempo.start(); // Inicia el temporizador

        setFocusable(true); // Habilita el foco en el panel para recibir eventos de teclado
        addKeyListener(this); // Asocia el panel como receptor de eventos de teclado
        requestFocusInWindow(); // Solicita el foco en la ventana

        // Configura los componentes de interfaz gráfica para el mensaje de perder
        mensajePerder = new JLabel("¡Perdiste! Tu puntuación es: 0");
        mensajePerder.setFont(new Font("Arial", Font.BOLD, 60));
        mensajePerder.setForeground(Color.WHITE);
        mensajePerder.setHorizontalAlignment(SwingConstants.CENTER);
        mensajePerder.setVisible(false);

        botonReiniciar = new JButton("Reiniciar Juego");
        botonReiniciar.setFont(new Font("Arial", Font.BOLD, 40));
        botonReiniciar.setBackground(Color.WHITE);
        botonReiniciar.setForeground(Color.BLACK);
        botonReiniciar.addActionListener(e -> reiniciarJuego()); // Asocia la acción de reiniciar
        botonReiniciar.setVisible(false);

        setLayout(new BorderLayout()); // Usa un diseño de bordes
        add(mensajePerder, BorderLayout.CENTER); // Añade el mensaje al centro
        add(botonReiniciar, BorderLayout.SOUTH); // Añade el botón en la parte inferior

        contadorSaltos = 0; // Inicializa la puntuación a 0

        if (musicaActivada) {
            cargarMusicaFondo(); // Carga la música si está activada
        }

        cargarHighScore(); // Carga el récord desde el archivo
    }

    // Método para cargar la música de fondo
    private void cargarMusicaFondo() {
        try {
            File musicaArchivo = new File("C:/JAVA/juegojava/src/Resources/Juego 35.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicaArchivo);
            musicaFondo = AudioSystem.getClip();
            musicaFondo.open(audioStream);
            musicaFondo.loop(Clip.LOOP_CONTINUOUSLY); // Reproduce la música en bucle
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // Método para cargar el récord desde un archivo
    private void cargarHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            highScore = Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            highScore = 0; // Si no hay archivo o está corrupto, inicializa el récord en 0
        }
    }

    // Método para guardar el récord en un archivo
    private void guardarHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Dibuja el fondo del panel
        g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this); // Dibuja la imagen de fondo
        pajaro.draw(g); // Dibuja el personaje

        for (Obstaculos obstaculo : obstaculos) {
            obstaculo.dibujar(g); // Dibuja cada obstáculo
        }
        
        // Dibuja la puntuación y el récord
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.setColor(Color.WHITE);
        g.drawString("Puntuación: " + contadorSaltos, 20, 40);
        g.drawString("Récord: " + highScore, 20, 80);
    }

    public void actionPerformed(ActionEvent e) {
        pajaro.update(getHeight()); // Actualiza la posición del personaje

        for (Obstaculos obstaculo : obstaculos) {
            obstaculo.mover(10); // Mueve los obstáculos hacia la izquierda
        }
        obstaculos.removeIf(Obstaculos::fueraDePantalla); // Elimina los obstáculos fuera de la pantalla

        contadorTiempo++;
        if (contadorTiempo % 80 == 0) { // Genera un nuevo par de obstáculos periódicamente
            generarObstaculos();
        }

        for (Obstaculos obstaculo : obstaculos) {
            if (pajaro.getBounds().intersects(obstaculo.getBounds())) { // Detecta colisiones
                perder();
                return;
            }
        }
        repaint(); // Redibuja el panel
    }

    // Método para generar obstáculos
    private void generarObstaculos() {
        int alturaVentana = getHeight();
        int espacioVertical = 210; // Espacio entre los obstáculos superior e inferior
        int alturaObstaculoSuperior = (int) (Math.random() * (alturaVentana - espacioVertical - 100)) + 60;

        // Obstáculo superior
        obstaculos.add(new Obstaculos(getWidth(), 0, ANCHO_OBSTACULO, alturaObstaculoSuperior,
                "C:/JAVA/juegojava/src/Resources/obstacle - copia.png"));

        // Obstáculo inferior
        obstaculos.add(new Obstaculos(getWidth(), alturaObstaculoSuperior + espacioVertical, ANCHO_OBSTACULO,
                alturaVentana - alturaObstaculoSuperior - espacioVertical,
                "C:/JAVA/juegojava/src/Resources/obstacle.png"));
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) { // Detecta la tecla "espacio" para saltar
            pajaro.jump();
            contadorSaltos++;
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    // Método llamado al perder el juego
    private void perder() {
        tiempo.stop(); // Detiene el temporizador

        if (contadorSaltos > highScore) {
            highScore = contadorSaltos;
            guardarHighScore(); // Actualiza el récord si es superado
            mensajePerder.setText("¡Nuevo récord! Puntuación: " + contadorSaltos);
        } else {
            mensajePerder.setText("¡Perdiste! Tu puntuación es: " + contadorSaltos);
        }

        mensajePerder.setVisible(true);
        botonReiniciar.setVisible(true);

        if (musicaFondo != null && musicaFondo.isRunning()) {
            musicaFondo.stop(); // Detiene la música
        }
    }

    // Método para reiniciar el juego
    private void reiniciarJuego() {
        contadorTiempo = 0;
        contadorSaltos = 0;
        obstaculos.clear(); // Elimina todos los obstáculos
        pajaro = new Personaje(100, 300); // Restaura la posición del personaje

        mensajePerder.setVisible(false);
        botonReiniciar.setVisible(false);

        tiempo.start(); // Reanuda el temporizador
        repaint();

        if (musicaActivada) {
            if (musicaFondo != null && musicaFondo.isRunning()) {
                musicaFondo.stop();
            }
            cargarMusicaFondo(); // Reinicia la música
        }
    }
}
