package controller;

import view.SimulationWorkspaceView;

public class MasterController {
    SimulationWorkspaceView simulationWorkspaceView;

    public MasterController(SimulationWorkspaceView simulationWorkspaceView) {
        this.simulationWorkspaceView = simulationWorkspaceView;
        this.simulationWorkspaceView.setController(this);
    }
}
