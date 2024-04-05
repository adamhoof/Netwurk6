package model;

import common.NetworkDeviceType;

import java.util.UUID;

public class PCModel extends NetworkDeviceModel {
    private IPAddress ipAddress;

    private Network network;
    private IPAddress defaultGateway;
    private SubnetMask subnetMask;
    private final ArpCache arpCache;

    private boolean isConfigured = false;

    private NetworkDeviceModel connection;

    public PCModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.PC);
        this.arpCache = new ArpCache();
    }

    public PCModel(UUID uuid, MACAddress macAddress, String name) {
        super(uuid, macAddress, NetworkDeviceType.PC, name);
        this.arpCache = new ArpCache();
    }

    public void connectToNetwork(Network network, IPAddress ipAddress, IPAddress defaultGateway) {
        this.network = network;
        this.ipAddress = ipAddress;
        this.defaultGateway = defaultGateway;
        this.subnetMask = network.getSubnetMask();
        isConfigured = true;
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

    public boolean isConfigured() {
        return isConfigured;
    }


    @Override
    public boolean addConnection(NetworkDeviceModel networkDeviceModel) {
        if (connection != null) {
            return false;
        }
        connection = networkDeviceModel;
        return true;
    }

    public NetworkDeviceModel getConnection(){
        return connection;
    }
}