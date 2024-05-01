package controller;

import common.AutoNameGenerator;
import model.MACAddress;
import model.NetworkDeviceStorage;
import model.PCModel;
import model.RouterModel;
import org.junit.jupiter.api.Test;
import view.SimulationWorkspaceView;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MasterControllerTest {

    @Test
    public void addConnection_nonExistingDevice_returnFalseConnectionNull() {
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        MasterController masterController = new MasterController(mock(SimulationWorkspaceView.class), storage, mock(NetworksController.class), mock(SimulationController.class));

        UUID pc0Uuid = UUID.randomUUID();
        PCModel pc0 = new PCModel(pc0Uuid, new MACAddress(pc0Uuid.toString()), AutoNameGenerator.getInstance().generatePcName());

        masterController.addDevice(pc0);
        pc0 = storage.getPcModel(pc0.getUuid());

        RouterModel router = mock(RouterModel.class);
        when(router.getUuid()).thenReturn(UUID.randomUUID());
        boolean result = masterController.addConnection(pc0, mock(RouterModel.class));
        assertFalse(result);
        assertNull(pc0.getConnection());
    }

    @Test
    public void addConnection_bothExistAndAcceptConnection_returnTrueConnectionNotNull() {
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        MasterController masterController = new MasterController(mock(SimulationWorkspaceView.class), storage, new NetworksController(), mock(SimulationController.class));

        UUID routerUuid = UUID.randomUUID();
        RouterModel router = new RouterModel(UUID.randomUUID(), new MACAddress(routerUuid.toString()), AutoNameGenerator.getInstance().generateRouterName());

        UUID pc0Uuid = UUID.randomUUID();
        PCModel pc0 = new PCModel(pc0Uuid, new MACAddress(pc0Uuid.toString()), AutoNameGenerator.getInstance().generatePcName());

        masterController.addDevice(router);
        masterController.addDevice(pc0);

        router = storage.getRouterModel(router.getUuid());
        pc0 = storage.getPcModel(pc0.getUuid());

        boolean result = masterController.addConnection(router, pc0);

        assertTrue(result);
        assertNotNull(pc0.getConnection());
    }

    @Test
    public void addConnection_pcDoesNotAcceptConnections_returnFalse() {
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        MasterController masterController = new MasterController(mock(SimulationWorkspaceView.class), storage, mock(NetworksController.class), mock(SimulationController.class));

        UUID routerUuid = UUID.randomUUID();
        RouterModel router = new RouterModel(routerUuid, new MACAddress(routerUuid.toString()), AutoNameGenerator.getInstance().generateRouterName());

        UUID pcUuid = UUID.randomUUID();
        PCModel pc0 = new PCModel(pcUuid, new MACAddress(pcUuid.toString()), AutoNameGenerator.getInstance().generatePcName());

        masterController.addDevice(pc0);
        masterController.addDevice(router);

        router = storage.getRouterModel(router.getUuid());
        pc0 = storage.getPcModel(pc0.getUuid());

        masterController.addConnection(router, pc0);
        boolean result = masterController.addConnection(router, pc0);

        assertFalse(result);
        assertEquals(router.getDirectConnectionLanInterface(), pc0.getConnection());
    }
}
