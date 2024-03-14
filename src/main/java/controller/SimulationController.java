package controller;

public class SimulationController {
    private SimulationActionsListener listener;

    public SimulationController(SimulationActionsListener listener) {

         this.listener = listener;
    }

    public void addRouter() {

    }

    public void addSwitch() {

    }

    public void addPC() {

    }

    public void setSimulationActionsListener(SimulationActionsListener listener) {
        this.listener = listener;
    }
}
