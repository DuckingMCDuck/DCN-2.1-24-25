import java.net.*;
        import java.util.*;
        import java.io.*;

class Client {
    private static final int SERVER_PORT = 5000;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private String username;
    private volatile boolean waitingForAck = false;
    private volatile boolean running = true;

    public Client(String username) throws SocketException, UnknownHostException {
        this.username = username;
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getLocalHost();
    }

    public void start() {
        // Start message receiver thread
        new Thread(this::receiveMessages).start();

        // Main thread handles user input
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Chat started. Type messages or commands (/offline, /available, /busy)");

            while (running) {
                String message = scanner.nextLine();
                if (message.isEmpty()) continue;

                sendMessage(message);
            }
        }
    }

    private void sendMessage(String message) {
        try {
            String fullMessage = username + ":" + message;
            byte[] buffer = fullMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length, serverAddress, SERVER_PORT
            );

            waitingForAck = true;
            socket.send(packet);

            // Wait for acknowledgment
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // Wait 5 seconds for ACK
                    if (waitingForAck) {
                        System.out.println("Message delivery failed - no server acknowledgment received");
                        waitingForAck = false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessages() {
        byte[] buffer = new byte[1024];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());

                if ("ACK".equals(message)) {
                    waitingForAck = false;
                } else {
                    System.out.println(message);
                }
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        running = false;
        socket.close();
    }

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            Client client = new Client(username);
            client.start();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }
}