package controller;

import javafx.stage.Stage;
import view.SimulationWorkspaceView;
import view.StartupView;

public class ViewTransitionController {
    private final Stage stage;
    private StartupView startupView;
    private SimulationWorkspaceView simulationWorkspaceView;

    private ViewActionsListener viewActionsListener;
    private SimulationActionsListener simulationActionsListener;


    public ViewTransitionController(Stage stage, ViewActionsListener listener) {
        this.viewActionsListener = listener;
        this.stage = stage;
    }

    public void showStartupView() {
        if (startupView == null) {
            this.startupView = new StartupView(this.stage, viewActionsListener);
        }
        startupView.display();
    }

    public void showSimulationWorkspaceView() {
        if (simulationWorkspaceView == null) {
            this.simulationWorkspaceView = new SimulationWorkspaceView(this.stage, simulationActionsListener);
        }
        simulationWorkspaceView.display();
    }

    public void setViewActionsListener(ViewActionsListener listener) {
        this.viewActionsListener = listener;
    }
}
