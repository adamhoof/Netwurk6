package controller;

import javafx.scene.paint.Color;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import view.SimulationWorkspaceView;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class MasterControllerTest {

    MasterController masterController;

    @BeforeEach
    public void setup() {
        masterController = new MasterController(mock(SimulationWorkspaceView.class), mock(NetworkDeviceStorage.class), mock(NetworksController.class), mock(SimulationController.class));
    }

    @Test
    public void addConnection_sameDevice_returnFalse() {
        PCModel pc0 = mock(PCModel.class);

        UUID pc0Uuid = UUID.fromString("f51331d1-e21e-4b13-9a8d-9d8ec5bc120d");
        when(pc0.getUuid()).thenReturn(pc0Uuid);

        assertFalse(masterController.addConnection(pc0, pc0));
        verify(masterController.simulationWorkspaceView).printToLogWindow("Can't connect to itself\n", Color.RED);
    }

    @Test
    public void addConnection_bothPcs_returnFalse() {
        PCModel pc0 = new PCModel(UUID.fromString("f51331d1-e21e-4b13-9a8d-9d8ec5bc120d"), mock(MACAddress.class), "PC0");
        PCModel pc1 = new PCModel(UUID.fromString("87644cc2-835f-4377-b852-95331df95d8e"), mock(MACAddress.class), "PC1");

        assertFalse(masterController.addConnection(pc0, pc1));
        verify(masterController.simulationWorkspaceView).printToLogWindow("Can't connect PC to PC\n", Color.RED);
    }

    @Test
    public void addConnection_firstNull_returnFalse() {
        PCModel pc0 = new PCModel(UUID.fromString("f51331d1-e21e-4b13-9a8d-9d8ec5bc120d"), mock(MACAddress.class), "PC0");
        PCModel pc1 = new PCModel(UUID.fromString("87644cc2-835f-4377-b852-95331df95d8e"), mock(MACAddress.class), "PC1");
        when(masterController.deviceStorage.get(pc0.getUuid())).thenReturn(null);
        when(masterController.deviceStorage.get(pc1.getUuid())).thenReturn(pc1);

        assertFalse(masterController.addConnection(pc0, pc1));
    }

    @Test
    public void addConnection_secondNull_returnFalse() {
        PCModel pc0 = new PCModel(UUID.fromString("f51331d1-e21e-4b13-9a8d-9d8ec5bc120d"), mock(MACAddress.class), "PC0");
        PCModel pc1 = new PCModel(UUID.fromString("87644cc2-835f-4377-b852-95331df95d8e"), mock(MACAddress.class), "PC1");
        when(masterController.deviceStorage.get(pc0.getUuid())).thenReturn(pc0);
        when(masterController.deviceStorage.get(pc1.getUuid())).thenReturn(null);

        assertFalse(masterController.addConnection(pc0, pc1));
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, true"})
    public void addConnection_pcWithExistingConnection_returnFalse(boolean firstPcHasConnection, boolean secondPcHasConnection) {

        PCModel pc0 = mock(PCModel.class);
        PCModel pc1 = mock(PCModel.class);

        UUID pc0Uuid = UUID.fromString("f51331d1-e21e-4b13-9a8d-9d8ec5bc120d");
        UUID pc1Uuid = UUID.fromString("87644cc2-835f-4377-b852-95331df95d8e");

        when(pc0.getUuid()).thenReturn(pc0Uuid);
        when(pc1.getUuid()).thenReturn(pc1Uuid);

        when(masterController.deviceStorage.get(pc0.getUuid())).thenReturn(pc0);
        when(masterController.deviceStorage.get(pc1.getUuid())).thenReturn(pc1);

        when(pc0.hasConnection()).thenReturn(firstPcHasConnection);
        when(pc1.hasConnection()).thenReturn(secondPcHasConnection);

        assertFalse(masterController.addConnection(pc0, pc1));
        verify(masterController.simulationWorkspaceView).printToLogWindow("Can't connect PC to multiple networks\n", Color.RED);
    }

    @Test
    public void addConnection_bothRouters_returnTrue() {
        UUID router0Uuid = UUID.fromString("f51331d1-e21e-4b13-9a8d-9d8ec5bc120d");
        UUID router1Uuid = UUID.fromString("87644cc2-835f-4377-b852-95331df95d8e");
        RouterModel router0 = mock(RouterModel.class);
        RouterModel router1 = mock(RouterModel.class);
        when(router0.getUuid()).thenReturn(router0Uuid);
        when(router1.getUuid()).thenReturn(router1Uuid);

        RouterInterface router0WanInterface = mock(RouterInterface.class);
        RouterInterface router1WanInterface = mock(RouterInterface.class);

        when(router0.getLastRouterInterface()).thenReturn(router0WanInterface);
        when(router1.getLastRouterInterface()).thenReturn(router1WanInterface);

        when(masterController.deviceStorage.get(router0.getUuid())).thenReturn(router0);
        when(masterController.deviceStorage.get(router1.getUuid())).thenReturn(router1);

        assertTrue(masterController.addConnection(router0, router1));
        verify(masterController.networksController).createWanLink(router0, router1);
        verify(masterController.deviceStorage).addRouterInterface(router0WanInterface);
        verify(masterController.deviceStorage).addRouterInterface(router1WanInterface);
    }

    @Test
    public void addConnection_pcSwitchBothAcceptConnections_returnTrue() {
        PCModel pc0 = mock(PCModel.class);
        SwitchModel sw0 = mock(SwitchModel.class);

        UUID pc0Uuid = UUID.fromString("f51331d1-e21e-4b13-9a8d-9d8ec5bc120d");
        UUID sw0uuid = UUID.fromString("87644cc2-835f-4377-b852-95331df95d8e");

        when(pc0.getUuid()).thenReturn(pc0Uuid);
        when(sw0.getUuid()).thenReturn(sw0uuid);

        when(masterController.deviceStorage.get(pc0.getUuid())).thenReturn(pc0);
        when(masterController.deviceStorage.get(sw0.getUuid())).thenReturn(sw0);

        when(pc0.addConnection(sw0)).thenReturn(true);
        when(sw0.addConnection(pc0)).thenReturn(true);

        assertTrue(masterController.addConnection(pc0, sw0));
        verify(pc0).addConnection(sw0);
        verify(sw0).addConnection(pc0);
    }
}
