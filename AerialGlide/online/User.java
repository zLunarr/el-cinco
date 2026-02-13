package online;

import java.net.InetAddress;

public class User {
    private final String id;
    private final String username;
    private final InetAddress ip;
    private final int port;

    public User(String username, InetAddress ip, int port) {
        this.username = username;
        this.ip = ip;
        this.port = port;
        this.id = ip.toString() + ":" + port;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
