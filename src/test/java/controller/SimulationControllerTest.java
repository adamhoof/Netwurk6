package controller;

import common.AutoNameGenerator;
import javafx.util.Pair;
import model.*;
import org.junit.jupiter.api.Test;
import view.SimulationWorkspaceView;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class SimulationControllerTest {
    @Test
    public void forwardToNextDevice_2pcsToSwitch() {
        SimulationController simulationController = new SimulationController(mock(SimulationWorkspaceView.class), mock(NetworkDeviceStorage.class), mock(NetworksController.class));
        UUID pc0Uuid = UUID.randomUUID();
        PCModel pc0 = new PCModel(pc0Uuid, new MACAddress(pc0Uuid.toString()), AutoNameGenerator.getInstance().generatePcName());

        UUID pc1Uuid = UUID.randomUUID();
        PCModel pc1 = new PCModel(pc1Uuid, new MACAddress(pc1Uuid.toString()), AutoNameGenerator.getInstance().generatePcName());

        UUID sw1Uuid = UUID.randomUUID();
        SwitchModel sw0 = new SwitchModel(sw1Uuid, new MACAddress(sw1Uuid.toString()), AutoNameGenerator.getInstance().generateSwitchName());

        assertTrue(pc0.addConnection(sw0));
        assertTrue(pc1.addConnection(sw0));

        assertTrue(sw0.addConnection(pc0));
        assertTrue(sw0.addConnection(pc1));

        Packet packet = mock(Packet.class);

        sw0.learnMacAddress(pc0.getMacAddress(), 0);
        sw0.learnMacAddress(pc1.getMacAddress(), 1);

        //send from pc0 to pc1 via sw0
        simulationController.sendPacket(new NetworkConnection(pc0, pc0.getConnection()), pc0.getMacAddress(), pc1.getMacAddress(), packet);

        //receive frame on sw0
        Pair<NetworkConnection, Frame> framePair = simulationController.receiveFrame();
        assertSame(pc0, framePair.getKey().getStartDevice());
        assertSame(sw0, framePair.getKey().getEndDevice());

        //forward from sw0 to pc1
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(sw0, framePair.getKey().getStartDevice());
        assertSame(pc1, framePair.getKey().getEndDevice());

        //no packets to handle should remain
        assertEquals(0, simulationController.queueSize());

        //send packet from pc1 to pc0 via sw0
        simulationController.sendPacket(new NetworkConnection(pc1, pc1.getConnection()), pc1.getMacAddress(), pc0.getMacAddress(), packet);
        framePair = simulationController.receiveFrame();
        assertSame(pc1,framePair.getKey().getStartDevice());
        assertSame(sw0, framePair.getKey().getEndDevice());

        //forward from sw0 to pc0
        simulationController.forwardToNextDevice(framePair.getKey(), framePair.getValue());
        framePair = simulationController.receiveFrame();
        assertSame(sw0, framePair.getKey().getStartDevice());
        assertSame(pc0, framePair.getKey().getEndDevice());
    }

    @Test
    public void testDhcpConfigurationForPcsOnDifferentSubnets() {
        NetworksController networksController = new NetworksController();
        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        SimulationWorkspaceView mockView = mock(SimulationWorkspaceView.class);
        SimulationController simulationController = new SimulationController(mockView, storage, networksController);
        MasterController masterController = new MasterController(mockView, storage, networksController, simulationController);

        UUID pc0Uuid = UUID.randomUUID();
        PCModel pc0 = new PCModel(pc0Uuid, new MACAddress(pc0Uuid.toString()), AutoNameGenerator.getInstance().generatePcName());
        UUID pc1Uuid = UUID.randomUUID();
        PCModel pc1 = new PCModel(pc1Uuid, new MACAddress(pc1Uuid.toString()), AutoNameGenerator.getInstance().generatePcName());
        UUID sw0Uuid = UUID.randomUUID();
        SwitchModel sw0 = new SwitchModel(sw0Uuid, new MACAddress(sw0Uuid.toString()), AutoNameGenerator.getInstance().generateSwitchName());
        UUID r0Uuid = UUID.randomUUID();
        RouterModel r0 = new RouterModel(r0Uuid, new MACAddress(r0Uuid.toString()), AutoNameGenerator.getInstance().generateRouterName());

        masterController.addDevice(pc0);
        masterController.addDevice(pc1);
        masterController.addDevice(sw0);
        masterController.addDevice(r0);
        assertTrue(masterController.addConnection(sw0, pc0));
        assertTrue(masterController.addConnection(r0, sw0));
        assertTrue(masterController.addConnection(r0, pc1));

        pc0 = storage.getPcModel(pc0.getUuid());
        pc1 = storage.getPcModel(pc1.getUuid());
        sw0 = storage.getSwitchModel(sw0.getUuid());
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
