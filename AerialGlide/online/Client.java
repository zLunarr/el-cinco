package online;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Client extends Thread {
    private final InetAddress ipServer;
    private final int serverPort;
    private final DatagramSocket socket;
    private volatile boolean end;
    private final NetManager netManager;
    private ClientListener listener;

    public Client(String serverIp, int serverPort, NetManager netManager, ClientListener listener) {
        try {
            this.ipServer = InetAddress.getByName(serverIp);
            this.serverPort = serverPort;
            this.socket = new DatagramSocket();
            this.socket.setSoTimeout(6000);
            this.netManager = netManager;
            this.listener = listener;
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
                String message = new String(packet.getData()).trim();
                if (listener != null) {
                    listener.onMessage(message);
                }
            } catch (SocketTimeoutException timeoutException) {
                if (netManager != null) {
                    netManager.timeOutEnded();
                }
            } catch (SocketException closed) {
                end = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setListener(ClientListener listener) {
        this.listener = listener;
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
            if (netManager != null && message.startsWith("connect")) {
                netManager.connect(true);
            }
        } catch (IOException e) {
            if (netManager != null && message.startsWith("connect")) {
                netManager.connect(false);
            }
            throw new RuntimeException(e);
        }
    }

    public void finish() {
        end = true;
        socket.close();
        interrupt();
    }
}
