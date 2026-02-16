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
    private final Image imagen;
    private final Color tinte;
    private final int gravedad = 1;

    public Personaje(int x, int y) {
        this(x, y, "Resources/bird.png", null);
    }

    public Personaje(int x, int y, String rutaImagen) {
        this(x, y, rutaImagen, null);
    }

    public Personaje(int x, int y, String rutaImagen, Color tinte) {
        this.x = x;
        this.y = y;
        this.velocidad = 0;
        Image sprite = ResourceLoader.loadImage(rutaImagen);
        if (sprite == null && !"Resources/bird.png".equals(rutaImagen)) {
            sprite = ResourceLoader.loadImage("Resources/bird.png");
        }
        this.imagen = sprite;
        this.tinte = tinte;
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
            if (tinte != null) {
                g.setColor(tinte);
                g.fillOval(x + 10, y + 10, ancho - 20, alto - 20);
            }
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval(x, y, ancho, alto);
        }
    }

    public Rectangle getBounds() {
        return CollisionBoxes.playerBounds(x, y, ancho, alto);
    }

    public boolean tocoSuelo(int panelHeight) {
        return y + alto >= panelHeight;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
