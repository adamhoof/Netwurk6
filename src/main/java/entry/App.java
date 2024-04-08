package entry;

import controller.MasterController;
import controller.SimulationController;
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
        NetworksController networksController = new NetworksController();
        NetworkDeviceStorage networkDeviceStorage = new NetworkDeviceStorage();
        SimulationController simulationController = new SimulationController(simulationWorkspaceView, networkDeviceStorage, networksController);
        MasterController masterController = new MasterController(simulationWorkspaceView, networkDeviceStorage, networksController, simulationController);

        ImageView imageView = new ImageView(new Image("logo.png"));
        primaryStage.getIcons().add(imageView.getImage());

        simulationWorkspaceView.display();
    }

    public static void main(String[] args) {
        launch(args);
    }
}