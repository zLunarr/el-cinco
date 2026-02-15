package juegojava;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class Obstaculos {
    private static final int COLLISION_MARGIN_X = 8;
    private static final int COLLISION_MARGIN_TOP = 6;
    private static final int COLLISION_MARGIN_BOTTOM_TOP_PIPE = 28;
    private static final int COLLISION_MARGIN_TOP_BOTTOM_PIPE = 12;
    private int x;
    private final int y;
    private final int width;
    private final int height;
    private final Image imagen;
    private boolean puntuado;

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

    public int getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }

    public int getY() {
        return y;
    }

    public boolean isPuntuado() {
        return puntuado;
    }

    public void marcarPuntuado() {
        puntuado = true;
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
        int hitboxX = x + COLLISION_MARGIN_X;
        int hitboxWidth = Math.max(1, width - (COLLISION_MARGIN_X * 2));

        if (y == 0) {
            int hitboxY = y + COLLISION_MARGIN_TOP;
            int hitboxHeight = Math.max(1, height - COLLISION_MARGIN_TOP - COLLISION_MARGIN_BOTTOM_TOP_PIPE);
            return new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
        }

        int hitboxY = y + COLLISION_MARGIN_TOP_BOTTOM_PIPE;
        int hitboxHeight = Math.max(1, height - COLLISION_MARGIN_TOP_BOTTOM_PIPE);
        return new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }
}
