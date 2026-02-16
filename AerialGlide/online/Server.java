package online;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private static final int PORT = 5555;
    private static final int TICK_MS = 20;
    private static final int WORLD_HEIGHT = 900;
    private static final int PLAYER_SPAWN_Y = 250;
    private static final int PLAYER_X = 220;
    private static final int PLAYER_WIDTH = 90;
    private static final int PLAYER_HEIGHT = 90;
    private static final int PLAYER_HITBOX_MARGIN_X = 18;
    private static final int PLAYER_HITBOX_MARGIN_Y = 14;
    private static final int GRAVITY = 1;
    private static final int JUMP_SPEED = -15;

    private static final int OBSTACLE_WIDTH = 120;
    private static final int OBSTACLE_SPEED = 10;
    private static final int OBSTACLE_GAP = 210;
    private static final int OBSTACLE_SPAWN_EVERY_TICKS = 80;

    private static final int COLLISION_MARGIN_X = 8;
    private static final int COLLISION_MARGIN_TOP = 6;
    private static final int COLLISION_MARGIN_BOTTOM_TOP_PIPE = 28;
    private static final int COLLISION_MARGIN_TOP_BOTTOM_PIPE = 12;

    private static Server instance;

    private static class Peer {
        String username;
        InetAddress ip;
        int port;

        Peer(String username, InetAddress ip, int port) {
            this.username = username;
            this.ip = ip;
            this.port = port;
        }
    }

    private static class ObstaclePair {
        int x;
        int topHeight;
        int bottomY;
        int bottomHeight;
        boolean scoredP1;
        boolean scoredP2;

        ObstaclePair(int x, int topHeight, int bottomY, int bottomHeight) {
            this.x = x;
            this.topHeight = topHeight;
            this.bottomY = bottomY;
            this.bottomHeight = bottomHeight;
        }
    }

    private final List<Peer> peers;
    private final List<ObstaclePair> obstacles;
    private DatagramSocket socket;
    private volatile boolean end;

    private final int[] playerY;
    private final int[] playerVelocity;
    private final boolean[] playerAlive;
    private final int[] playerScore;
    private final boolean[] jumpQueued;
    private final boolean[] playerReady;
    private boolean restartPending;

    private int tickCounter;
    private boolean roundOver;
    private int roundResult;
    private boolean stateDirty;

    public Server() {
        this.peers = new ArrayList<>();
        this.obstacles = new ArrayList<>();
        this.playerY = new int[]{PLAYER_SPAWN_Y, PLAYER_SPAWN_Y};
        this.playerVelocity = new int[]{0, 0};
        this.playerAlive = new boolean[]{true, true};
        this.playerScore = new int[]{0, 0};
        this.jumpQueued = new boolean[]{false, false};
        this.playerReady = new boolean[]{false, false};
        this.restartPending = false;

        try {
            this.socket = new DatagramSocket(PORT);
            this.socket.setSoTimeout(TICK_MS);
            System.out.println("Servidor iniciado en el puerto " + PORT);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void ensureRunning() {
        if (instance == null || !instance.isAlive()) {
            instance = new Server();
            instance.start();
        }
    }

    public static synchronized void stopRunning() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::finish));
        long lastTick = System.currentTimeMillis();

        while (!end) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);
                socket.receive(packet);
                processMessage(packet);
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                if (!end) {
                    throw new RuntimeException(e);
                }
            }

            long now = System.currentTimeMillis();
            while (now - lastTick >= TICK_MS) {
                updateGameTick();
                lastTick += TICK_MS;
            }
        }
    }

    private synchronized void processMessage(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength()).trim();
        String[] parts = message.split("\\$");
        String command = parts[0];

        if ("connect".equals(command)) {
            onConnect(parts, packet.getAddress(), packet.getPort());
            return;
        }

        int sender = searchPeer(packet.getAddress(), packet.getPort());
        if (sender == -1) {
            return;
        }

        switch (command) {
            case "disconnect" -> {
                peers.remove(sender);
                resetRound();
                pingEveryone("waiting");
            }
            case "jump" -> jumpQueued[sender] = true;
            case "player_ready_restart" -> onPlayerReadyRestart(sender);
            default -> {
            }
        }
    }

    private void onConnect(String[] parts, InetAddress ip, int port) {
        int existing = searchPeer(ip, port);
        if (existing != -1) {
            sendMessage("connected$" + (existing + 1), ip, port);
            return;
        }

        if (peers.size() >= 2) {
            sendMessage("full", ip, port);
            return;
        }

        String username = parts.length > 1 ? parts[1] : "usuario" + (peers.size() + 1);
        peers.add(new Peer(username, ip, port));
        sendMessage("connected$" + peers.size(), ip, port);

        if (peers.size() < 2) {
            sendMessage("waiting", ip, port);
            return;
        }

        resetRound();
        pingEveryone("start$" + peers.get(0).username + "$" + peers.get(1).username);
    }

    private void onPlayerReadyRestart(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= playerReady.length || peers.size() < 2) {
            return;
        }

        if (!roundOver) {
            return;
        }

        restartPending = true;
        playerReady[playerIndex] = true;
        stateDirty = true;

        if (allPlayersReady()) {
            resetRound();
            pingEveryone("restart_game");
            playerReady[0] = false;
            playerReady[1] = false;
            stateDirty = true;
        }
    }

    private boolean allPlayersReady() {
        return playerReady[0] && playerReady[1];
    }

    private synchronized void updateGameTick() {
        if (peers.size() < 2) {
            return;
        }

        if (!roundOver && !restartPending) {
            tickCounter++;
            applyPlayerPhysics(0);
            applyPlayerPhysics(1);

            moveAndCleanupObstacles();
            if (tickCounter % OBSTACLE_SPAWN_EVERY_TICKS == 0) {
                spawnObstaclePair();
            }

            evaluateScoresAndCollisions();
            if (!playerAlive[0] || !playerAlive[1]) {
                roundOver = true;
                if (!playerAlive[0] && !playerAlive[1]) {
                    roundResult = 3;
                } else if (playerAlive[0]) {
                    roundResult = 1;
                } else {
                    roundResult = 2;
                }
            }
        }

        if (stateDirty || !roundOver) {
            pingEveryone(buildStateMessage());
            stateDirty = false;
        }
    }

    private void applyPlayerPhysics(int playerIndex) {
        if (!playerAlive[playerIndex]) {
            jumpQueued[playerIndex] = false;
            return;
        }

        if (jumpQueued[playerIndex]) {
            playerVelocity[playerIndex] = JUMP_SPEED;
        }
        jumpQueued[playerIndex] = false;

        playerY[playerIndex] += playerVelocity[playerIndex];
        playerVelocity[playerIndex] += GRAVITY;

        if (playerY[playerIndex] + PLAYER_HEIGHT >= WORLD_HEIGHT) {
            playerY[playerIndex] = WORLD_HEIGHT - PLAYER_HEIGHT;
            if (playerVelocity[playerIndex] > 0) {
                playerVelocity[playerIndex] = 0;
            }
        }

        if (playerY[playerIndex] < 0) {
            playerY[playerIndex] = 0;
            if (playerVelocity[playerIndex] < 0) {
                playerVelocity[playerIndex] = 0;
            }
        }
    }

    private void moveAndCleanupObstacles() {
        obstacles.forEach(obstacle -> obstacle.x -= OBSTACLE_SPEED);
        obstacles.removeIf(obstacle -> obstacle.x + OBSTACLE_WIDTH < 0);
    }

    private void spawnObstaclePair() {
        int maxTopHeight = Math.max(1, WORLD_HEIGHT - OBSTACLE_GAP - 120);
        int topHeight = (int) (Math.random() * maxTopHeight) + 60;
        int bottomY = topHeight + OBSTACLE_GAP;
        int bottomHeight = Math.max(1, WORLD_HEIGHT - bottomY);
        obstacles.add(new ObstaclePair(1600, topHeight, bottomY, bottomHeight));
        stateDirty = true;
    }

    private void evaluateScoresAndCollisions() {
        Rectangle[] playerBoxes = new Rectangle[]{playerBounds(0), playerBounds(1)};

        for (ObstaclePair obstacle : obstacles) {
            Rectangle top = obstacleBounds(obstacle.x, 0, OBSTACLE_WIDTH, obstacle.topHeight, true);
            Rectangle bottom = obstacleBounds(obstacle.x, obstacle.bottomY, OBSTACLE_WIDTH, obstacle.bottomHeight, false);

            for (int i = 0; i < 2; i++) {
                if (!playerAlive[i]) {
                    continue;
                }
                if (playerBoxes[i].intersects(top) || playerBoxes[i].intersects(bottom)) {
                    playerAlive[i] = false;
                    stateDirty = true;
                    continue;
                }

                if (playerBoxes[i].x > obstacle.x + OBSTACLE_WIDTH) {
                    if (i == 0 && !obstacle.scoredP1) {
                        obstacle.scoredP1 = true;
                        playerScore[i]++;
                        stateDirty = true;
                    }
                    if (i == 1 && !obstacle.scoredP2) {
                        obstacle.scoredP2 = true;
                        playerScore[i]++;
                        stateDirty = true;
                    }
                }
            }
        }
    }

    private Rectangle playerBounds(int index) {
        int hitboxX = PLAYER_X + PLAYER_HITBOX_MARGIN_X;
        int hitboxY = playerY[index] + PLAYER_HITBOX_MARGIN_Y;
        int hitboxWidth = Math.max(1, PLAYER_WIDTH - (PLAYER_HITBOX_MARGIN_X * 2));
        int hitboxHeight = Math.max(1, PLAYER_HEIGHT - (PLAYER_HITBOX_MARGIN_Y * 2));
        return new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }

    private Rectangle obstacleBounds(int x, int y, int width, int height, boolean topPipe) {
        int hitboxX = x + COLLISION_MARGIN_X;
        int hitboxWidth = Math.max(1, width - (COLLISION_MARGIN_X * 2));

        if (topPipe) {
            int hitboxY = y + COLLISION_MARGIN_TOP;
            int hitboxHeight = Math.max(1, height - COLLISION_MARGIN_TOP - COLLISION_MARGIN_BOTTOM_TOP_PIPE);
            return new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
        }

        int hitboxY = y + COLLISION_MARGIN_TOP_BOTTOM_PIPE;
        int hitboxHeight = Math.max(1, height - COLLISION_MARGIN_TOP_BOTTOM_PIPE);
        return new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }

    private String buildStateMessage() {
        StringBuilder builder = new StringBuilder("server_state");
        builder.append('$').append(WORLD_HEIGHT);
        builder.append('$').append(roundOver);
        builder.append('$').append(roundResult);

        for (int i = 0; i < 2; i++) {
            builder.append('$').append(playerY[i]);
            builder.append('$').append(playerVelocity[i]);
            builder.append('$').append(playerAlive[i]);
            builder.append('$').append(playerScore[i]);
        }

        builder.append('$').append(playerReady[0]);
        builder.append('$').append(playerReady[1]);

        builder.append('$').append(obstacles.size());
        for (ObstaclePair obstacle : obstacles) {
            builder.append('$').append(obstacle.x);
            builder.append('$').append(obstacle.topHeight);
            builder.append('$').append(obstacle.bottomY);
            builder.append('$').append(obstacle.bottomHeight);
        }
        return builder.toString();
    }

    private void resetRound() {
        obstacles.clear();
        playerY[0] = PLAYER_SPAWN_Y;
        playerY[1] = PLAYER_SPAWN_Y;
        playerVelocity[0] = 0;
        playerVelocity[1] = 0;
        playerAlive[0] = true;
        playerAlive[1] = true;
        playerScore[0] = 0;
        playerScore[1] = 0;
        jumpQueued[0] = false;
        jumpQueued[1] = false;
        playerReady[0] = false;
        playerReady[1] = false;
        tickCounter = 0;
        roundOver = false;
        roundResult = 0;
        restartPending = false;
        stateDirty = true;
    }

    private int searchPeer(InetAddress ip, int port) {
        for (int i = 0; i < peers.size(); i++) {
            Peer peer = peers.get(i);
            if (peer.ip.equals(ip) && peer.port == port) {
                return i;
            }
        }
        return -1;
    }

    private void pingEveryone(String message) {
        for (Peer peer : peers) {
            sendMessage(message, peer.ip, peer.port);
        }
    }

    public void sendMessage(String message, InetAddress ip, int port) {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);

        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void finish() {
        end = true;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (instance == this) {
            instance = null;
        }
    }
}
