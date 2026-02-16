package juegojava;

import java.awt.Dimension;
import javax.swing.JFrame;

public class Ejecucion {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        MenuPanel menuPanel = new MenuPanel(frame);

        frame.setUndecorated(true);
        frame.add(menuPanel);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setIconImage(ResourceLoader.loadImage("Resources/icono.jpg"));
        frame.setTitle("Aerial Glide - Menú de Inicio");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
