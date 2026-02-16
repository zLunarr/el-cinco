package com.aerialglide.net;

import com.aerialglide.GameConfig;
import com.aerialglide.model.CollisionBoxes;
import com.aerialglide.model.ObstaclePair;
import com.aerialglide.model.PlayerState;
import com.badlogic.gdx.math.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TcpGameServer {
    private final int port;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final PlayerState[] players = {new PlayerState(), new PlayerState()};
    private final List<ObstaclePair> obstacles = new ArrayList<>();
    private volatile boolean running;
    private boolean roundOver;
    private int roundResult;
    private int ticks;

    public TcpGameServer(int port) {
        this.port = port;
    }

    public void start() {
        running = true;
        Thread serverThread = new Thread(this::acceptLoop, "tcp-server-accept");
        Thread gameThread = new Thread(this::gameLoop, "tcp-server-game");
        serverThread.start();
        gameThread.start();
    }

    private void acceptLoop() {
        try (ServerSocket ss = new ServerSocket(port)) {
            while (running) {
                Socket socket = ss.accept();
                synchronized (this) {
                    if (clients.size() >= 2) {
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println("FULL");
                        socket.close();
                        continue;
                    }
                    ClientHandler handler = new ClientHandler(this, socket, clients.size());
                    clients.add(handler);
                    new Thread(handler, "tcp-client-" + clients.size()).start();
                }
            }
        } catch (IOException e) {
            if (running) throw new RuntimeException(e);
        }
    }

    private void gameLoop() {
        long last = System.currentTimeMillis();
        while (running) {
            if (System.currentTimeMillis() - last >= 20) {
                synchronized (this) {
                    updateTick();
                }
                last += 20;
            }
            try {
                Thread.sleep(2);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void updateTick() {
        if (clients.size() < 2) {
            broadcast("WAITING");
            return;
        }
        if (!roundOver) {
            ticks++;
            updatePlayer(0);
            updatePlayer(1);
            moveObstacles();
            if (ticks % GameConfig.OBSTACLE_SPAWN_TICKS == 0) spawnObstacle();
            collisionsAndScore();
            if (!players[0].alive || !players[1].alive) {
                roundOver = true;
                roundResult = !players[0].alive && !players[1].alive ? 3 : (players[0].alive ? 1 : 2);
            }
        }
        broadcast(buildState());
    }

    private void updatePlayer(int i) {
        PlayerState p = players[i];
        if (!p.alive) {
            p.jumpQueued = false;
            return;
        }
        if (p.jumpQueued) p.velocity = GameConfig.JUMP_SPEED;
        p.jumpQueued = false;
        p.y += p.velocity;
        p.velocity += GameConfig.GRAVITY;
        if (p.y + GameConfig.PLAYER_SIZE >= GameConfig.WORLD_HEIGHT) {
            p.y = GameConfig.WORLD_HEIGHT - GameConfig.PLAYER_SIZE;
            p.velocity = 0;
            p.alive = false;
        }
        if (p.y < 0) {
            p.y = 0;
            if (p.velocity < 0) p.velocity = 0;
        }
    }

    private void moveObstacles() {
        obstacles.forEach(o -> o.x -= GameConfig.OBSTACLE_SPEED);
        obstacles.removeIf(o -> o.x + GameConfig.OBSTACLE_WIDTH < 0);
    }

    private void spawnObstacle() {
        int maxTop = Math.max(1, GameConfig.WORLD_HEIGHT - GameConfig.OBSTACLE_GAP - 120);
        int top = (int) (Math.random() * maxTop) + 60;
        int by = top + GameConfig.OBSTACLE_GAP;
        obstacles.add(new ObstaclePair(GameConfig.WORLD_WIDTH, top, by, GameConfig.WORLD_HEIGHT - by));
    }

    private void collisionsAndScore() {
        Rectangle[] pb = {
                CollisionBoxes.playerBounds(GameConfig.PLAYER_X, players[0].y, GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE),
                CollisionBoxes.playerBounds(GameConfig.PLAYER_X, players[1].y, GameConfig.PLAYER_SIZE, GameConfig.PLAYER_SIZE)
        };
        for (ObstaclePair o : obstacles) {
            Rectangle top = CollisionBoxes.obstacleBounds(o.x, 0, GameConfig.OBSTACLE_WIDTH, o.topHeight);
            Rectangle bottom = CollisionBoxes.obstacleBounds(o.x, o.bottomY, GameConfig.OBSTACLE_WIDTH, o.bottomHeight);
            for (int i = 0; i < 2; i++) {
                if (!players[i].alive) continue;
                if (pb[i].overlaps(top) || pb[i].overlaps(bottom)) {
                    players[i].alive = false;
                    continue;
                }
                if (pb[i].x > o.x + GameConfig.OBSTACLE_WIDTH) {
                    if (i == 0 && !o.scored1) {
                        o.scored1 = true;
                        players[i].score++;
                    }
                    if (i == 1 && !o.scored2) {
                        o.scored2 = true;
                        players[i].score++;
                    }
                }
            }
        }
    }

    public synchronized void onMessage(int idx, String msg) {
        switch (msg) {
            case "JUMP" -> players[idx].jumpQueued = true;
            case "READY" -> {
                players[idx].ready = true;
                if (players[0].ready && players[1].ready) resetRound();
            }
            case "DISCONNECT" -> disconnect(idx);
            default -> {
            }
        }
    }

    private void resetRound() {
        obstacles.clear();
        for (PlayerState p : players) {
            p.y = 250;
            p.velocity = 0;
            p.alive = true;
            p.score = 0;
            p.ready = false;
            p.jumpQueued = false;
        }
        ticks = 0;
        roundOver = false;
        roundResult = 0;
    }

    private String buildState() {
        StringBuilder sb = new StringBuilder("STATE");
        sb.append('|').append(roundOver);
        sb.append('|').append(roundResult);
        sb.append('|').append(players[0].y).append('|').append(players[0].velocity).append('|').append(players[0].alive).append('|').append(players[0].score);
        sb.append('|').append(players[1].y).append('|').append(players[1].velocity).append('|').append(players[1].alive).append('|').append(players[1].score);
        sb.append('|').append(players[0].ready).append('|').append(players[1].ready);
        sb.append('|').append(obstacles.size());
        for (ObstaclePair o : obstacles) {
            sb.append('|').append(o.x).append('|').append(o.topHeight).append('|').append(o.bottomY).append('|').append(o.bottomHeight);
        }
        return sb.toString();
    }

    public synchronized void onHello(int idx, String name) {
        broadcast("ASSIGN|" + (idx + 1) + "|" + name);
    }

    public synchronized void disconnect(int idx) {
        if (idx < clients.size()) {
            clients.get(idx).close();
            clients.remove(idx);
        }
        resetRound();
        broadcast("DISCONNECTED");
    }

    private void broadcast(String message) {
        for (ClientHandler c : new ArrayList<>(clients)) {
            c.send(message);
        }
    }

    public void stop() {
        running = false;
        synchronized (this) {
            for (ClientHandler c : clients) c.close();
            clients.clear();
        }
    }

    private static class ClientHandler implements Runnable {
        private final TcpGameServer server;
        private final Socket socket;
        private final int index;
        private BufferedReader in;
        private PrintWriter out;

        ClientHandler(TcpGameServer server, Socket socket, int index) {
            this.server = server;
            this.socket = socket;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("HELLO|")) server.onHello(index, line.substring(6));
                    else server.onMessage(index, line);
                }
            } catch (IOException ignored) {
            } finally {
                server.disconnect(index);
            }
        }

        synchronized void send(String message) {
            if (out != null) out.println(message);
        }

        synchronized void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
