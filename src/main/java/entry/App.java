package entry;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import view.StartupView;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        StartupView startupView = new StartupView(primaryStage);
        ImageView imageView = new ImageView(new Image("logo.png"));
        primaryStage.getIcons().add(imageView.getImage());

        startupView.display();
    }

    public static void main(String[] args) {
        launch(args);
    }
}