package Lam.camu.p100fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import Lam.camu.p100fx.controller.MainController;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Lam/camu/p100fx/primary.fxml"));
        Parent root = loader.load();

       MainController controller = loader.getController();
       controller.setPrimaryStage(primaryStage);

        Scene scene = new Scene(root, 1280, 700);
        scene.getStylesheets().add(getClass().getResource("/Lam/camu/p100fx/styles/main.css").toExternalForm());

        primaryStage.setTitle("P100FX Pixel Designer");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/Lam/camu/p100fx/images/app-icon.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}