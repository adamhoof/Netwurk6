import controller.MasterController;
import controller.NetworksController;
import controller.SimulationController;
import javafx.scene.image.Image;
import model.MACAddress;
import model.NetworkDeviceStorage;
import model.PCModel;
import model.RouterModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import view.PCView;
import view.RouterView;
import view.SimulationWorkspaceView;

import java.util.UUID;

public class MasterControllerTest {

    @Test
    public void addConnection_nonExistingDevice_returnFalseConnectionNull() {
        SimulationWorkspaceView mockView = Mockito.mock(SimulationWorkspaceView.class);
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        MasterController masterController = new MasterController(mockView, storage, new NetworksController(), Mockito.mock(SimulationController.class));
        Image mockImage = Mockito.mock(Image.class);

        PCView pcView = new PCView(UUID.randomUUID(),mockImage);
        masterController.addDevice(pcView);
        PCModel pcModel = storage.getPcModel(pcView.getUuid());

        boolean result = masterController.addConnection(new RouterModel(UUID.randomUUID(), new MACAddress(UUID.randomUUID().toString())), pcModel);
        Assertions.assertFalse(result);
        Assertions.assertNull(pcModel.getConnection());
    }

    @Test
    public void addConnection_bothExistAndAcceptConnection_returnTrueConnectionNotNull() {
        SimulationWorkspaceView mockView = Mockito.mock(SimulationWorkspaceView.class);
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        MasterController masterController = new MasterController(mockView, storage, Mockito.mock(NetworksController.class), Mockito.mock(SimulationController.class));
        Image mockImage = Mockito.mock(Image.class);

        RouterView routerView = new RouterView(UUID.randomUUID(),mockImage);
        PCView pcView = new PCView(UUID.randomUUID(),mockImage);
        masterController.addDevice(routerView);
        masterController.addDevice(pcView);
        RouterModel routerModel = storage.getRouterModel(routerView.getUuid());
        PCModel pcModel = storage.getPcModel(pcView.getUuid());

        boolean result = masterController.addConnection(routerModel, pcModel);

        Assertions.assertTrue(result);
        Assertions.assertNotNull(pcModel.getConnection());
    }

    @Test
    public void addConnection_pcDoesNotAcceptConnections_returnFalse() {
        SimulationWorkspaceView mockView = Mockito.mock(SimulationWorkspaceView.class);
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        MasterController masterController = new MasterController(mockView, storage, Mockito.mock(NetworksController.class), Mockito.mock(SimulationController.class));
        Image mockImage = Mockito.mock(Image.class);

        RouterView routerView = new RouterView(UUID.randomUUID(),mockImage);
        PCView pcView = new PCView(UUID.randomUUID(),mockImage);
        masterController.addDevice(routerView);
        masterController.addDevice(pcView);
        RouterModel routerModel = storage.getRouterModel(routerView.getUuid());
        PCModel pcModel = storage.getPcModel(pcView.getUuid());

        masterController.addConnection(routerModel, pcModel);
        boolean result = masterController.addConnection(routerModel, pcModel);

        Assertions.assertFalse(result);
        Assertions.assertNotNull(pcModel.getConnection());
    }
}
