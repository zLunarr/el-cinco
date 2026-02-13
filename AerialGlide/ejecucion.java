package juegojava;

import java.awt.*;
import javax.swing.*;

public class Ejecucion {
    public static void main(String[] args) {
        // Crear la ventana del juego
        JFrame frame = new JFrame();
        // Crear el panel del menú de inicio
        MenuPanel menuPanel = new MenuPanel(frame);

        // Agregar el panel del menú a la ventana
        frame.add(menuPanel);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack(); // Ajusta el tamaño de la ventana a la del panel
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Título de la ventana
        frame.setTitle("Aerial Glide - Menú de Inicio");
        // Cerrar la aplicación al salir
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Evita que la ventana sea redimensionable
        frame.setResizable(false);
        // Centra la ventana en la pantalla
        frame.setLocationRelativeTo(null);
        // Muestra la ventana
        frame.setVisible(true);
    }
}
