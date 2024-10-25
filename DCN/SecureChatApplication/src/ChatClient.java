import java.io.*;
        import java.net.*;
        import java.util.Base64;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private CryptoService cryptoService;

    public ChatClient(String host, int port) throws Exception {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        cryptoService = new CryptoService();

        // Receive symmetric key from server
        String encodedKey = in.readLine();
        byte[] symmetricKey = Base64.getDecoder().decode(encodedKey);
        cryptoService.setSymmetricKey(symmetricKey);

        System.out.println("Connected to server and initialized encryption");
    }

    public void sendMessage(String message) {
        try {
            String encryptedMsg = cryptoService.encryptMessage(message);
            String hash = cryptoService.calculateHash(encryptedMsg);
            String fullMessage = encryptedMsg + "|" + hash;
            out.println(fullMessage);
            System.out.println("Sent (encrypted): " + encryptedMsg); // Debug output
        } catch (Exception e) {
            System.out.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void startReceiving() {
        new Thread(() -> {
            try {
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
                        String calculatedHash = cryptoService.calculateHash(encryptedContent);
                        if (!calculatedHash.equals(receivedHash)) {
                            System.out.println("Message integrity check failed!");
                            continue;
                        }

                        String decryptedMsg = cryptoService.decryptMessage(encryptedContent);
                        System.out.println("Received: " + decryptedMsg);
                    } catch (Exception e) {
                        System.out.println("Error processing received message: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection to server lost");
            }
        }).start();
    }
}