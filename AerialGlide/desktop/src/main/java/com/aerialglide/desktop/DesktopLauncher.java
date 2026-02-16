package com.aerialglide.desktop;

import com.aerialglide.AerialGlideGame;
import com.aerialglide.net.TcpGameServer;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        String mode = arg(args, "--mode", "game");
        int port = Integer.parseInt(arg(args, "--port", "5000"));

        if ("server".equalsIgnoreCase(mode)) {
            TcpGameServer server = new TcpGameServer(port);
            server.start();
            System.out.println("Servidor TCP activo en puerto " + port + ". Ctrl+C para cerrar.");
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            server.stop();
            return;
        }

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Aerial Glide LibGDX");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        new Lwjgl3Application(new AerialGlideGame(), config);
    }

    private static String arg(String[] args, String key, String defaultValue) {
        for (String arg : args) {
            if (arg.startsWith(key + "=")) {
                return arg.substring((key + "=").length());
            }
        }
        return defaultValue;
    }
}
