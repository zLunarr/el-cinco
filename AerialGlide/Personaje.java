package juegojava;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class Personaje {
    private int x;
    private int y;
    private int velocidad;
    private final int ancho = 90;
    private final int alto = 90;
    private static final int HITBOX_MARGIN_X = 18;
    private static final int HITBOX_MARGIN_Y = 14;
    private final Image imagen;
    private final int gravedad = 1;

    public Personaje(int x, int y) {
        this.x = x;
        this.y = y;
        this.velocidad = 0;
        this.imagen = ResourceLoader.loadImage("Resources/bird.png");
    }

    public void update(int panelHeight) {
        y += velocidad;
        velocidad += gravedad;

        if (y + alto >= panelHeight) {
            y = panelHeight - alto;
            if (velocidad > 0) {
                velocidad = 0;
            }
        }

        if (y < 0) {
            y = 0;
            if (velocidad < 0) {
                velocidad = 0;
            }
        }
    }

    public void jump() {
        velocidad = -15;
    }

    public void draw(Graphics g) {
        if (imagen != null) {
            g.drawImage(imagen, x, y, ancho, alto, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval(x, y, ancho, alto);
        }
    }

    public Rectangle getBounds() {
        int hitboxX = x + HITBOX_MARGIN_X;
        int hitboxY = y + HITBOX_MARGIN_Y;
        int hitboxWidth = Math.max(1, ancho - (HITBOX_MARGIN_X * 2));
        int hitboxHeight = Math.max(1, alto - (HITBOX_MARGIN_Y * 2));
        return new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
