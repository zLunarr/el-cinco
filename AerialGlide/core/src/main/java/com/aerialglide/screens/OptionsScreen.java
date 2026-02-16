package com.aerialglide.screens;

import com.aerialglide.AerialGlideGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class OptionsScreen extends ScreenAdapter {
    private final AerialGlideGame game;
    private final Screen backScreen;
    private final BitmapFont font = new BitmapFont();

    public OptionsScreen(AerialGlideGame game, Screen backScreen) {
        this.game = game;
        this.backScreen = backScreen;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(backScreen);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.audio.setEnabled(!game.audio.isEnabled());
            if (game.audio.isEnabled()) game.audio.playMusicLoop("Juego 35.wav");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) game.audio.setVolume(game.audio.getVolume() + 0.05f);
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) game.audio.setVolume(game.audio.getVolume() - 0.05f);

        Gdx.gl.glClearColor(0.07f, 0.08f, 0.12f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        font.draw(game.batch, "Opciones de Audio", 80, Gdx.graphics.getHeight() - 80);
        font.draw(game.batch, "M: activar/desactivar sonido -> " + game.audio.isEnabled(), 80, Gdx.graphics.getHeight() - 120);
        font.draw(game.batch, "UP/DOWN volumen -> " + Math.round(game.audio.getVolume() * 100) + "%", 80, Gdx.graphics.getHeight() - 150);
        font.draw(game.batch, "ESC volver", 80, Gdx.graphics.getHeight() - 180);
        game.batch.end();
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}
