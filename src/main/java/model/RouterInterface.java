package model;

import common.NetworkDeviceType;

import java.util.HashSet;
import java.util.UUID;

public class RouterInterface extends NetworkDeviceModel {
    private final IPAddress ipAddress;
    private final MACAddress macAddress;

    private final RouterModel interfacesRouter;

    private final HashSet<NetworkDeviceModel> directConnections = new HashSet<>();

    private final Network network;

    public RouterInterface(UUID uuid, IPAddress ipAddress, MACAddress macAddress, RouterModel interfacesRouter, Network network) {
        super(uuid, macAddress, NetworkDeviceType.ROUTER_INTERFACE);
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.interfacesRouter = interfacesRouter;
        this.network = network;
    }

    public IPAddress getIpAddress() {
        return ipAddress;
    }

    public RouterModel getInterfacesRouter() {
        return interfacesRouter;
    }

    public boolean addConnection(NetworkDeviceModel networkDeviceModel) {
        directConnections.add(networkDeviceModel);
        return true;
    }

    public NetworkDeviceModel getFirstConnectedDevice(){
        return directConnections.iterator().next();
    }

    public Network getNetwork() {
        return network;
    }

    public synchronized HashSet<NetworkDeviceModel> getDirectConnections() {
        return directConnections;
    }
}
