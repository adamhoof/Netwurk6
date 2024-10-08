import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.AutoNameGenerator;
import common.NetworkDeviceType;
import controller.MasterController;
import controller.NetworksController;
import controller.SimulationController;
import io.*;
import javafx.util.Pair;
import model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import view.SimulationWorkspaceView;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessTests {

    @Test
    public void testCommunication_2switchesInRow_CorrectlyChannelMacThrough() {
        NetworksController networksController = new NetworksController();
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        SimulationWorkspaceView mockView = Mockito.mock(SimulationWorkspaceView.class);
        SimulationController simulationController = new SimulationController(mockView, storage, networksController);
        MasterController masterController = new MasterController(mockView, storage, networksController, simulationController);

        UUID pc0Uuid = UUID.fromString("f51331d1-e21e-4b13-9a8d-9d8ec5bc120d");
        PCModel pc0 = new PCModel(pc0Uuid, new MACAddress(pc0Uuid.toString()), AutoNameGenerator.getInstance().generatePcName());

        UUID pcUuid1 = UUID.fromString("773602ea-3a72-43f3-908a-f8683b218a9e");
        PCModel pc1 = new PCModel(pcUuid1, new MACAddress(pcUuid1.toString()), AutoNameGenerator.getInstance().generatePcName());

        UUID sw0Uuid = UUID.fromString("184a5733-f06f-4b8f-a10d-b8a465cabd83");
        SwitchModel sw0 = new SwitchModel(sw0Uuid, new MACAddress(sw0Uuid.toString()), AutoNameGenerator.getInstance().generateSwitchName());

        UUID sw1Uuid = UUID.fromString("82b7c57d-fead-4bb9-8786-6fb5a2406c5b");
        SwitchModel sw1 = new SwitchModel(sw1Uuid, new MACAddress(sw1Uuid.toString()), AutoNameGenerator.getInstance().generateSwitchName());

        masterController.addDevice(pc0);
        masterController.addDevice(pc1);
        masterController.addDevice(sw0);
        masterController.addDevice(sw1);

        pc0 = storage.getPcModel(pc0.getUuid());
        pc1 = storage.getPcModel(pc1.getUuid());
        sw0 = storage.getSwitchModel(sw0Uuid);
        sw1 = storage.getSwitchModel(sw1Uuid);

        assertNotNull(pc0);
        assertNotNull(pc1);
        assertNotNull(sw0);
        assertNotNull(sw1);

        assertTrue(masterController.addConnection(pc0, sw0));
        assertTrue(masterController.addConnection(sw0, sw1));
        assertTrue(masterController.addConnection(pc1, sw1));

        assertEquals(pc0.getConnection(), sw0);
        assertEquals(pc1.getConnection(), sw1);
        SwitchModel finalSw1 = sw1;
        assertTrue(sw0.getSwitchConnections().stream().anyMatch(switchConnection -> switchConnection.getNetworkDeviceModel().equals(finalSw1)));
        PCModel finalPc = pc0;
        assertTrue(sw0.getSwitchConnections().stream().anyMatch(switchConnection -> switchConnection.getNetworkDeviceModel().equals(finalPc)));
        SwitchModel finalSw0 = sw0;
        assertTrue(sw1.getSwitchConnections().stream().anyMatch(switchConnection -> switchConnection.getNetworkDeviceModel().equals(finalSw0)));
        PCModel finalPc1 = pc1;
        assertTrue(sw1.getSwitchConnections().stream().anyMatch(switchConnection -> switchConnection.getNetworkDeviceModel().equals(finalPc1)));

        Packet mockPacket = Mockito.mock(Packet.class);

        //send packet from pc0 to its only connection(sw0)
        NetworkConnection networkConnection = new NetworkConnection(pc0, pc0.getConnection());
        simulationController.sendPacket(networkConnection, pc0.getMacAddress(), pc1.getMacAddress(), mockPacket);

        //receive frame from pc0 to sw00
        Pair<NetworkConnection, Frame> framePair = simulationController.receiveFrame();
        assertSame(pc0, framePair.getKey().getStartDevice());
        assertSame(sw0, framePair.getKey().getEndDevice());

        //forward from sw0 to sw1
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(sw0, framePair.getKey().getStartDevice());
        assertSame(sw1, framePair.getKey().getEndDevice());
        assertTrue(sw0.knowsMacAddress(pc0.getMacAddress()));

        //forward from sw1 to pc1
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(sw1, framePair.getKey().getStartDevice());
        assertSame(pc1, framePair.getKey().getEndDevice());
        assertFalse(sw1.knowsMacAddress(pc1.getMacAddress()));
        assertTrue(sw1.knowsMacAddress(pc0.getMacAddress()));
    }

    @Test
    public void testDORA_pcToSwitchToRouter_successfulCommunication() {
        NetworksController networksController = new NetworksController();
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        SimulationWorkspaceView mockView = Mockito.mock(SimulationWorkspaceView.class);
        SimulationController simulationController = new SimulationController(mockView, storage, networksController);
        MasterController masterController = new MasterController(mockView, storage, networksController, simulationController);

        UUID pcUuid = UUID.fromString("f51331d1-e21e-4b13-9a8d-9d8ec5bc120d");
        PCModel pc0 = new PCModel(pcUuid, new MACAddress(pcUuid.toString()), "PC0");

        UUID sw0Uuid = UUID.fromString("773602ea-3a72-43f3-908a-f8683b218a9e");
        SwitchModel sw0 = new SwitchModel(sw0Uuid, new MACAddress(sw0Uuid.toString()), "SWITCH0");

        UUID routerUuid = UUID.fromString("184a5733-f06f-4b8f-a10d-b8a465cabd83");
        RouterModel router0 = new RouterModel(routerUuid, new MACAddress(routerUuid.toString()), "ROUTER0");

        masterController.addDevice(pc0);
        masterController.addDevice(sw0);
        masterController.addDevice(router0);

        pc0 = storage.getPcModel(pc0.getUuid());
        sw0 = storage.getSwitchModel(sw0.getUuid());
        router0 = storage.getRouterModel(router0.getUuid());

        assertNotNull(pc0);
        assertNotNull(sw0);
        assertNotNull(router0);

        assertEquals(1, router0.getRouterInterfaces().size());
        assertTrue(masterController.addConnection(sw0, pc0));
        assertTrue(masterController.addConnection(router0, sw0));
        assertEquals(2, router0.getRouterInterfaces().size());
        assertEquals(2, router0.getLanNetworks().size());

        //the one at index 0 is default LAN, therefore at index 1 is definitely the one just added
        Network network = router0.getLanNetworks().get(1);
        RouterInterface routerInterface = router0.getNetworksRouterInterface(network);

        //pc0 initiates communication with its only connection (sw)
        simulationController.sendDhcpDiscovery(new NetworkConnection(pc0, pc0.getConnection()), pc0.getMacAddress());
        pc0.setConfigurationInProgress();
        Pair<NetworkConnection, Frame> framePair = simulationController.receiveFrame();
        assertEquals(pc0, framePair.getKey().getStartDevice());
        assertEquals(sw0, framePair.getKey().getEndDevice());
        assertTrue(pc0.isConfigurationInProgress());

        //sw0 broadcast communication router0
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(sw0, framePair.getKey().getStartDevice());
        assertSame(routerInterface, framePair.getKey().getEndDevice());

        assertTrue(sw0.knowsMacAddress(pc0.getMacAddress()));

        //router interface sends dhcp offer message to sw
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(routerInterface, framePair.getKey().getStartDevice());
        assertSame(sw0, framePair.getKey().getEndDevice());
        assertInstanceOf(DhcpOfferMessage.class, framePair.getValue().getPacket().getMessage());

        //sw forwards dhcp offer message to pc0
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(sw0, framePair.getKey().getStartDevice());
        assertSame(pc0, framePair.getKey().getEndDevice());
        assertTrue(sw0.knowsMacAddress(routerInterface.getMacAddress()));

        //pc0 sends dhcp response back and configures itself
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(pc0, framePair.getKey().getStartDevice());
        assertSame(sw0, framePair.getKey().getEndDevice());
        assertTrue(pc0.isConfigured());
        assertInstanceOf(DhcpResponseMessage.class, framePair.getValue().getPacket().getMessage());

        //sw already knows mac of router interface, no need to broadcast
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(sw0, framePair.getKey().getStartDevice());
        assertSame(routerInterface, framePair.getKey().getEndDevice());

        //router interface sends dhcp ack message targeted to pc0
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(routerInterface, framePair.getKey().getStartDevice());
        assertSame(sw0, framePair.getKey().getEndDevice());
        assertInstanceOf(DhcpAckMessage.class, framePair.getValue().getPacket().getMessage());

        //sw already knows mac of pc0, no need to broadcast
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(sw0, framePair.getKey().getStartDevice());
        assertSame(pc0, framePair.getKey().getEndDevice());

        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
    }

    @Test
    public void testIp_DifferentSubnetsPcs_successfulCommunication() {
        NetworksController networksController = new NetworksController();
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        SimulationWorkspaceView mockView = Mockito.mock(SimulationWorkspaceView.class);
        SimulationController simulationController = new SimulationController(mockView, storage, networksController);
        MasterController masterController = new MasterController(mockView, storage, networksController, simulationController);

        UUID pc0Uuid = UUID.randomUUID();
        PCModel pc0 = new PCModel(pc0Uuid, new MACAddress(pc0Uuid.toString()), AutoNameGenerator.getInstance().generatePcName());
        UUID pc1Uuid = UUID.randomUUID();
        PCModel pc1 = new PCModel(pc1Uuid, new MACAddress(pc1Uuid.toString()), AutoNameGenerator.getInstance().generatePcName());
        UUID sw0Uuid = UUID.randomUUID();
        SwitchModel sw0 = new SwitchModel(sw0Uuid, new MACAddress(sw0Uuid.toString()), AutoNameGenerator.getInstance().generateSwitchName());
        UUID r0Uuid = UUID.randomUUID();
        RouterModel router = new RouterModel(r0Uuid, new MACAddress(r0Uuid.toString()), AutoNameGenerator.getInstance().generateRouterName());

        masterController.addDevice(pc0);
        masterController.addDevice(pc1);
        masterController.addDevice(sw0);
        masterController.addDevice(router);

        pc0 = storage.getPcModel(pc0.getUuid());
        pc1 = storage.getPcModel(pc1.getUuid());
        sw0 = storage.getSwitchModel(sw0.getUuid());
        router = storage.getRouterModel(router.getUuid());

        assertTrue(masterController.addConnection(sw0, pc0));
        //router0 to sw0 connection creates new subnet that a dedicated router interface handles
        assertTrue(masterController.addConnection(router, sw0));
        //router0 to pc1 connection does not create new dedicated router interface, pcs always connect to default router interfaces when connected directly to router
        assertTrue(masterController.addConnection(router, pc1));

        //retrieve respective router interfaces for LANs
        RouterInterface defaultRouterInterface = router.getDirectConnectionLanInterface();
        RouterInterface subnetRouterInterface = router.getLastRouterInterface();

        //check whether the router connections propagated correctly to the respective router interfaces
        assertSame(defaultRouterInterface, router.getDirectConnectionLanInterface());
        assertTrue(sw0.getSwitchConnections().stream().anyMatch(switchConnection -> switchConnection.getNetworkDeviceModel().equals(subnetRouterInterface)));

        //check connections
        PCModel finalPc = pc0;
        assertTrue(sw0.getSwitchConnections().stream().anyMatch(switchConnection -> switchConnection.getNetworkDeviceModel().equals(finalPc)));
        assertSame(pc0.getConnection(), sw0);
        assertSame(pc1.getConnection(), defaultRouterInterface);

        Network defaultLan = defaultRouterInterface.getNetwork();
        Network subnetLan = subnetRouterInterface.getNetwork();

        //manually configure pc0 (skip DHCP DORA process and ARP)
        pc0.setIpAddress(networksController.reserveIpAddressInNetwork(subnetLan));
        pc0.setSubnetMask(subnetLan.getSubnetMask());
        pc0.setDefaultGateway(subnetRouterInterface.getIpAddress());
        //manually configure pc1 (skip DHCP DORA process and ARP)
        pc1.setIpAddress(networksController.reserveIpAddressInNetwork(defaultLan));
        pc1.setSubnetMask(defaultLan.getSubnetMask());
        pc1.setDefaultGateway(defaultRouterInterface.getIpAddress());

        //manually update ARP for router
        router.updateArp(pc0.getIpAddress(), pc0.getMacAddress());
        router.updateArp(pc1.getIpAddress(), pc1.getMacAddress());

        //send packet from pc0 over sw to pc1
        simulationController.sendPacket(new NetworkConnection(pc0, pc0.getConnection()), pc0.getMacAddress(), pc1.getMacAddress(),
                new Packet(pc0.getIpAddress(), pc1.getIpAddress(), new StringMessage("Yeet")));
        Pair<NetworkConnection, Frame> framePair = simulationController.receiveFrame();
        assertSame(pc0, framePair.getKey().getStartDevice());
        assertSame(sw0, framePair.getKey().getEndDevice());

        //
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(sw0, framePair.getKey().getStartDevice());
        assertSame(subnetRouterInterface, framePair.getKey().getEndDevice());

        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(defaultRouterInterface, framePair.getKey().getStartDevice());
        assertSame(pc1, framePair.getKey().getEndDevice());

        assertInstanceOf(StringMessage.class, framePair.getValue().getPacket().getMessage());
    }

    @Test
    public void testExportImport_standardConfiguration_successfulOperation(@TempDir Path tempDir) {
        JsonExporter jsonExporter = new JsonExporter();
        JsonImporter jsonImporter = new JsonImporter();

        NetworkDeviceViewDTO pcDTO = new NetworkDeviceViewDTO(UUID.randomUUID(), "PC0", 0.0, 0.0, NetworkDeviceType.PC);
        NetworkDeviceViewDTO switchDTO = new NetworkDeviceViewDTO(UUID.randomUUID(), "SWITCH0", 0.0, 0.0, NetworkDeviceType.SWITCH);
        NetworkDeviceViewDTO routerDTO = new NetworkDeviceViewDTO(UUID.randomUUID(), "ROUTER0", 0.0, 0.0, NetworkDeviceType.ROUTER);

        ConnectionLineDTO pcSwitchConnectionLineDTO = new ConnectionLineDTO(pcDTO.uuid(), switchDTO.uuid());
        ConnectionLineDTO switchRouterConnectionLineDTO = new ConnectionLineDTO(switchDTO.uuid(), routerDTO.uuid());

        ArrayList<NetworkDeviceViewDTO> networkDeviceViewDTOs = new ArrayList<>();
        ArrayList<ConnectionLineDTO> connectionLineDTOs = new ArrayList<>();

        networkDeviceViewDTOs.add(pcDTO);
        networkDeviceViewDTOs.add(switchDTO);
        networkDeviceViewDTOs.add(routerDTO);

        connectionLineDTOs.add(pcSwitchConnectionLineDTO);
        connectionLineDTOs.add(switchRouterConnectionLineDTO);

        AutoNameGenerator.getInstance().setPcNextAvailableNumber(1);
        AutoNameGenerator.getInstance().setSwitchNextAvailableNumber(1);
        AutoNameGenerator.getInstance().setRouterNextAvailableNumber(1);
        AutoNameGenerator.getInstance().setRouterInterfaceNextAvailableNumber(3);

        DTOConvertor dtoConvertor = new DTOConvertor();
        AutoNameGeneratorDTO autoNameGeneratorDTO = dtoConvertor.convertAutoNameGeneratorToDTO(AutoNameGenerator.getInstance());

        File file = tempDir.resolve("test.json").toFile();

        assertDoesNotThrow(() -> jsonExporter.exportNetworkData(
                networkDeviceViewDTOs,
                connectionLineDTOs,
                autoNameGeneratorDTO,
                file)
        );

        assertTrue(file.exists());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualObj = assertDoesNotThrow(() -> objectMapper.readTree(file));
        JsonNode expectedObj = assertDoesNotThrow(() -> objectMapper.readTree(
                "{\"devices\":[" +
                        "{\"uuid\":\"" + pcDTO.uuid() + "\",\"name\":\"PC0\",\"x\":0.0,\"y\":0.0,\"type\":\"PC\"}," +
                        "{\"uuid\":\"" + switchDTO.uuid() + "\",\"name\":\"SWITCH0\",\"x\":0.0,\"y\":0.0,\"type\":\"SWITCH\"}," +
                        "{\"uuid\":\"" + routerDTO.uuid() + "\",\"name\":\"ROUTER0\",\"x\":0.0,\"y\":0.0,\"type\":\"ROUTER\"}]," +
                        "\"connections\":[" +
                        "{\"startDeviceId\":\"" + pcDTO.uuid() + "\",\"endDeviceId\":\"" + switchDTO.uuid() + "\"}," +
                        "{\"startDeviceId\":\"" + switchDTO.uuid() + "\",\"endDeviceId\":\"" + routerDTO.uuid() + "\"}]," +
                        "\"autoNameGeneratorDTO\":" +
                        "{\"routerNameCounter\":1," +
                        "\"switchNameCounter\":1," +
                        "\"routerInterfaceNameCounter\":3," +
                        "\"pcNameCounter\":1}}"));

        assertEquals(expectedObj, actualObj);

        NetworkData importedNetworkData = jsonImporter.importNetworkData(file);
        assertEquals(networkDeviceViewDTOs, importedNetworkData.devices(), "The devices should match and be in the same order.");
        assertEquals(connectionLineDTOs, importedNetworkData.connections(), "The connections should match and be in the same order.");
        assertEquals(autoNameGeneratorDTO, importedNetworkData.autoNameGeneratorDTO(), "The auto name generator values should match.");
    }
}
