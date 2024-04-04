package model;

import common.NetworkDeviceType;

import java.util.HashSet;
import java.util.UUID;

public class SwitchModel extends NetworkDeviceModel {
    private final CAMTable camTable;
    private final HashSet<SwitchConnection> switchConnections;
    int currentAvailablePort = 0;

    public SwitchModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.SWITCH);
        this.camTable = new CAMTable();
        this.switchConnections = new HashSet<>();
    }

    public SwitchModel(UUID uuid, MACAddress macAddress, String name) {
        super(uuid, macAddress, NetworkDeviceType.SWITCH, name);
        this.camTable = new CAMTable();
        this.switchConnections = new HashSet<>();
    }


    public void learnMacAddress(MACAddress macAddress, int port) {
        camTable.addEntry(macAddress, port);
    }

    public boolean knowsMacAddress(MACAddress macAddress) {
        return camTable.containsEntry(macAddress);
    }

    public void forgetMacAddress(MACAddress macAddress) {
        camTable.removeEntry(macAddress);
    }

    public CAMTable getCamTable() {
        return camTable;
    }

    public int getPort(MACAddress macAddress) {
        for (CamEntry camEntry : camTable.getEntries()) {
            if (camEntry.macAddress == macAddress) {
                return camEntry.port;
            }
        }
        return -1;
    }

    /*public NetworkDeviceModel getDevice(MACAddress macAddress) {
        for (SwitchConnection switchConnection : switchConnections) {
            if (switchConnection.getNetworkDeviceModel().getMacAddress() == macAddress) {
                return switchConnection.networkDeviceModel;
            }
        }
    }*/

    @Override
    public boolean addConnection(NetworkDeviceModel networkDeviceModel) {
        switchConnections.add(new SwitchConnection(networkDeviceModel, currentAvailablePort++));
        return true;
    }

    public HashSet<SwitchConnection> getSwitchConnections() {
        return switchConnections;
    }
}
