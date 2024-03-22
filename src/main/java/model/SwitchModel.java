package model;

import common.NetworkDeviceType;

import java.util.UUID;

public class SwitchModel extends NetworkDeviceModel {
    private CAMTable camTable;

    public SwitchModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.SWITCH);
        this.camTable = new CAMTable();
    }
}