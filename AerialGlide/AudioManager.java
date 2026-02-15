package juegojava;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class AudioManager {
    private static final String MUSICA_PRINCIPAL = "Resources/Juego 35.wav";
    private static Clip clipActual;
    private static String pistaActual;
    private static boolean sonidoActivado = true;
    private static int volumenPorcentaje = 70;

    private AudioManager() {
    }

    public static synchronized boolean isSoundEnabled() {
        return sonidoActivado;
    }

    public static synchronized int getVolumePercent() {
        return volumenPorcentaje;
    }

    public static synchronized void setSoundEnabled(boolean activar) {
        sonidoActivado = activar;
        if (!sonidoActivado) {
            stopAllAudio();
        } else {
            playMainLoop();
        }
    }

    public static synchronized void setVolumePercent(int volumen) {
        volumenPorcentaje = Math.max(0, Math.min(100, volumen));
        aplicarVolumen();
    }

    public static synchronized void playMainLoop() {
        playLoop(MUSICA_PRINCIPAL);
    }

    public static synchronized void playLoop(String ruta) {
        if (!sonidoActivado) {
            return;
        }

        if (clipActual != null && clipActual.isRunning() && ruta.equals(pistaActual)) {
            aplicarVolumen();
            return;
        }

        stopAllAudio();

        try {
            File musicaArchivo = ResourceLoader.findFile(ruta);
            if (musicaArchivo == null) {
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicaArchivo);
            clipActual = AudioSystem.getClip();
            clipActual.open(audioStream);
            pistaActual = ruta;
            aplicarVolumen();
            clipActual.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ignored) {
            clipActual = null;
            pistaActual = null;
        }
    }

    public static synchronized void stopAllAudio() {
        if (clipActual != null) {
            clipActual.stop();
            clipActual.close();
        }
        clipActual = null;
        pistaActual = null;
    }

    private static void aplicarVolumen() {
        if (clipActual == null || !clipActual.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl control = (FloatControl) clipActual.getControl(FloatControl.Type.MASTER_GAIN);
        if (!sonidoActivado || volumenPorcentaje <= 0) {
            control.setValue(control.getMinimum());
            return;
        }

        float min = control.getMinimum();
        float max = control.getMaximum();
        float gain = min + (max - min) * (volumenPorcentaje / 100f);
        control.setValue(Math.max(min, Math.min(max, gain)));
    }
}
