package juegojava;

import java.awt.Rectangle;

public final class CollisionBoxes {
    private static final int PLAYER_HITBOX_MARGIN_X = 18;
    private static final int PLAYER_HITBOX_MARGIN_Y = 14;

    private CollisionBoxes() {
    }

    public static Rectangle playerBounds(int x, int y, int width, int height) {
        int hitboxX = x + PLAYER_HITBOX_MARGIN_X;
        int hitboxY = y + PLAYER_HITBOX_MARGIN_Y;
        int hitboxWidth = Math.max(1, width - (PLAYER_HITBOX_MARGIN_X * 2));
        int hitboxHeight = Math.max(1, height - (PLAYER_HITBOX_MARGIN_Y * 2));
        return new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }

    public static Rectangle obstacleBounds(int x, int y, int width, int height) {
        return new Rectangle(x, y, width, height);
    }
}
