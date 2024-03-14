package controller;

import javafx.stage.Stage;

public class MasterController implements ViewActionsListener, SimulationActionsListener{
    private final ViewTransitionController viewTransitionController;
    private final SimulationController simulationController;

    public MasterController(Stage stage) {
        this.viewTransitionController = new ViewTransitionController(stage, this);
        this.simulationController = new SimulationController(this);

        this.viewTransitionController.setViewActionsListener(this);
        this.simulationController.setSimulationActionsListener(this);
    }
    @Override
    public void showStartupView() {
        viewTransitionController.showStartupView();
    }

    @Override
    public void showSimulationWorkspaceView() {
        viewTransitionController.showSimulationWorkspaceView();
    }

    @Override
    public void addRouter() {
        simulationController.addRouter();
    }

    @Override
    public void addSwitch() {

    }

    @Override
    public void addPC() {

    }
}
