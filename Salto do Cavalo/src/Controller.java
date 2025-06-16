import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Controller {

    @FXML private TextField tfName;
    @FXML private GridPane boardGrid;
    @FXML private Label currentPlayerLabel;
    @FXML private Button restartButton;

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

    @FXML
    public void initialize() {
        createBoard();
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
                btn.setPrefSize(100, 100); // adjust size as needed
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
                    ((row + col) % 2 == 0 ? "#f0d9b5" : "#b58863") // chess style
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
}
