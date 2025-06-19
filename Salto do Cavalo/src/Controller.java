import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
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
    @FXML
    private TextField chatInput;
    @FXML
    private TextArea chatLog;
    @FXML
    private TextArea moveLog;
    @FXML
    private Label roundLabel;
    @FXML
    private Button restartButton; // botão Recomeçar

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

    private int ronda = 1;
    private boolean jogadaPar = false;

    private final GameClient client = new GameClient();

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
            createBoard();
        }
        updateBoard();
    }

    @FXML
    public void initialize() {
        createBoard();

        // Configura ação do botão Recomeçar
        if (restartButton != null) {
            restartButton.setOnAction(e -> onRestartGame());
        }
    }

    public void startGameClient() {
        client.startClient(message -> Platform.runLater(() -> handleOpponentMove(message)));
    }

    private void handleOpponentMove(String message) {
        if (message.startsWith("CHAT:")) {
            String chatMessage = message.substring(5);
            appendChat(getOpponent(currentPlayer).getName() + ": " + chatMessage);
            return;
        }

        if (message.startsWith("START ")) {
            this.playerRole = message.substring(6);
            this.isMyTurn = playerRole.equals("P1");
            updateBoard();
            updateCurrentPlayerDisplay();
            return;
        }

        if (message.startsWith("MOVE:")) {
            String[] coords = message.substring(5).split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);

            Horse opponentHorse = playerRole.equals("P1") ? horse2 : horse1;
            opponentHorse.moveTo(row, col);
            updateBoard();
            togglePlayer();
            updateCurrentPlayerDisplay();
            isMyTurn = true;
            String opponentName = getOpponent(currentPlayer).getName();
            logMove(opponentName, row, col);
            return;
        }

        if (message.equals("RESTART_REQUEST")) {
            // Recebeu pedido para reiniciar
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Pedido de reinício");
                alert.setHeaderText(null);
                alert.setContentText("O outro jogador quer reiniciar o jogo. Aceita?");

                ButtonType yes = new ButtonType("Sim");
                ButtonType no = new ButtonType("Não");
                alert.getButtonTypes().setAll(yes, no);

                alert.showAndWait().ifPresent(response -> {
                    if (response == yes) {
                        client.sendMessage("RESTART_ACCEPTED");
                        restartLocalGame();
                        appendChat("[Sistema] Você aceitou reiniciar o jogo.");
                    } else {
                        client.sendMessage("RESTART_DECLINED");
                        appendChat("[Sistema] Você recusou reiniciar o jogo.");
                    }
                });
            });
            return;
        }

        if (message.equals("RESTART_ACCEPTED")) {
            // Pedido aceito, reinicia
            restartLocalGame();
            appendChat("[Sistema] O outro jogador aceitou reiniciar o jogo.");
            return;
        }

        if (message.equals("RESTART_DECLINED")) {
            appendChat("[Sistema] O outro jogador recusou reiniciar o jogo.");
            return;
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
        if (!isMyTurn)
            return;

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
                logMove(currentPlayer.getName(), row, col);

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

        btn.setStyle("-fx-background-color: #ff6666;");

        new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> btn.setStyle(originalStyle));
        }).start();
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

        blocked[horse1.getRow()][horse1.getCol()] = true;
        blocked[horse2.getRow()][horse2.getCol()] = true;

        for (String pos : horse1.getVisitedPositions()) {
            String[] parts = pos.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            blocked[r][c] = true;
        }

        for (String pos : horse2.getVisitedPositions()) {
            String[] parts = pos.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            blocked[r][c] = true;
        }

        return blocked;
    }

    private boolean isMoveValid(int row, int col) {
        if (possibleMoves == null)
            return false;

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
            for (String pos : horse1.getVisitedPositions()) {
                int r = Integer.parseInt(pos.split(",")[0]);
                int c = Integer.parseInt(pos.split(",")[1]);
                if (r != horse1.getRow() || c != horse1.getCol()) {
                    boardButtons[r][c].setStyle("-fx-background-color: #a9a9a9;");
                }
            }
            for (String pos : horse2.getVisitedPositions()) {
                int r = Integer.parseInt(pos.split(",")[0]);
                int c = Integer.parseInt(pos.split(",")[1]);
                if (r != horse2.getRow() || c != horse2.getCol()) {
                    boardButtons[r][c].setStyle("-fx-background-color: #a9a9a9;");
                }
            }

            if (possibleMoves != null) {
                for (int[] move : possibleMoves) {
                    int r = move[0];
                    int c = move[1];
                    boardButtons[r][c].setStyle("-fx-background-color: #90ee90;");
                }
            }
        }

        if (playerRole != null) {
            if (playerRole.equals("P1")) {
                boardButtons[horse1.getRow()][horse1.getCol()].setText("♘");
                boardButtons[horse2.getRow()][horse2.getCol()].setText("♞");
            } else {
                boardButtons[horse1.getRow()][horse1.getCol()].setText("♞");
                boardButtons[horse2.getRow()][horse2.getCol()].setText("♘");
            }
        }
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

        for (String pos : horse1.getVisitedPositions()) {
            int r = Integer.parseInt(pos.split(",")[0]);
            int c = Integer.parseInt(pos.split(",")[1]);
            if (r != horse1.getRow() || c != horse1.getCol()) {
                boardButtons[r][c].setStyle("-fx-background-color: #a9a9a9;");
            }
        }
        for (String pos : horse2.getVisitedPositions()) {
            int r = Integer.parseInt(pos.split(",")[0]);
            int c = Integer.parseInt(pos.split(",")[1]);
            if (r != horse2.getRow() || c != horse2.getCol()) {
                boardButtons[r][c].setStyle("-fx-background-color: #a9a9a9;");
            }
        }

        if (playerRole != null) {
            if (playerRole.equals("P1")) {
                boardButtons[horse1.getRow()][horse1.getCol()].setText("♘");
                boardButtons[horse2.getRow()][horse2.getCol()].setText("♞");
            } else {
                boardButtons[horse1.getRow()][horse1.getCol()].setText("♞");
                boardButtons[horse2.getRow()][horse2.getCol()].setText("♘");
            }
        }
    }

    private void togglePlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private void showGameOver(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Jogo terminado");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();

            if (mainWindow != null) {
                mainWindow.close();
            }
        });
    }

    private Player getOpponent(Player player) {
        return (player == player1) ? player2 : player1;
    }

    private void logMove(String playerName, int row, int col) {
        moveLog.appendText("[Ronda " + ronda + " de " + playerName + "] - moveu para (" + row + ", " + col + ")\n");

        jogadaPar = !jogadaPar;
        if (!jogadaPar) {
            ronda++;
        }

        roundLabel.setText("Ronda: " + ronda);
    }

    @FXML
    private void onRestartGame() {
        // Envia pedido para reiniciar o jogo para o outro cliente
        client.sendMessage("RESTART_REQUEST");
        appendChat("[Sistema] Pedido de reinício enviado. Aguardando confirmação do adversário...");
        // Opcional: bloquear UI ou botão até resposta (não incluído aqui)
    }

    private void restartLocalGame() {
        // Resetar posições dos jogadores e cavalos
        player1.setPosition(4, 0);
        player2.setPosition(0, 4);

        horse1 = new Horse(4, 0);
        horse2 = new Horse(0, 4);

        currentPlayer = player1;
        isMyTurn = playerRole != null && playerRole.equals("P1");

        ronda = 1;
        jogadaPar = false;
        selectingMove = false;
        possibleMoves = null;
        currentHorse = null;

        updateCurrentPlayerDisplay();
        updateBoard();
        roundLabel.setText("Ronda: " + ronda);

        chatLog.clear();
        moveLog.clear();

        moveLog.appendText("[Sistema] Jogo reiniciado.\n");
    }
}
