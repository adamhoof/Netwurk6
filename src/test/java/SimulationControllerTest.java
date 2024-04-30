import common.AutoNameGenerator;
import controller.MasterController;
import controller.NetworksController;
import controller.SimulationController;
import javafx.util.Pair;
import model.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import view.SimulationWorkspaceView;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimulationControllerTest {
    @Test
    public void forwardToNextDevice_pcToSwitchToPc_correctlyGetMacOfSrc() {
        NetworksController networksController = new NetworksController();
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        SimulationController simulationController = new SimulationController(Mockito.mock(SimulationWorkspaceView.class), storage, networksController);
        UUID pcUuid = UUID.randomUUID();
        PCModel pcModel = new PCModel(pcUuid, new MACAddress(pcUuid.toString()), "PC1");

        UUID pcUuid2 = UUID.randomUUID();
        PCModel pcModel2 = new PCModel(pcUuid2, new MACAddress(pcUuid2.toString()), "PC2");

        UUID sw1Uuid = UUID.randomUUID();
        SwitchModel sw1Model = new SwitchModel(sw1Uuid, new MACAddress(sw1Uuid.toString()), "SW1");

        storage.add(pcModel);
        storage.add(pcModel2);
        storage.add(sw1Model);

        pcModel.addConnection(sw1Model);
        pcModel2.addConnection(sw1Model);

        sw1Model.addConnection(pcModel);
        sw1Model.addConnection(pcModel2);

        Packet packet = new Packet(null, null, new StringMessage("Hello there from PC1"));
        Packet packet2 = new Packet(null, null, new StringMessage("Hello there from P2"));

        NetworkConnection networkConnection = new NetworkConnection(pcModel, pcModel.getConnection());
        Frame frame = new Frame(pcModel.getMacAddress(), pcModel2.getMacAddress(), packet);
        simulationController.forwardToNextDevice(networkConnection, frame);

        networkConnection = new NetworkConnection(pcModel2, pcModel2.getConnection());
        frame = new Frame(pcModel2.getMacAddress(), pcModel.getMacAddress(), packet2);
        simulationController.forwardToNextDevice(networkConnection, frame);

        assertTrue(sw1Model.knowsMacAddress(pcModel.getMacAddress()) && sw1Model.knowsMacAddress(pcModel2.getMacAddress()));
    }





    @Test
    public void testDhcpConfigurationForPcsOnDifferentSubnets() {
        // Initialize controllers and mock view
        NetworksController networksController = new NetworksController();
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        SimulationWorkspaceView mockView = Mockito.mock(SimulationWorkspaceView.class);
        SimulationController simulationController = new SimulationController(mockView, storage, networksController);
        MasterController masterController = new MasterController(mockView, storage, networksController, simulationController);

        // Setup devices
        UUID pc0Uuid = UUID.randomUUID();
        PCModel pc0 = new PCModel(pc0Uuid, new MACAddress(pc0Uuid.toString()), AutoNameGenerator.getInstance().generatePcName());
        UUID pc1Uuid = UUID.randomUUID();
        PCModel pc1 = new PCModel(pc1Uuid, new MACAddress(pc1Uuid.toString()), AutoNameGenerator.getInstance().generatePcName());
        UUID sw0Uuid = UUID.randomUUID();
        SwitchModel sw0 = new SwitchModel(sw0Uuid, new MACAddress(sw0Uuid.toString()), AutoNameGenerator.getInstance().generateSwitchName());
        UUID r0Uuid = UUID.randomUUID();
        RouterModel r0 = new RouterModel(r0Uuid, new MACAddress(r0Uuid.toString()), AutoNameGenerator.getInstance().generateRouterName());

        // Add devices to master controller and connect them
        masterController.addDevice(pc0);
        masterController.addDevice(pc1);
        masterController.addDevice(sw0);
        masterController.addDevice(r0);
        assertTrue(masterController.addConnection(sw0, pc0));
        assertTrue(masterController.addConnection(r0, sw0));
        assertTrue(masterController.addConnection(r0, pc1));

        // Refresh device instances
        pc0 = storage.getPcModel(pc0.getUuid());
        pc1 = storage.getPcModel(pc1.getUuid());
        sw0 = (SwitchModel) storage.get(sw0.getUuid());
        r0 = storage.getRouterModel(r0.getUuid());

        assertEquals(2, r0.getRouterInterfaces().size());

        // Start the DHCP process for pc0
        simulationController.sendDhcpDiscovery(new NetworkConnection(pc0, pc0.getConnection()), pc0.getMacAddress());
        Pair<NetworkConnection, Frame> framePair;

        // Process frames until DHCP ACK is received by pc0
        while (true) {
            framePair = simulationController.receiveFrame();
            NetworkDeviceModel receiver = framePair.getKey().getEndDevice();
            Frame frame = framePair.getValue();

            if (frame.getPacket().getMessage() instanceof DhcpAckMessage && receiver.equals(pc0)) {
                break; // DHCP process completed for pc0
            }
            simulationController.forwardToNextDevice(framePair.getKey(), frame);
        }
        assertTrue(pc0.isConfigured());

        // Start the DHCP process for pc1
        simulationController.sendDhcpDiscovery(new NetworkConnection(pc1, pc1.getConnection()), pc1.getMacAddress());

        // Process frames until DHCP ACK is received by pc1
        while (true) {
            framePair = simulationController.receiveFrame();
            NetworkDeviceModel receiver = framePair.getKey().getEndDevice();
            Frame frame = framePair.getValue();

            if (frame.getPacket().getMessage() instanceof DhcpAckMessage && receiver.equals(pc1)) {
                break; // DHCP process completed for pc1
            }
            simulationController.forwardToNextDevice(framePair.getKey(), frame);
        }
        assertTrue(pc1.isConfigured());
    }


}
