package entry;

import controller.MasterController;
import javafx.application.Application;
import javafx.stage.Stage;
import view.SimulationWorkspaceView;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        SimulationWorkspaceView simulationWorkspaceView = new SimulationWorkspaceView(primaryStage);
        MasterController masterController = new MasterController(simulationWorkspaceView);
        simulationWorkspaceView.display();
    }

    public static void main(String[] args) {
        launch(args);
    }
}