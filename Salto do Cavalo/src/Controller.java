import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Controller {

    @FXML private TextField tfName;
    @FXML private GridPane boardGrid;
    @FXML private Label currentPlayerLabel;
    @FXML private Button restartButton;
    @FXML private TextField chatInput;
    @FXML private TextArea chatLog;

    private Stage mainWindow;
    private final int SIZE = 5;
    private Button[][] boardButtons = new Button[SIZE][SIZE];

    private Player player1;
    private Player player2;
    private Horse horse1;
    private Horse horse2;
    private Player currentPlayer;
    private Horse currentHorse;

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void setPlayers(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = p1;
        updateCurrentPlayerDisplay(currentPlayer);
    }

    public void setHorses(Horse h1, Horse h2) {
        this.horse1 = h1;
        this.horse2 = h2;
        this.currentHorse = h1;
    }

    private final GameClient client = new GameClient();  // no @FXML here

    public void startGameClient() {
        client.startClient(new GameClientListener() {
            @Override
            public void onMessageReceived(String message) {
                Platform.runLater(() -> handleOpponentMove(message));
            }
        });
    }
    
    private void updateCurrentPlayerDisplay(Player player) {
        currentPlayerLabel.setText("Jogador atual: " + player.getName());
    }

    private void createBoard() {
        boardGrid.getChildren().clear();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Button btn = new Button();

                // Set square shape and remove spacing
                btn.setPrefSize(100, 100);
                btn.setMinSize(100, 100);
                btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                // Remove all padding/margins
                btn.setPadding(Insets.EMPTY);
                btn.setStyle(
                    "-fx-background-radius: 0;" +
                    "-fx-border-radius: 0;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-insets: 0;" +
                    "-fx-border-width: 0;" +
                    "-fx-border-color: transparent;" +
                    "-fx-background-color: " +
                    ((row + col) % 2 == 0 ? "#f0d9b5" : "#b58863")
                );

                boardGrid.add(btn, col, row);
                boardButtons[row][col] = btn;
            }
        }
    }

    @FXML
    void onBtnClick(ActionEvent event) {
        String title = tfName.getText();
        mainWindow.setTitle(title);
    }

    public void handleOpponentMove(String message) {
        if (message.startsWith("CHAT:")) {
            String chatMessage = message.substring(5);
            appendChat("Oponente: " + chatMessage);
            return;
        }

        String[] parts = message.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        boardButtons[row][col].setText("♞");
        boardButtons[row][col].setDisable(true);
    }

    @FXML
    public void onSendChat() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage("CHAT:" + message);  // send with prefix
            appendChat("Você: " + message);
            chatInput.clear();
        }
    }

    private void appendChat(String message) {
        chatLog.appendText(message + "\n");
    }
}
