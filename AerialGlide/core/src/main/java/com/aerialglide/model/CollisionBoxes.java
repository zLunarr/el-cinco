package com.aerialglide.model;

import com.badlogic.gdx.math.Rectangle;

public final class CollisionBoxes {
    private static final int PLAYER_MARGIN_X = 18;
    private static final int PLAYER_MARGIN_Y = 14;

    private CollisionBoxes() {}

    public static Rectangle playerBounds(float x, float y, float width, float height) {
        return new Rectangle(
                x + PLAYER_MARGIN_X,
                y + PLAYER_MARGIN_Y,
                Math.max(1, width - PLAYER_MARGIN_X * 2),
                Math.max(1, height - PLAYER_MARGIN_Y * 2)
        );
    }

    public static Rectangle obstacleBounds(float x, float y, float width, float height) {
        return new Rectangle(x, y, width, height);
    }
}
