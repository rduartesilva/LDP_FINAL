import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ServerController {
    @FXML private Button startServerButton;
    @FXML private Button startGameButton;
    @FXML private Label clientCountLabel;
    @FXML private TextArea logArea;

    private GameServer server;
    private int clientCount = 0;
    private boolean gameReady = false;
    private boolean gameStarted = false;

    @FXML
    public void onStartServer() {
        log("Servidor iniciando...");
        server = new GameServer(this);
        new Thread(server::startServer).start();
    }

    @FXML
    public void onStartGame() {
        if (!gameReady) {
            log("Á espera de dois jogadores antes de iniciar o jogo.");
            return;
        }

        if (gameStarted) {
            log("O jogo já foi iniciado.");
            return;
        }

        gameStarted = true;
        log("A iniciar jogo...");
        openGameWindow();
    }

    public void updateClientCount(int count) {
        this.clientCount = count;
        Platform.runLater(() -> clientCountLabel.setText(String.valueOf(count)));

        if (clientCount >= 2) {
            gameReady = true;
            log("Dois jogadores conectados. Pronto para iniciar o jogo.");
        }
    }

    public void log(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
    }

    private void openGameWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("initialScene.fxml"));
            Parent gameRoot = loader.load();
            Stage gameStage = new Stage();
            gameStage.setTitle("Salto do Cavalo - Jogo");
            gameStage.setScene(new Scene(gameRoot));
            gameStage.show();
        } catch (IOException e) {
            log("Erro ao abrir a janela do jogo: " + e.getMessage());
        }
    }
}