package juegojava;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.Rectangle;

public class Personaje {
    private int x, y, velocidad;
    private final int ancho = 90, alto = 90;
    private final Image imagen;
    private final int gravedad = 1;  // Constante para simular la gravedad

    public Personaje(int x, int y) {
        this.x = x;
        this.y = y;
        this.velocidad = 0; // Inicia estático
        this.imagen = new ImageIcon("C:/JAVA/juegojava/src/Resources/bird.png").getImage();
    }

     //Actualiza la posición y velocidad del personaje.
     //panelHeight Altura del panel para limitar el movimiento del personaje.
    public void update(int panelHeight) {
        y += velocidad; // Actualizar posición
        velocidad += gravedad; // Aplicar gravedad

        // Verificar si el personaje toca el suelo
        if (y + alto >= panelHeight) {
            y = panelHeight - alto; // Ajustar posición para no salir del panel
            if (velocidad > 0) {
                velocidad = 0;     // Detener caída solo si iba descendiendo
            }
        }

        // Verificar si el personaje toca el límite superior
        if (y < 0) {
            y = 0;       // No permitir que salga por arriba
            if (velocidad < 0) {
                velocidad = 0; // Detener ascenso
            }
        }
    }

    public void jump() {
        velocidad = -15; // Valor negativo para moverse hacia arriba
    }
    
    //Dibuja el personaje en pantalla.
    public void draw(Graphics g) {
        g.drawImage(imagen, x, y, ancho, alto, null);
    }

    //Devuelve los límites del personaje para detección de colisiones.
	//Un rectángulo que representa los límites del personaje.
    public Rectangle getBounds() {
        return new Rectangle(x, y, ancho, alto);
    }
}
