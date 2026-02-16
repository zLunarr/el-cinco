package juegojava;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class Ejecucion {
    public static void main(String[] args) {
        // Crear la ventana del juego
        JFrame frame = new JFrame();
        // Crear el panel del menú de inicio
        MenuPanel menuPanel = new MenuPanel(frame);

        // Configuración de ventana completa sin bordes
        frame.setUndecorated(true);
        frame.add(menuPanel);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack(); // Ajusta el tamaño de la ventana a la del panel
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setIconImage(ResourceLoader.loadImage("Resources/icono.jpg"));
        // Título de la ventana
        frame.setTitle("Aerial Glide - Menú de Inicio");
        // Cerrar la aplicación al salir
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Evita que la ventana sea redimensionable
        frame.setResizable(false);
        // Centra la ventana en la pantalla
        frame.setLocationRelativeTo(null);
        // ESC siempre cierra la app, sin depender del foco de un panel específico
        JRootPane rootPane = frame.getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "salirAplicacion");
        rootPane.getActionMap().put("salirAplicacion", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });

        // Fallback global: captura ESC desde el primer frame, incluso si el foco aún no está en un componente Swing.
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(event -> {
            if (event.getID() == KeyEvent.KEY_PRESSED && event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                frame.dispose();
                System.exit(0);
                return true;
            }
            return false;
        });

        // Muestra la ventana
        frame.setVisible(true);
    }
}
