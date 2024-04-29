package entry;

import com.google.common.eventbus.Subscribe;
import common.ExitRequestEvent;
import common.GlobalEventBus;
import common.ReadyToExitEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import view.StartupView;

public class App extends Application {
    Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        GlobalEventBus.register(this);
        stage.setOnCloseRequest(event -> {
            event.consume();

            GlobalEventBus.post(new ExitRequestEvent());
        });

        StartupView startupView = new StartupView(stage);
        ImageView imageView = new ImageView(new Image("logo.png"));
        stage.getIcons().add(imageView.getImage());

        startupView.display();
    }


    @Subscribe
    public void handleReadyToExitEvent(ReadyToExitEvent event) {
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}