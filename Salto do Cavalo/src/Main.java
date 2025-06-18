import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
   public void start(Stage primaryStage) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerScene.fxml"));
        Parent root = loader.load();
        Stage serverStage = new Stage();
        serverStage.setTitle("Servidor - Salto do Cavalo");
        serverStage.setScene(new Scene(root));
        serverStage.show();
        System.out.println("Servidor iniciado");
        System.out.println("Janela do tabuleiro aberta");

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public static void main(String[] args) {
        launch(args); // Only launches the SERVER scene
    }
}
