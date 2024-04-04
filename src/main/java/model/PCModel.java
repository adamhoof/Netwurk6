package model;

import common.NetworkDeviceType;

import java.util.UUID;

public class PCModel extends NetworkDeviceModel {
    private IPAddress ipAddress;

    private Network network;
    private IPAddress defaultGateway;
    private SubnetMask subnetMask;
    private final ArpCache arpCache;

    public PCModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.PC);
        this.arpCache = new ArpCache();
    }

    public void connectToNetwork(Network network, IPAddress ipAddress, IPAddress defaultGateway) {
        this.network = network;
        this.ipAddress = ipAddress;
        this.defaultGateway = defaultGateway;
        this.subnetMask = network.getSubnetMask();
    }

    public Network getNetwork() {
        return network;
    }

    public IPAddress getIpAddress() {
        return ipAddress;
    }

    public SubnetMask getSubnetMask() {
        return subnetMask;
    }

    public ArpCache getArpCache() {
        return arpCache;
    }

    public IPAddress getDefaultGateway() {
        return defaultGateway;
    }
    @Override
    public boolean addConnection(NetworkDeviceModel networkDeviceModel) {
        if (!directConnections.isEmpty()) {
            return false;
        }
        directConnections.add(networkDeviceModel);
        return true;
    }
}