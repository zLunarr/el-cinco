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

    private DatagramSocket socket;
    private final int port;
    private volatile boolean end;
    private final int maxClients;
    private final List<User> users;

    public Server() {
        this.port = 5555;
        this.end = false;
        this.maxClients = 2;
        this.users = new ArrayList<>();

        try {
            socket = new DatagramSocket(port);
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

        int sender = searchUser(packet.getAddress(), packet.getPort());
        if (sender == -1) {
            return;
        }

        if ("disconnect".equals(command)) {
            users.remove(sender);
            pingEveryone("waiting");
            return;
        }

        relayToOthers(sender, message);
    }

    private void onConnect(String[] parts, InetAddress address, int port) {
        if (searchUser(address, port) != -1) {
            sendMessage("connected$" + (searchUser(address, port) + 1), address, port);
            return;
        }

        if (users.size() >= maxClients) {
            sendMessage("full", address, port);
            return;
        }

        String username = parts.length > 1 ? parts[1] : "usuario" + (users.size() + 1);
        User user = new User(username, address, port);
        users.add(user);

        int index = users.size();
        sendMessage("connected$" + index, address, port);

        if (users.size() < maxClients) {
            sendMessage("waiting", address, port);
            return;
        }

        String startMessage = "start$" + users.get(0).getUsername() + "$" + users.get(1).getUsername();
        pingEveryone(startMessage);
    }

    private void relayToOthers(int sender, String message) {
        for (int i = 0; i < users.size(); i++) {
            if (i == sender) {
                continue;
            }
            User user = users.get(i);
            sendMessage(message, user.getIp(), user.getPort());
        }
    }

    private int searchUser(InetAddress ip, int port) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getIp().equals(ip) && user.getPort() == port) {
                return i;
            }
        }
        return -1;
    }

    private void pingEveryone(String message) {
        for (User user : users) {
            sendMessage(message, user.getIp(), user.getPort());
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
