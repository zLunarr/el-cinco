package online;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client extends Thread {
    private final InetAddress ipServer;
    private final int serverPort;
    private final DatagramSocket socket;
    private final ConcurrentLinkedQueue<String> inbox;
    private volatile boolean end;

    public Client(String serverIp, int serverPort) {
        try {
            this.ipServer = InetAddress.getByName(serverIp);
            this.serverPort = serverPort;
            this.socket = new DatagramSocket();
            this.socket.setSoTimeout(250);
            this.inbox = new ConcurrentLinkedQueue<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (!end) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket.receive(packet);
                inbox.add(new String(packet.getData()).trim());
            } catch (SocketTimeoutException ignored) {
            } catch (SocketException closed) {
                end = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String pollMessage() {
        return inbox.poll();
    }

    public void connect(String username) {
        sendMessage("connect$" + username);
    }

    public void sendState(int y, int score, boolean alive) {
        sendMessage("state$" + y + "$" + score + "$" + alive);
    }

    public void sendJump() {
        sendMessage("jump");
    }

    public void sendDisconnect() {
        sendMessage("disconnect");
    }

    private void sendMessage(String message) {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, ipServer, serverPort);

        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void finish() {
        end = true;
        socket.close();
        interrupt();
    }
}
