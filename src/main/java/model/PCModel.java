package model;

import common.NetworkDeviceType;

import java.util.UUID;

public class PCModel extends NetworkDeviceModel {
    private IPAddress ipAddress;
    private IPAddress defaultGateway;
    private SubnetMask subnetMask;
    private ArpCache arpCache;

    public PCModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.PC);
        this.arpCache = new ArpCache();
    }
}