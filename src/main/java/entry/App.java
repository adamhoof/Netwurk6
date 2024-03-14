package entry;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import view.Startup;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        Startup startup = new Startup(primaryStage);
        startup.display();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
