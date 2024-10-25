import java.net.*;
import java.util.*;
import java.io.IOException;

class Server {
    private static final int PORT = 5000;
    private DatagramSocket socket;
    private Map<String, ClientInfo> clients = new HashMap<>();

    static class ClientInfo {
        InetAddress address;
        int port;
        String status;
        long lastMessageTime;

        ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
            this.status = "AVAILABLE";
            this.lastMessageTime = System.currentTimeMillis();
        }
    }

    public Server() throws SocketException {
        socket = new DatagramSocket(PORT);
    }

    public void start() {
        System.out.println("Server started on port " + PORT);
        byte[] buffer = new byte[1024];

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                handleMessage(message, packet.getAddress(), packet.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(String message, InetAddress clientAddress, int clientPort) {
        String[] parts = message.split(":", 2);
        if (parts.length != 2) return;

        String username = parts[0];
        String content = parts[1];

        // Update or create client info
        clients.putIfAbsent(username, new ClientInfo(clientAddress, clientPort));
        ClientInfo client = clients.get(username);
        client.lastMessageTime = System.currentTimeMillis();

        // Handle commands
        if (content.startsWith("/")) {
            handleCommand(username, content);
            return;
        }

        // Send acknowledgment to sender
        sendAck(username);

        // Broadcast message to all available clients
        broadcastMessage(username + ": " + content);
    }

    private void handleCommand(String username, String command) {
        ClientInfo client = clients.get(username);
        switch (command.toLowerCase()) {
            case "/offline":
                client.status = "OFFLINE";
                broadcastMessage("System: " + username + " is now offline");
                break;
            case "/available":
                client.status = "AVAILABLE";
                broadcastMessage("System: " + username + " is now available");
                break;
            case "/busy":
                client.status = "BUSY";
                broadcastMessage("System: " + username + " is now busy");
                break;
        }
    }

    private void sendAck(String username) {
        ClientInfo client = clients.get(username);
        try {
            String ackMessage = "ACK";
            byte[] buffer = ackMessage.getBytes();
            DatagramPacket ackPacket = new DatagramPacket(
                    buffer, buffer.length, client.address, client.port
            );
            socket.send(ackPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(String message) {
        String[] parts = message.split(":");
        byte[] buffer = message.getBytes();
        System.out.println(message);
        clients.forEach((username, client) -> {
            if (!"OFFLINE".equals(client.status) && !username.equals(parts[0])) {
                try {
                    DatagramPacket packet = new DatagramPacket(
                            buffer, buffer.length, client.address, client.port
                    );
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        try {
            new Server().start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
