import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
   public void start(Stage primaryStage) {
    try {
        FXMLLoader serverLoader = new FXMLLoader(getClass().getResource("ServerScene.fxml"));
        Parent rootServer = serverLoader.load();
        Stage serverStage = new Stage();
        serverStage.setTitle("Servidor - Salto do Cavalo");
        serverStage.setScene(new Scene(rootServer));
        serverStage.show();
        System.out.println("Servidor iniciado");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("initialScene.fxml"));
        System.out.println("Carregando initialScene.fxml...");
        Parent root = loader.load();
        System.out.println("initialScene.fxml carregado com sucesso!");

        Controller controller = loader.getController();

        Player player1 = new Player("Duarte", 4, 0, '1', true);
        Player player2 = new Player("André", 0, 4, '2', false);

        Horse horse1 = new Horse(4, 0);
        Horse horse2 = new Horse(0, 4);

        controller.setPlayers(player1, player2);
        controller.setHorses(horse1, horse2);
        controller.setMainWindow(primaryStage);

        primaryStage.setTitle("Salto do Cavalo");
        primaryStage.setScene(new Scene(root, 800, 800));
        primaryStage.show();
        System.out.println("Janela do tabuleiro aberta");

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public static void main(String[] args) {
        launch(args); // Only launches the SERVER scene
    }
}
