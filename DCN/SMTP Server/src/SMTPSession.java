import java.io.*;
import java.net.*;
import java.util.*;

class SMTPSession implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private State currentState;
    private String sender;
    private List<String> recipients;
    private String messageContent;
    private boolean isRunning;

    public SMTPSession(Socket socket) {
        this.clientSocket = socket;
        this.recipients = new ArrayList<>();
        this.currentState = new InitialState();
        this.isRunning = true;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Send initial greeting
            sendResponse(currentState.getPrompt());

            String inputLine;
            while (isRunning && (inputLine = in.readLine()) != null) {
                currentState = currentState.handleInput(inputLine, this);
                if (currentState == null) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error in SMTP session: " + e.getMessage());
        } finally {
            close();
        }
    }

    public void sendResponse(String response) {
        out.println(response);
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public void addRecipient(String recipient) {
        recipients.add(recipient);
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setMessageContent(String content) {
        this.messageContent = content;
    }

    public void close() {
        try {
            isRunning = false;
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing session: " + e.getMessage());
        }
    }
}
