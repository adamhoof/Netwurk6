package controller;

import javafx.stage.Stage;
import view.SimulationWorkspaceView;
import view.StartupView;

public class ViewTransitionController {
    private final Stage stage;
    private StartupView startupView;
    private SimulationWorkspaceView simulationWorkspaceView;

    public ViewTransitionController(Stage stage) {
        this.stage = stage;
    }

    public void showStartupView() {
        if (startupView == null) {
            this.startupView = new StartupView(this.stage, this);
        }
        startupView.display();
    }

    public void showSimulationWorkspaceView() {
        if (simulationWorkspaceView == null) {
            this.simulationWorkspaceView = new SimulationWorkspaceView(stage, this);
        }
        simulationWorkspaceView.display();
    }
}
