package com.aerialglide.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TcpGameClient {
    private final String host;
    private final int port;
    private final String name;
    private final ConcurrentLinkedQueue<String> inbox = new ConcurrentLinkedQueue<>();
    private Socket socket;
    private PrintWriter out;
    private volatile boolean running;

    public TcpGameClient(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        running = true;
        new Thread(this::readLoop, "tcp-client-read").start();
        send("HELLO|" + name);
    }

    private void readLoop() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while (running && (line = in.readLine()) != null) {
                inbox.add(line);
            }
        } catch (IOException ignored) {
        } finally {
            running = false;
            inbox.add("DISCONNECTED");
        }
    }

    public String poll() {
        return inbox.poll();
    }

    public void send(String msg) {
        if (out != null) out.println(msg);
    }

    public void close() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}
