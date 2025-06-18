import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;


import java.util.List;

public class Controller {

    @FXML
    private GridPane boardGrid;
    @FXML
    private Label currentPlayerLabel;
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
    private boolean isMyTurn = true;
    private String playerRole; 

    private boolean selectingMove = false;
    private List<int[]> possibleMoves;

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void setPlayers(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = p1;
        updateCurrentPlayerDisplay();
    }

    public void setHorses(Horse h1, Horse h2) {
        this.horse1 = h1;
        this.horse2 = h2;
        this.currentHorse = null;

        if (boardButtons[0][0] == null) {
            createBoard(); // ensure it's ready
        }

        updateBoard();
    }

    @FXML
    public void initialize() {
        createBoard();
    }

    private final GameClient client = new GameClient(); 

    public void startGameClient() {
        client.startClient(new GameClientListener() {
            @Override
            public void onMessageReceived(String message) {
                Platform.runLater(() -> handleOpponentMove(message));
            }
        });
        createBoard();
    }

    public void handleOpponentMove(String message) {
        if (message.startsWith("CHAT:")) {
            String chatMessage = message.substring(5);
            appendChat("Oponente: " + chatMessage);
            return;
        }

        if (message.startsWith("START ")) {
            this.playerRole = message.substring(6); // "P1" or "P2"
            this.isMyTurn = playerRole.equals("P1"); // P1 starts
            return;
        }

        if (message.startsWith("MOVE:")) {
            String[] coords = message.substring(5).split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);

            // 🧠 Use the opponent's horse!
            Horse opponentHorse = playerRole.equals("P1") ? horse2 : horse1;
            opponentHorse.moveTo(row, col);
            updateBoard();
            togglePlayer();
            updateCurrentPlayerDisplay();
            isMyTurn = true;
        }
    }

    @FXML
    public void onSendChat() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage("CHAT:" + message);
            appendChat("Você: " + message);
            chatInput.clear();
        }
    }

    private void appendChat(String message) {
        chatLog.appendText(message + "\n");
    }

    private void updateCurrentPlayerDisplay() {
        currentPlayerLabel.setText("Jogador atual: " + currentPlayer.getName());
    }

    private void createBoard() {
        boardGrid.getChildren().clear();
        boardGrid.getColumnConstraints().clear();
        boardGrid.getRowConstraints().clear();

        for (int i = 0; i < SIZE; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / SIZE);
            boardGrid.getColumnConstraints().add(colConst);

            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / SIZE);
            boardGrid.getRowConstraints().add(rowConst);
        }

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Button btn = new Button();
                btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btn.setPadding(Insets.EMPTY);
                btn.setStyle(
                        "-fx-background-radius: 0;" +
                                "-fx-border-radius: 0;" +
                                "-fx-background-insets: 0;" +
                                "-fx-border-insets: 0;" +
                                "-fx-border-width: 0;" +
                                "-fx-border-color: transparent;" +
                                "-fx-background-color: " +
                                ((row + col) % 2 == 0 ? "#f0d9b5" : "#b58863"));

                final int r = row;
                final int c = col;

                btn.setOnAction(e -> onCellClicked(r, c));

                boardGrid.add(btn, col, row);
                boardButtons[row][col] = btn;
            }
        }
    }

    private boolean isMyHorse(Horse horse) {
        return (playerRole.equals("P1") && horse == horse1)
            || (playerRole.equals("P2") && horse == horse2);
    }

    private void onCellClicked(int row, int col) {
        if (!isMyTurn) return;

        if (!selectingMove) {
            Horse horse = getHorseAtPosition(row, col);
            if (horse != null && isMyHorse(horse)) {
                currentHorse = horse;
                boolean[][] blocked = getBlockedCells();
                possibleMoves = horse.getPossibleMoves(SIZE, blocked);

                if (possibleMoves.isEmpty()) {
                    showGameOver("Fim do jogo! " + getOpponent(currentPlayer).getName() + " venceu!");
                    return;
                }

                highlightPossibleMoves(true);
                selectingMove = true;
            }
        } else {
            if (isMoveValid(row, col)) {
                currentHorse.moveTo(row, col);
                updatePlayerPosition(currentHorse, row, col);

                highlightPossibleMoves(false);
                selectingMove = false;
                currentHorse = null;

                client.sendMessage("MOVE:" + row + "," + col);
                togglePlayer();
                updateCurrentPlayerDisplay();
                updateBoard();
                isMyTurn = false;
            } else {
                flashInvalidMove(row, col);
            }
        }
    }

    private void flashInvalidMove(int row, int col) {
        Button btn = boardButtons[row][col];
        String originalStyle = btn.getStyle();

        // Muda a cor de fundo para vermelho
        btn.setStyle("-fx-background-color: #ff6666;");

        // Volta ao estilo original depois de 300ms
        new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            javafx.application.Platform.runLater(() -> btn.setStyle(originalStyle));
        }).start();
    }

    private boolean isCurrentPlayersHorse(Horse horse) {
        if (horse == horse1)
            return currentPlayer == player1;
        if (horse == horse2)
            return currentPlayer == player2;
        return false;
    }

    private Horse getHorseAtPosition(int row, int col) {
        if (horse1.getRow() == row && horse1.getCol() == col)
            return horse1;
        if (horse2.getRow() == row && horse2.getCol() == col)
            return horse2;
        return null;
    }

    private boolean[][] getBlockedCells() {
        boolean[][] blocked = new boolean[SIZE][SIZE];

        // Bloqueia posições atuais dos cavalos
        blocked[horse1.getRow()][horse1.getCol()] = true;
        blocked[horse2.getRow()][horse2.getCol()] = true;

        // Bloqueia todas as casas visitadas pelo cavalo 1
        for (String pos : horse1.getVisitedPositions()) {
            String[] parts = pos.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            blocked[r][c] = true;
        }

        // Bloqueia todas as casas visitadas pelo cavalo 2
        for (String pos : horse2.getVisitedPositions()) {
            String[] parts = pos.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            blocked[r][c] = true;
        }

        return blocked;
    }

    private boolean isMoveValid(int row, int col) {
        for (int[] move : possibleMoves) {
            if (move[0] == row && move[1] == col)
                return true;
        }
        return false;
    }

    private void highlightPossibleMoves(boolean highlight) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Button btn = boardButtons[row][col];
                // Fundo padrão do tabuleiro
                btn.setStyle(
                        "-fx-background-radius: 0;" +
                                "-fx-border-radius: 0;" +
                                "-fx-background-insets: 0;" +
                                "-fx-border-insets: 0;" +
                                "-fx-border-width: 0;" +
                                "-fx-border-color: transparent;" +
                                "-fx-background-color: " +
                                ((row + col) % 2 == 0 ? "#f0d9b5" : "#b58863"));
                btn.setText("");
            }
        }

        if (highlight) {
            // Pintar casas visitadas de cinza (visíveis durante seleção)
            for (String pos : horse1.getVisitedPositions()) {
                String[] parts = pos.split(",");
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                boardButtons[r][c].setStyle("-fx-background-color: #a9a9a9;"); // cinza
            }
            for (String pos : horse2.getVisitedPositions()) {
                String[] parts = pos.split(",");
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                boardButtons[r][c].setStyle("-fx-background-color: #a9a9a9;"); // cinza
            }

            // Pintar possíveis movimentos em verde claro
            if (possibleMoves != null) {
                for (int[] move : possibleMoves) {
                    int r = move[0];
                    int c = move[1];
                    boardButtons[r][c].setStyle("-fx-background-color: #90ee90;"); // verde claro
                }
            }
        }

        // Mostrar os cavalos sempre
        boardButtons[horse1.getRow()][horse1.getCol()].setText("♞");
        boardButtons[horse2.getRow()][horse2.getCol()].setText("♞");
    }

    private void updatePlayerPosition(Horse horse, int row, int col) {
        if (horse == horse1) {
            player1.setPosition(row, col);
        } else if (horse == horse2) {
            player2.setPosition(row, col);
        }
    }

    public void updateBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Button btn = boardButtons[row][col];
                btn.setStyle(
                        "-fx-background-radius: 0;" +
                                "-fx-border-radius: 0;" +
                                "-fx-background-insets: 0;" +
                                "-fx-border-insets: 0;" +
                                "-fx-border-width: 0;" +
                                "-fx-border-color: transparent;" +
                                "-fx-background-color: " +
                                ((row + col) % 2 == 0 ? "#f0d9b5" : "#b58863"));
                btn.setText("");
            }
        }

        // Pintar casas visitadas de cinza
        for (String pos : horse1.getVisitedPositions()) {
            String[] parts = pos.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            boardButtons[r][c].setStyle("-fx-background-color: #a9a9a9;"); // cinza
        }
        for (String pos : horse2.getVisitedPositions()) {
            String[] parts = pos.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            boardButtons[r][c].setStyle("-fx-background-color: #a9a9a9;"); // cinza
        }

        // Mostrar os cavalos
        boardButtons[horse1.getRow()][horse1.getCol()].setText("♞");
        boardButtons[horse2.getRow()][horse2.getCol()].setText("♞");
    }

    private void togglePlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private void showGameOver(String message) {
        // Usando Alert do JavaFX
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Jogo terminado");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();

            // Fecha a janela principal (ou pode reiniciar o jogo)
            if (mainWindow != null) {
                mainWindow.close();
            }
        });
    }

    private Player getOpponent(Player player) {
        return (player == player1) ? player2 : player1;
    }

}
