import java.io.*;
import java.net.*;
import java.util.*;
import javafx.application.Platform;

public class GameServer {
    private static final int PORT = 1234;
    private static final int MAX_PLAYERS = 2;

    private final List<ClientHandler> clients = new ArrayList<>();
    private final ServerController ui;

    private Runnable onGameStart;

    public void setOnGameStart(Runnable callback) {
        this.onGameStart = callback;
    }

    public GameServer(ServerController ui) {
        this.ui = ui;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("[Server] Listening on port " + PORT + "...");

            while (clients.size() < MAX_PLAYERS) {
                Socket socket = serverSocket.accept();
                String ip = socket.getInetAddress().getHostAddress();
                log("[Server] Player connected: " + ip);

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                ClientHandler clientHandler = new ClientHandler(socket, in, out);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

                updateClientCount(clients.size());
            }

            log("[Server] Two players connected. Starting game...");
            clients.get(0).setOpponent(clients.get(1));
            clients.get(1).setOpponent(clients.get(0));

            clients.get(0).sendMessage("START P1");
            clients.get(1).sendMessage("START P2");

            if (onGameStart != null) {
                Platform.runLater(onGameStart);
            }

        } catch (IOException e) {
            log("[Server Error] " + e.getMessage());
        }
    }

    private void log(String message) {
        Platform.runLater(() -> ui.log(message));
    }

    private void updateClientCount(int count) {
        Platform.runLater(() -> ui.updateClientCount(count));
    }

    class ClientHandler implements Runnable {
        private final Socket socket;
        private final DataInputStream input;
        private final DataOutputStream output;
        private ClientHandler opponent;
        private boolean active = true;

        public ClientHandler(Socket socket, DataInputStream input, DataOutputStream output) {
            this.socket = socket;
            this.input = input;
            this.output = output;
        }

        public void setOpponent(ClientHandler opponent) {
            this.opponent = opponent;
        }

        public void sendMessage(String message) {
            try {
                output.writeUTF(message);
            } catch (IOException e) {
                log("[Server] Failed to send message: " + message);
            }
        }

        @Override
        public void run() {
            try {
                while (active) {
                    String received = input.readUTF();
                    log("[Server] Received: " + received);

                    if (received.equalsIgnoreCase("logout")) {
                        active = false;
                        input.close();
                        output.close();
                        socket.close();
                        log("[Server] A player has disconnected.");
                        if (opponent != null) opponent.sendMessage("Opponent disconnected.");
                        break;
                    }

                    if (opponent != null) {
                        opponent.sendMessage(received);
                    }
                }
            } catch (IOException e) {
                log("[Server] Connection error: " + e.getMessage());
            }
        }
    }
}
