package com.aerialglide.screens;

import com.aerialglide.AerialGlideGame;
import com.aerialglide.GameConfig;
import com.aerialglide.model.CollisionBoxes;
import com.aerialglide.model.ObstaclePair;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class OfflineGameScreen extends ScreenAdapter {
    private final AerialGlideGame game;
    private final Texture bg = new Texture("fondo juego.jpg");
    private final Texture bird = new Texture("bird.png");
    private final Texture topObs = new Texture("obstacle - copia.png");
    private final Texture bottomObs = new Texture("obstacle.png");
    private final BitmapFont font = new BitmapFont();
    private final List<ObstaclePair> obstacles = new ArrayList<>();
    private int y = 250;
    private int vel = 0;
    private int ticks = 0;
    private int score = 0;
    private boolean alive = true;

    public OfflineGameScreen(AerialGlideGame game) {
        this.game = game;
        game.audio.playMusicLoop("Juego 35.wav");
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            return;
        }
        if (alive && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) vel = GameConfig.JUMP_SPEED;
        if (!alive && Gdx.input.isKeyJustPressed(Input.Keys.R)) reset();

        if (alive) update();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.batch.draw(bg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        for (ObstaclePair o : obstacles) {
            game.batch.draw(topObs, scaleX(o.x), scaleY(GameConfig.WORLD_HEIGHT - o.topHeight), scaleW(GameConfig.OBSTACLE_WIDTH), scaleH(o.topHeight));
            game.batch.draw(bottomObs, scaleX(o.x), 0, scaleW(GameConfig.OBSTACLE_WIDTH), scaleH(o.bottomHeight));
        }
        game.batch.draw(bird, scaleX(GameConfig.PLAYER_X), scaleY(GameConfig.WORLD_HEIGHT - y - GameConfig.PLAYER_SIZE), scaleW(GameConfig.PLAYER_SIZE), scaleH(GameConfig.PLAYER_SIZE));
        font.setColor(Color.WHITE);
        font.draw(game.batch, "Puntaje: " + score, 20, Gdx.graphics.getHeight() - 20);
        font.draw(game.batch, "SPACE saltar", 20, Gdx.graphics.getHeight() - 50);
        if (!alive) font.draw(game.batch, "GAME OVER - R reiniciar", 20, Gdx.graphics.getHeight() - 80);
        game.batch.end();
    }

    private void update() {
        ticks++;
        y += vel;
        vel += GameConfig.GRAVITY;
        if (y < 0) {
            y = 0;
            if (vel < 0) vel = 0;
        }
        if (y + GameConfig.PLAYER_SIZE >= GameConfig.WORLD_HEIGHT) {
            y = GameConfig.WORLD_HEIGHT - GameConfig.PLAYER_SIZE;
            vel = 0;
            alive = false;
        }
        obstacles.forEach(o -> o.x -= GameConfig.OBSTACLE_SPEED);
        obstacles.removeIf(o -> o.x + GameConfig.OBSTACLE_WIDTH < 0);
        if (ticks % GameConfig.OBSTACLE_SPAWN_TICKS == 0) {
            int maxTop = Math.max(1, GameConfig.WORLD_HEIGHT - GameConfig.OBSTACLE_GAP - 120);
            int top = (int) (Math.random() * maxTop) + 60;
            int by = top + GameConfig.OBSTACLE_GAP;
            obstacles.add(new ObstaclePair(GameConfig.WORLD_WIDTH, top, by, GameConfig.WORLD_HEIGHT - by));
        }
        Rectangle player = CollisionBoxes.playerBounds(GameConfig.PLAYER_X, y, GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE);
        for (ObstaclePair o : obstacles) {
            Rectangle top = CollisionBoxes.obstacleBounds(o.x, 0, GameConfig.OBSTACLE_WIDTH, o.topHeight);
            Rectangle bottom = CollisionBoxes.obstacleBounds(o.x, o.bottomY, GameConfig.OBSTACLE_WIDTH, o.bottomHeight);
            if (player.overlaps(top) || player.overlaps(bottom)) alive = false;
            if (player.x > o.x + GameConfig.OBSTACLE_WIDTH && !o.scored1) {
                o.scored1 = true;
                score++;
            }
        }
    }

    private void reset() {
        y = 250;
        vel = 0;
        ticks = 0;
        score = 0;
        alive = true;
        obstacles.clear();
    }

    private float scaleX(float x) { return x * Gdx.graphics.getWidth() / (float) GameConfig.WORLD_WIDTH; }
    private float scaleY(float y) { return y * Gdx.graphics.getHeight() / (float) GameConfig.WORLD_HEIGHT; }
    private float scaleW(float w) { return w * Gdx.graphics.getWidth() / (float) GameConfig.WORLD_WIDTH; }
    private float scaleH(float h) { return h * Gdx.graphics.getHeight() / (float) GameConfig.WORLD_HEIGHT; }

    @Override
    public void dispose() {
        bg.dispose();bird.dispose();topObs.dispose();bottomObs.dispose();font.dispose();
    }
}
