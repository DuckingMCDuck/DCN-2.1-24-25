//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        try {
            System.out.println("Choose mode (1 for server, 2 for client):");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String choice = reader.readLine();
            if (choice.equals("1")) {
                System.out.println("Starting server...");
                ChatServer server = new ChatServer(5000);
                server.start();
            } else if (choice.equals("2")) {
                System.out.println("Starting client...");
                ChatClient client = new ChatClient("localhost", 5000);
                client.startReceiving();
                System.out.println("Enter messages (press Enter to send):");

                String message;
                while((message = reader.readLine()) != null) {
                    client.sendMessage(message);
                }
            } else {
                System.out.println("Invalid choice. Please enter 1 for server or 2 for client.");
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }
}

