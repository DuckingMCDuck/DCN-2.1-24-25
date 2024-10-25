import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ChatServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private CryptoService cryptoService;

    public ChatServer(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        cryptoService = new CryptoService();
        System.out.println("Server started on port " + port);
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private CryptoService clientCrypto;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            try {
                this.clientCrypto = new CryptoService();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // Send symmetric key to client
                out.println(Base64.getEncoder().encodeToString(clientCrypto.getEncodedSymmetricKey()));

                String message;
                while ((message = in.readLine()) != null) {
                    try {
                        String[] parts = message.split("\\|");
                        if (parts.length != 2) {
                            System.out.println("Invalid message format received");
                            continue;
                        }

                        String encryptedContent = parts[0];
                        String receivedHash = parts[1];

                        // Verify hash
                        String calculatedHash = clientCrypto.calculateHash(encryptedContent);
                        if (!calculatedHash.equals(receivedHash)) {
                            System.out.println("Message integrity check failed!");
                            continue;
                        }

                        // Decrypt and broadcast
                        String decryptedMsg = clientCrypto.decryptMessage(encryptedContent);
                        System.out.println("Received: " + decryptedMsg); // Debug output
                        broadcast(decryptedMsg, this);

                    } catch (Exception e) {
                        System.out.println("Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                clients.remove(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message, ClientHandler sender) {
            try {
                String encryptedMsg = clientCrypto.encryptMessage(message);
                String hash = clientCrypto.calculateHash(encryptedMsg);
                String fullMessage = encryptedMsg + "|" + hash;

                for (ClientHandler client : clients) {
                    if (client != sender) {
                        client.out.println(fullMessage);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error broadcasting message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}