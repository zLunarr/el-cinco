package com.aerialglide;

public final class GameConfig {
    public static final int WORLD_WIDTH = 1600;
    public static final int WORLD_HEIGHT = 900;
    public static final int PLAYER_X = 220;
    public static final int PLAYER_SIZE = 90;
    public static final int JUMP_SPEED = -15;
    public static final int GRAVITY = 1;
    public static final int OBSTACLE_WIDTH = 120;
    public static final int OBSTACLE_SPEED = 10;
    public static final int OBSTACLE_GAP = 210;
    public static final int OBSTACLE_SPAWN_TICKS = 80;
    public static final int DEFAULT_PORT = 5000;
    public static final float TICK_SECONDS = 0.02f;

    private GameConfig() {
    }
}
