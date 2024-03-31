import controller.MasterController;
import controller.NetworksController;
import javafx.stage.Stage;
import model.MACAddress;
import model.NetworkDeviceStorage;
import model.RouterModel;
import model.RoutingTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import view.SimulationWorkspaceView;

import java.util.UUID;

public class SimulationControllerTest {

    @Test
    public void startRip_2routers_test() {
        SimulationWorkspaceView simulationWorkspaceView = new SimulationWorkspaceView(new Stage());
        MasterController masterController = new MasterController(simulationWorkspaceView, new NetworkDeviceStorage(), new NetworksController());


        RouterModel first = new RouterModel(UUID.randomUUID(), new MACAddress(UUID.randomUUID().toString()));
        RouterModel second = new RouterModel(UUID.randomUUID(), new MACAddress(UUID.randomUUID().toString()));
        masterController.addDevice(first);
        masterController.addDevice(second);

        masterController.addConnection(first, second);

        RoutingTable expected = first.getRoutingTable();

        masterController.startSimulation();

        RoutingTable actual = first.getRoutingTable();

        Assertions.assertEquals(expected,actual);
    }
}
