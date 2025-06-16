import java.io.*;
import java.net.*;
import java.util.Scanner;

public class GameClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1234;

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    public static void main(String[] args) {
        new GameClient().startClient();
    }

    public void startClient() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            System.out.println("Connected to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);

            // Thread to listen for server messages
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String serverMessage = input.readUTF();
                        System.out.println("Server: " + serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            readThread.setDaemon(true);
            readThread.start();

            // Send moves to the server manually (for testing)
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();
                output.writeUTF(message);
                if (message.equalsIgnoreCase("logout")) break;
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMove(String move) {
        try {
            output.writeUTF(move);
        } catch (IOException e) {
            System.err.println("Failed to send move: " + move);
        }
    }
}
