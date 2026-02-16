package com.aerialglide.net;

import com.aerialglide.model.ObstaclePair;
import java.util.ArrayList;
import java.util.List;

public class ServerState {
    public boolean roundOver;
    public int roundResult;
    public int p1Y;
    public int p1Velocity;
    public boolean p1Alive;
    public int p1Score;
    public int p2Y;
    public int p2Velocity;
    public boolean p2Alive;
    public int p2Score;
    public boolean p1Ready;
    public boolean p2Ready;
    public List<ObstaclePair> obstacles = new ArrayList<>();

    public static ServerState parse(String line) {
        String[] p = line.split("\\|");
        if (!"STATE".equals(p[0])) {
            throw new IllegalArgumentException("Invalid state: " + line);
        }
        ServerState s = new ServerState();
        s.roundOver = Boolean.parseBoolean(p[1]);
        s.roundResult = Integer.parseInt(p[2]);
        s.p1Y = Integer.parseInt(p[3]);
        s.p1Velocity = Integer.parseInt(p[4]);
        s.p1Alive = Boolean.parseBoolean(p[5]);
        s.p1Score = Integer.parseInt(p[6]);
        s.p2Y = Integer.parseInt(p[7]);
        s.p2Velocity = Integer.parseInt(p[8]);
        s.p2Alive = Boolean.parseBoolean(p[9]);
        s.p2Score = Integer.parseInt(p[10]);
        s.p1Ready = Boolean.parseBoolean(p[11]);
        s.p2Ready = Boolean.parseBoolean(p[12]);
        int count = Integer.parseInt(p[13]);
        int idx = 14;
        for (int i = 0; i < count; i++) {
            int x = Integer.parseInt(p[idx++]);
            int top = Integer.parseInt(p[idx++]);
            int by = Integer.parseInt(p[idx++]);
            int bh = Integer.parseInt(p[idx++]);
            s.obstacles.add(new ObstaclePair(x, top, by, bh));
        }
        return s;
    }
}
