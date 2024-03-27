package entry;

import controller.MasterController;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.NetworkDeviceStorage;
import controller.NetworksController;
import view.SimulationWorkspaceView;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        SimulationWorkspaceView simulationWorkspaceView = new SimulationWorkspaceView(primaryStage);
        MasterController masterController = new MasterController(simulationWorkspaceView, new NetworkDeviceStorage(), new NetworksController());

        ImageView imageView = new ImageView(new Image("logo.png"));
        primaryStage.getIcons().add(imageView.getImage());

        simulationWorkspaceView.display();
    }

    public static void main(String[] args) {
        launch(args);
    }
}