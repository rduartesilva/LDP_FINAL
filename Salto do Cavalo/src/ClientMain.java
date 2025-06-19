import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("initialScene.fxml"));
        Parent root = loader.load();

        Controller controller = loader.getController();

        Player player1 = new Player("Duarte", 4, 0, '1', true);
        Player player2 = new Player("André", 0, 4, '2', false);
        Horse horse1 = new Horse(4, 0);
        Horse horse2 = new Horse(0, 4);

        controller.startGameClient();
        controller.setPlayers(player1, player2);
        controller.setHorses(horse1, horse2);


        primaryStage.setTitle("Salto do Cavalo - Jogador");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
