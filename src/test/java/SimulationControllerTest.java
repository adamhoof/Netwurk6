import common.AutoNameGenerator;
import controller.MasterController;
import controller.NetworksController;
import controller.SimulationController;
import javafx.util.Pair;
import model.*;
import org.junit.jupiter.api.Assertions;
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
        PCModel pcModel = new PCModel(pcUuid, new MACAddress("PC1_MAC"), "PC1");

        UUID pcUuid2 = UUID.randomUUID();
        PCModel pcModel2 = new PCModel(pcUuid2, new MACAddress("PC2_MAC"), "PC2");

        UUID sw1Uuid = UUID.randomUUID();
        SwitchModel sw1Model = new SwitchModel(sw1Uuid, new MACAddress("SW1_MAC"), "SW1");

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
    public void forwardToNextDevice_2switches_correctlyGetMacOfSrc() {
        NetworksController networksController = new NetworksController();
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        SimulationWorkspaceView mockView = Mockito.mock(SimulationWorkspaceView.class);
        SimulationController simulationController = new SimulationController(mockView, storage, networksController);
        MasterController masterController = new MasterController(mockView, storage, networksController, simulationController);

        UUID pcUuid = UUID.randomUUID();
        PCModel pc0 = new PCModel(pcUuid, new MACAddress(pcUuid.toString()), AutoNameGenerator.generatePcName());

        UUID pcUuid2 = UUID.randomUUID();
        PCModel pc1 = new PCModel(pcUuid2, new MACAddress(pcUuid2.toString()), AutoNameGenerator.generatePcName());

        UUID sw1Uuid = UUID.randomUUID();
        SwitchModel sw0 = new SwitchModel(sw1Uuid, new MACAddress(sw1Uuid.toString()), AutoNameGenerator.generateSwitchName());

        UUID sw2Uuid = UUID.randomUUID();
        SwitchModel sw1 = new SwitchModel(sw2Uuid, new MACAddress(sw2Uuid.toString()), AutoNameGenerator.generateSwitchName());

        masterController.addDevice(pc0);
        masterController.addDevice(pc1);
        masterController.addDevice(sw0);
        masterController.addDevice(sw1);

        masterController.addConnection(pc0, sw0);
        masterController.addConnection(sw0, sw1);
        masterController.addConnection(pc1, sw1);

        pc0 = storage.getPcModel(pc0.getUuid());
        pc1 = storage.getPcModel(pc1.getUuid());
        sw0 = (SwitchModel) storage.get(sw1Uuid);
        sw1 = (SwitchModel) storage.get(sw2Uuid);

        Packet mockPacket = Mockito.mock(Packet.class);

        //send packet from pc0 to its only connection(switch)
        NetworkConnection networkConnection = new NetworkConnection(pc0, pc0.getConnection());
        simulationController.sendPacket(networkConnection, pc0.getMacAddress(), pc1.getMacAddress(), mockPacket);

        //receive frame from pc0 to switch0
        Pair<NetworkConnection, Frame> framePair = simulationController.receiveFrame();
        Assertions.assertSame(pc0, framePair.getKey().getStartDevice());
        Assertions.assertSame(sw0, framePair.getKey().getEndDevice());

        //forward from switch0 to switch1
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        Assertions.assertSame(sw0, framePair.getKey().getStartDevice());
        Assertions.assertSame(sw1, framePair.getKey().getEndDevice());
        assertTrue(sw0.knowsMacAddress(pc0.getMacAddress()));

        //forward from switch1 to pc1
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        Assertions.assertSame(sw1, framePair.getKey().getStartDevice());
        Assertions.assertSame(pc1, framePair.getKey().getEndDevice());
        Assertions.assertFalse(sw1.knowsMacAddress(pc1.getMacAddress()));
        assertTrue(sw1.knowsMacAddress(pc0.getMacAddress()));
    }

    @Test
    public void testFlow_2pcsToSwitchToRouter() {
        NetworksController networksController = new NetworksController();
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        SimulationWorkspaceView mockView = Mockito.mock(SimulationWorkspaceView.class);
        SimulationController simulationController = new SimulationController(mockView, storage, networksController);
        MasterController masterController = new MasterController(mockView, storage, networksController, simulationController);

        UUID pcUuid = UUID.randomUUID();
        PCModel pcModel = new PCModel(pcUuid, new MACAddress(pcUuid.toString()), AutoNameGenerator.generatePcName());

        UUID pcUuid2 = UUID.randomUUID();
        PCModel pcModel2 = new PCModel(pcUuid2, new MACAddress(pcUuid2.toString()), AutoNameGenerator.generatePcName());

        UUID sw1Uuid = UUID.randomUUID();
        SwitchModel sw = new SwitchModel(sw1Uuid, new MACAddress(sw1Uuid.toString()), AutoNameGenerator.generateSwitchName());

        UUID routerUuid = UUID.randomUUID();
        RouterModel router = new RouterModel(routerUuid, new MACAddress(routerUuid.toString()), AutoNameGenerator.generateRouterName());

        masterController.addDevice(pcModel);
        masterController.addDevice(pcModel);
        masterController.addDevice(pcModel2);
        masterController.addDevice(sw);
        masterController.addDevice(router);

        pcModel = storage.getPcModel(pcModel.getUuid());
        pcModel2 = storage.getPcModel(pcModel2.getUuid());
        sw = (SwitchModel) storage.get(sw.getUuid());
        router = storage.getRouterModel(router.getUuid());

        assertTrue(masterController.addConnection(sw, pcModel));
        assertTrue(masterController.addConnection(router, sw));
        assertTrue(masterController.addConnection(sw, pcModel2));
        assertEquals(2, router.getRouterInterfaces().size());

        Network network = router.getLanNetworks().get(1);
        RouterInterface routerInterface = router.getNetworksRouterInterface(network);

        //PC initiates communication with its only connection (switch)
        simulationController.sendDhcpDiscovery(new NetworkConnection(pcModel, pcModel.getConnection()), pcModel.getMacAddress());
        pcModel.setConfigurationInProgress();
        Pair<NetworkConnection, Frame> framePair = simulationController.receiveFrame();
        assertEquals(framePair.getKey().getStartDevice(), pcModel);
        assertEquals(framePair.getKey().getEndDevice(), sw);
        assertTrue(pcModel.isConfigurationInProgress());

        //Switch broadcast communication to pc2 and router, pc2 ignore
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        //If pc2 is the ony who got it first, throw the frame away and get the one directed to router interface instead
        if (framePair.getKey().getEndDevice() instanceof PCModel) {
            framePair = simulationController.receiveFrame();
        } else {
            simulationController.receiveFrame();
        }

        Assertions.assertSame(framePair.getKey().getStartDevice(), sw);
        Assertions.assertSame(routerInterface, framePair.getKey().getEndDevice());

        //after sending it to both connected devices, switch should know how to get to pc
        assertTrue(sw.knowsMacAddress(pcModel.getMacAddress()));

        //Router interface send dhcp offer message to switch
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        Assertions.assertSame(routerInterface, framePair.getKey().getStartDevice());
        Assertions.assertSame(framePair.getKey().getEndDevice(), sw);
        Assertions.assertInstanceOf(DhcpOfferMessage.class, framePair.getValue().getPacket().getMessage());

        //Switch forwards dhcp offer message to pc
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        Assertions.assertSame(framePair.getKey().getEndDevice(), pcModel);
        Assertions.assertSame(sw, framePair.getKey().getStartDevice());
        assertTrue(sw.knowsMacAddress(routerInterface.getMacAddress()));

        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        simulationController.receiveFrame();
        assertTrue(pcModel.isConfigured());
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
        PCModel pc0 = new PCModel(pc0Uuid, new MACAddress(pc0Uuid.toString()), AutoNameGenerator.generatePcName());
        UUID pc1Uuid = UUID.randomUUID();
        PCModel pc1 = new PCModel(pc1Uuid, new MACAddress(pc1Uuid.toString()), AutoNameGenerator.generatePcName());
        UUID sw0Uuid = UUID.randomUUID();
        SwitchModel sw0 = new SwitchModel(sw0Uuid, new MACAddress(sw0Uuid.toString()), AutoNameGenerator.generateSwitchName());
        UUID r0Uuid = UUID.randomUUID();
        RouterModel r0 = new RouterModel(r0Uuid, new MACAddress(r0Uuid.toString()), AutoNameGenerator.generateRouterName());

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

    @Test
    public void testCommunicationBetweenDevicesInDifferentSubnets() {
        // Initialize controllers and mock view
        NetworksController networksController = new NetworksController();
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        SimulationWorkspaceView mockView = Mockito.mock(SimulationWorkspaceView.class);
        SimulationController simulationController = new SimulationController(mockView, storage, networksController);
        MasterController masterController = new MasterController(mockView, storage, networksController, simulationController);

        // Setup devices
        UUID pc0Uuid = UUID.randomUUID();
        PCModel pc0 = new PCModel(pc0Uuid, new MACAddress(pc0Uuid.toString()), AutoNameGenerator.generatePcName());
        UUID pc1Uuid = UUID.randomUUID();
        PCModel pc1 = new PCModel(pc1Uuid, new MACAddress(pc1Uuid.toString()), AutoNameGenerator.generatePcName());
        UUID sw0Uuid = UUID.randomUUID();
        SwitchModel sw0 = new SwitchModel(sw0Uuid, new MACAddress(sw0Uuid.toString()), AutoNameGenerator.generateSwitchName());
        UUID r0Uuid = UUID.randomUUID();
        RouterModel r0 = new RouterModel(r0Uuid, new MACAddress(r0Uuid.toString()), AutoNameGenerator.generateRouterName());

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

        // Start the DHCP process for pc0
        simulationController.sendDhcpDiscovery(new NetworkConnection(pc0, pc0.getConnection()), pc0.getMacAddress());

        // Process frames until DHCP ACK is received by pc0
        while (true) {
            Pair<NetworkConnection, Frame> framePair = simulationController.receiveFrame();
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
            Pair<NetworkConnection, Frame> framePair = simulationController.receiveFrame();
            NetworkDeviceModel receiver = framePair.getKey().getEndDevice();
            Frame frame = framePair.getValue();

            if (frame.getPacket().getMessage() instanceof DhcpAckMessage && receiver.equals(pc1)) {
                break; // DHCP process completed for pc1
            }
            simulationController.forwardToNextDevice(framePair.getKey(), frame);
        }
        assertTrue(pc1.isConfigured());

        // Simulate communication from PC0 to PC1


        simulationController.initiateCommunication(pc0, pc1);


        // Process frames until PC1 receives the message
        while (true) {
            Pair<NetworkConnection, Frame> framePair = simulationController.receiveFrame();
            NetworkDeviceModel receiver = framePair.getKey().getEndDevice();
            Frame frame = framePair.getValue();

            Message message = frame.getPacket().getMessage();
            if (receiver == r0.getRouterInterfaces().values().stream().toList().get(1) && message instanceof StringMessage) {
                assertEquals(frame.getSourceMac(),pc0.getMacAddress());
                assertEquals(frame.getPacket().getDestinationIp(),r0.getDirectConnectionLanInterface().getIpAddress());
            } else if (message instanceof ArpResponseMessage){
                System.out.println("Fuck");
            }

            simulationController.forwardToNextDevice(framePair.getKey(), frame);
        }
    }
}
