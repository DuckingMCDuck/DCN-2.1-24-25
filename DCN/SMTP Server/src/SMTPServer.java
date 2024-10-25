import java.net.*;
import java.io.*;

public class SMTPServer {
    private ServerSocket serverSocket;
    private boolean isRunning;

    public SMTPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        isRunning = true;
        System.out.println("SMTP Server started on port " + port);
    }

    public void start() {
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from: " + clientSocket.getInetAddress());

                // Create new thread for each connection
                SMTPSession session = new SMTPSession(clientSocket);
                new Thread(session).start();
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            SMTPServer server = new SMTPServer(25);
            server.start();
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }
}