package com.aerialglide.screens;

import com.aerialglide.AerialGlideGame;
import com.aerialglide.GameConfig;
import com.aerialglide.net.ServerState;
import com.aerialglide.net.TcpGameClient;
import com.aerialglide.net.TcpGameServer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import java.io.IOException;

public class OnlineGameScreen extends ScreenAdapter {
    private final AerialGlideGame game;
    private final TcpGameServer ownedServer;
    private TcpGameClient client;
    private final Texture bg = new Texture("fondo juego.jpg");
    private final Texture bird1 = new Texture("bird.png");
    private final Texture bird2 = new Texture("bird2.png");
    private final Texture topObs = new Texture("obstacle - copia.png");
    private final Texture bottomObs = new Texture("obstacle.png");
    private final BitmapFont font = new BitmapFont();

    private final String host;
    private final int port;
    private final String username;
    private int localIndex = 0;
    private boolean connected;
    private boolean disconnected;
    private ServerState state;

    public OnlineGameScreen(AerialGlideGame game, String host, int port, String username, TcpGameServer ownedServer) {
        this.game = game;
        this.host = host;
        this.port = port;
        this.username = username;
        this.ownedServer = ownedServer;
        connect();
    }

    private void connect() {
        try {
            client = new TcpGameClient(host, port, username);
            client.connect();
            connected = true;
        } catch (IOException e) {
            disconnected = true;
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (disconnected) {
                game.setScreen(new MainMenuScreen(game));
                return;
            }
            disconnectAndBack();
            return;
        }

        if (connected) {
            String msg;
            while ((msg = client.poll()) != null) {
                if (msg.startsWith("ASSIGN|")) {
                    localIndex = Integer.parseInt(msg.split("\\|")[1]) - 1;
                } else if (msg.startsWith("STATE|")) {
                    state = ServerState.parse(msg);
                } else if ("DISCONNECTED".equals(msg)) {
                    disconnected = true;
                }
            }
        }

        if (!disconnected && state != null && !state.roundOver && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            client.send("JUMP");
        }
        if (!disconnected && state != null && state.roundOver && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            client.send("READY");
        }

        draw();
    }

    private void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (state != null) {
            for (var o : state.obstacles) {
                game.batch.draw(topObs, sx(o.x), sy(GameConfig.WORLD_HEIGHT - o.topHeight), sw(GameConfig.OBSTACLE_WIDTH), sh(o.topHeight));
                game.batch.draw(bottomObs, sx(o.x), 0, sw(GameConfig.OBSTACLE_WIDTH), sh(o.bottomHeight));
            }
            int localY = localIndex == 0 ? state.p1Y : state.p2Y;
            int remoteY = localIndex == 0 ? state.p2Y : state.p1Y;
            game.batch.draw(bird1, sx(GameConfig.PLAYER_X), sy(GameConfig.WORLD_HEIGHT - localY - GameConfig.PLAYER_SIZE), sw(GameConfig.PLAYER_SIZE), sh(GameConfig.PLAYER_SIZE));
            game.batch.draw(bird2, sx(GameConfig.PLAYER_X), sy(GameConfig.WORLD_HEIGHT - remoteY - GameConfig.PLAYER_SIZE), sw(GameConfig.PLAYER_SIZE), sh(GameConfig.PLAYER_SIZE));

            int localScore = localIndex == 0 ? state.p1Score : state.p2Score;
            int remoteScore = localIndex == 0 ? state.p2Score : state.p1Score;
            font.draw(game.batch, "Tu puntaje: " + localScore, 20, Gdx.graphics.getHeight() - 20);
            font.draw(game.batch, "Rival: " + remoteScore, 20, Gdx.graphics.getHeight() - 50);
            if (state.roundOver) {
                String t = state.roundResult == 3 ? "Empate" : ((localIndex == 0 && state.roundResult == 1) || (localIndex == 1 && state.roundResult == 2) ? "Ganaste" : "Perdiste");
                font.setColor(Color.YELLOW);
                font.draw(game.batch, t + " - R para listo/reiniciar", 20, Gdx.graphics.getHeight() - 80);
                font.setColor(Color.WHITE);
            }
        }
        if (disconnected) {
            font.setColor(Color.RED);
            font.draw(game.batch, "Desconectado. ESC para volver al menu", 20, Gdx.graphics.getHeight() - 110);
            font.setColor(Color.WHITE);
        }
        font.draw(game.batch, "SPACE saltar", 20, Gdx.graphics.getHeight() - 140);
        game.batch.end();
    }

    private void disconnectAndBack() {
        if (client != null) {
            client.send("DISCONNECT");
            client.close();
        }
        if (ownedServer != null) ownedServer.stop();
        game.setScreen(new MainMenuScreen(game));
    }

    private float sx(float x) { return x * Gdx.graphics.getWidth() / (float) GameConfig.WORLD_WIDTH; }
    private float sy(float y) { return y * Gdx.graphics.getHeight() / (float) GameConfig.WORLD_HEIGHT; }
    private float sw(float w) { return w * Gdx.graphics.getWidth() / (float) GameConfig.WORLD_WIDTH; }
    private float sh(float h) { return h * Gdx.graphics.getHeight() / (float) GameConfig.WORLD_HEIGHT; }

    @Override
    public void dispose() {
        if (client != null) client.close();
        if (ownedServer != null) ownedServer.stop();
        bg.dispose();bird1.dispose();bird2.dispose();topObs.dispose();bottomObs.dispose();font.dispose();
    }
}
