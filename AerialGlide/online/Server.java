package online;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
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

    private final int port;
    private final List<Peer> peers;
    private DatagramSocket socket;
    private volatile boolean end;

    public Server() {
        this.port = 5555;
        this.peers = new ArrayList<>();

        try {
            this.socket = new DatagramSocket(port);
            System.out.println("Servidor iniciado en el puerto " + port);
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

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::finish));

        while (!end) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket.receive(packet);
                processMessage(packet);
            } catch (IOException e) {
                if (!end) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private synchronized void processMessage(DatagramPacket packet) {
        String message = new String(packet.getData()).trim();
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

        if ("disconnect".equals(command)) {
            peers.remove(sender);
            pingEveryone("waiting");
            return;
        }

        relayToOthers(sender, message);
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

        pingEveryone("start$" + peers.get(0).username + "$" + peers.get(1).username);
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

    private void relayToOthers(int sender, String message) {
        for (int i = 0; i < peers.size(); i++) {
            if (i == sender) {
                continue;
            }
            Peer peer = peers.get(i);
            sendMessage(message, peer.ip, peer.port);
        }
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
    }
}
