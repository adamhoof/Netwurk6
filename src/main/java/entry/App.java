package entry;

import controller.ViewTransitionController;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        ViewTransitionController viewTransitionController= new ViewTransitionController(primaryStage);
        viewTransitionController.showStartupView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
