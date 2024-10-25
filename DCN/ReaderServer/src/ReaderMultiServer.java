import java.io.IOException;
import java.net.ServerSocket;

public class ReaderMultiServer {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Usage: java ReaderMultiServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        boolean listening = true;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                new ReaderMultiServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }

    }
}