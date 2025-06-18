import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ServerController {
    @FXML private Button startServerButton;
    @FXML private Label clientCountLabel;
    @FXML private TextArea logArea;

    private GameServer server;

    @FXML
    public void onStartServer() {
        log("Servidor iniciando...");
        server = new GameServer(this);
        new Thread(server::startServer).start();
    }

    public void updateClientCount(int count) {
        Platform.runLater(() -> clientCountLabel.setText(String.valueOf(count)));
    }


    public void log(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
    }
}