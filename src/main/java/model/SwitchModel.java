package model;

import common.NetworkDeviceType;

import java.util.UUID;

public class SwitchModel extends NetworkDeviceModel {
    private final CAMTable camTable;

    public SwitchModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.SWITCH);
        this.camTable = new CAMTable();
    }

    public void learnMacAddress(MACAddress macAddress) {
        camTable.addEntry(macAddress);
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
    @Override
    public boolean addConnection(NetworkDeviceModel networkDeviceModel) {
        switchConnections.add(new SwitchConnection(networkDeviceModel, currentAvailablePort++));
        return true;
    }

    public HashSet<SwitchConnection> getSwitchConnections() {
        return switchConnections;
    }
}
