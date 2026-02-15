package juegojava;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class Obstaculos {
    private int x;
    private final int y;
    private final int width;
    private final int height;
    private final Image imagen;

    public Obstaculos(int x, int y, int width, int height, String rutaImagen) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.imagen = ResourceLoader.loadImage(rutaImagen);
    }

    public void mover(int velocidad) {
        x -= velocidad;
    }

    public boolean fueraDePantalla() {
        return x + width < 0;
    }

    public void dibujar(Graphics g) {
        if (imagen != null) {
            g.drawImage(imagen, x, y, width, height, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
