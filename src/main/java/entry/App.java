package entry;

import javafx.application.Application;
import javafx.stage.Stage;
import view.StartupView;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        StartupView startup = new StartupView(primaryStage);
        startup.display();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
