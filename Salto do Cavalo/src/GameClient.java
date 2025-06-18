import java.io.*;
import java.net.*;

public class GameClient {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private GameClientListener listener;

    public void startClient(GameClientListener listener) {
        this.listener = listener;

        try {
            socket = new Socket("localhost", 1234);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            System.out.println("[Client] Connected to server.");

            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String message = input.readUTF();
                        System.out.println("[Client] Received: " + message);
                        if (listener != null) {
                            listener.onMessageReceived(message);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("[Client] Connection lost or error: " + e.getMessage());
                }
            });

            readThread.setDaemon(true);
            readThread.start();

        } catch (IOException e) {
            System.err.println("[Client] Error connecting to server: " + e.getMessage());
        }
    }

    public void sendMove(int row, int col) {
        sendMessage(row + "," + col);
    }

    public void sendMessage(String msg) {
        try {
            output.writeUTF(msg);
        } catch (IOException e) {
            System.err.println("[Client] Failed to send message: " + e.getMessage());
        }
    }
}
