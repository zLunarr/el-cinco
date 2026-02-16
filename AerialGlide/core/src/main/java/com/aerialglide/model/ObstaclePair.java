package com.aerialglide.model;

public class ObstaclePair {
    public int x;
    public int topHeight;
    public int bottomY;
    public int bottomHeight;
    public boolean scored1;
    public boolean scored2;

    public ObstaclePair(int x, int topHeight, int bottomY, int bottomHeight) {
        this.x = x;
        this.topHeight = topHeight;
        this.bottomY = bottomY;
        this.bottomHeight = bottomHeight;
    }
}
