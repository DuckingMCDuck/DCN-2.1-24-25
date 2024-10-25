import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class Pop3Client {
    private SSLSocket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final String server;
    private final int port;
    private final String username;
    private final String password;

    public Pop3Client(String server, int port, String username, String password) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void connect() throws IOException {
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) factory.createSocket(server, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            // Read server greeting
            String response = reader.readLine();
            if (!response.startsWith("+OK")) {
                throw new IOException("Invalid server greeting: " + response);
            }

            // Login
            login();
        } catch (IOException e) {
            disconnect();
            throw e;
        }
    }

    private void login() throws IOException {
        // Send username
        sendCommand("USER " + username);

        // Send password
        sendCommand("PASS " + password);
    }

    public List<String> getEmailSubjects() throws IOException {
        List<String> subjects = new ArrayList<>();

        // Get number of messages
        String statsResponse = sendCommand("STAT");
        String[] stats = statsResponse.split(" ");
        int messageCount = Integer.parseInt(stats[1]);

        // Retrieve each message's subject
        for (int i = 1; i <= messageCount; i++) {
            // Retrieve message headers
            writer.println("TOP " + i + " 0");
            String line;
            boolean foundSubject = false;

            while ((line = reader.readLine()) != null) {

                if (line.toLowerCase().startsWith("subject:")) {
                    subjects.add("Message " + i + ": " + line.substring(8).trim());
                    foundSubject = true;
                    break;
                }

            }

            if (!foundSubject) {
                subjects.add("Message " + i + ": (No subject)");
            }
        }

        return subjects;
    }

    private String sendCommand(String command) throws IOException {
        writer.println(command);
        String response = reader.readLine();

        if (response.startsWith("-ERR")) {
            throw new IOException("Server error: " + response);
        }

        return response;
    }

    public void disconnect() {
        try {
            if (writer != null) {
                sendCommand("QUIT");
                writer.close();
            }
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        // Read configuration from properties file or environment variables
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        String server = "pop.gmail.com";
        int port = 995;
        String username = "pop3server1234@gmail.com";
        String password = "tdyw zltu lold vbzo";

        System.out.println("When pressing 'enter' the following login details will used:" +
                "\nServer: pop.gmail.com" +
                "\nUsername: pop3server1234@gmail.com" +
                "\nPassword: tdyw zltu lold vbzo" +
                "\n\nWrite 'custom' for custom login details");
        try {
            if (consoleReader.readLine().equals("custom")) {
                System.out.println("Server:");
                server = consoleReader.readLine();
                System.out.println("Username:");
                username = consoleReader.readLine();
                System.out.println("Password:");
                password = consoleReader.readLine();
            }
        } catch (Exception ex){
            System.out.println("Something went wrong!");
            System.exit(0);
        }

        Pop3Client client = new Pop3Client(server, port, username, password);

        try {
            System.out.println("Connecting to mail server...");
            client.connect();
            System.out.println("Connected successfully!");

            System.out.println("\nRetrieving email subjects...");
            List<String> subjects = client.getEmailSubjects();

            System.out.println("\nEmail subjects in your mailbox:");
            for (String subject : subjects) {
                System.out.println(subject);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            client.disconnect();
        }
    }
}