package entry;

import controller.MasterController;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        MasterController masterController = new MasterController(primaryStage);
        masterController.showStartupView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
