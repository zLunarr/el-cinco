package com.aerialglide;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.aerialglide.audio.AudioManager;
import com.aerialglide.screens.MainMenuScreen;

public class AerialGlideGame extends Game {
    public SpriteBatch batch;
    public AudioManager audio;

    @Override
    public void create() {
        batch = new SpriteBatch();
        audio = new AudioManager();
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        if (screen != null) {
            screen.dispose();
        }
        audio.dispose();
        batch.dispose();
    }
}
