package com.aerialglide.screens;

import com.aerialglide.AerialGlideGame;
import com.aerialglide.GameConfig;
import com.aerialglide.net.TcpGameServer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class MainMenuScreen extends ScreenAdapter {
    private final AerialGlideGame game;
    private final BitmapFont font = new BitmapFont();
    private final Texture bg = new Texture("background.png");
    private final Texture title = new Texture("menu_title.png");
    private String ipBuffer = "192.168.";

    public MainMenuScreen(AerialGlideGame game) {
        this.game = game;
        game.audio.playMusicLoop("Juego 35.wav");
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            game.setScreen(new OfflineGameScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            TcpGameServer server = new TcpGameServer(GameConfig.DEFAULT_PORT);
            server.start();
            game.setScreen(new OnlineGameScreen(game, "127.0.0.1", GameConfig.DEFAULT_PORT, "Servidor", server));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            game.setScreen(new OnlineGameScreen(game, ipBuffer, GameConfig.DEFAULT_PORT, "Cliente", null));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            game.setScreen(new OptionsScreen(game, this));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && !ipBuffer.isEmpty()) {
            ipBuffer = ipBuffer.substring(0, ipBuffer.length() - 1);
        }
        for (int i = Input.Keys.NUM_0; i <= Input.Keys.NUM_9; i++) {
            if (Gdx.input.isKeyJustPressed(i)) ipBuffer += (char) ('0' + i - Input.Keys.NUM_0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)) ipBuffer += '.';

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.draw(title, 40, Gdx.graphics.getHeight() - 220, 520, 200);
        font.draw(game.batch, "1) Offline", 80, 300);
        font.draw(game.batch, "2) Online Host (server + cliente)", 80, 260);
        font.draw(game.batch, "3) Online Cliente", 80, 220);
        font.draw(game.batch, "4) Opciones", 80, 180);
        font.draw(game.batch, "ESC salir", 80, 140);
        font.draw(game.batch, "IP cliente (tecla numeros y punto): " + ipBuffer, 80, 100);
        font.draw(game.batch, "Puerto fijo: " + GameConfig.DEFAULT_PORT, 80, 70);
        game.batch.end();
    }

    @Override
    public void dispose() {
        font.dispose();
        bg.dispose();
        title.dispose();
    }
}
