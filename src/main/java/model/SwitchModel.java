package model;

import common.NetworkDeviceType;

import java.util.HashSet;
import java.util.UUID;

/**
 * Represents a network switch capable of connecting multiple devices and managing their connections via a CAM table.
 */
public class SwitchModel extends NetworkDeviceModel {
    private final CAMTable camTable;
    private final HashSet<SwitchConnection> switchConnections;
    int currentAvailablePort = 0;

    /**
     * Constructs a SwitchModel with a UUID and MAC address.
     *
     * @param uuid Unique identifier for the switch.
     * @param macAddress MAC address of the switch.
     */
    public SwitchModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.SWITCH);
        this.camTable = new CAMTable();
        this.switchConnections = new HashSet<>();
    }

    /**
     * Constructs a SwitchModel with a UUID, MAC address, and a name.
     *
     * @param uuid Unique identifier for the switch.
     * @param macAddress MAC address of the switch.
     * @param name The name of the switch.
     */
    public SwitchModel(UUID uuid, MACAddress macAddress, String name) {
        super(uuid, macAddress, NetworkDeviceType.SWITCH, name);
        this.camTable = new CAMTable();
        this.switchConnections = new HashSet<>();
    }

    /**
     * Learns a MAC address and associates it with a specific port in the CAM table.
     *
     * @param macAddress The MAC address to learn.
     * @param port The port number to associate with the MAC address.
     */
    public void learnMacAddress(MACAddress macAddress, int port) {
        camTable.addEntry(macAddress, port);
    }

    /**
     * Checks if the switch knows the MAC address.
     *
     * @param macAddress The MAC address to check.
     * @return true if the CAM table contains the MAC address, otherwise false.
     */
    public boolean knowsMacAddress(MACAddress macAddress) {
        return camTable.containsEntry(macAddress);
    }

    /**
     * Forgets a MAC address, removing it from the CAM table.
     *
     * @param macAddress The MAC address to forget.
     */
    public void forgetMacAddress(MACAddress macAddress) {
        camTable.removeEntry(macAddress);
    }

    /**
     * Retrieves the CAM table of the switch.
     *
     * @return The CAM table.
     */
    public CAMTable getCamTable() {
        return camTable;
    }

    /**
     * Retrieves the port number associated with a specific MAC address.
     *
     * @param macAddress The MAC address whose port number is to be retrieved.
     * @return The port number, or -1 if the MAC address is not known.
     */
    public int getPort(MACAddress macAddress) {
        for (CamEntry camEntry : camTable.getEntries()) {
            if (camEntry.macAddress == macAddress) {
                return camEntry.port;
            }
        }
        return -1;
    }

    /**
     * Adds a network device connection to the switch.
     *
     * @param networkDeviceModel The network device model to connect.
     * @return true if the connection is added successfully.
     */
    @Override
    public boolean addConnection(NetworkDeviceModel networkDeviceModel) {
        switchConnections.add(new SwitchConnection(networkDeviceModel, currentAvailablePort++));
        return true;
    }

    /**
     * Retrieves all switch connections.
     *
     * @return A set of all connections within the switch.
     */
    public HashSet<SwitchConnection> getSwitchConnections() {
        return switchConnections;
    }
}
