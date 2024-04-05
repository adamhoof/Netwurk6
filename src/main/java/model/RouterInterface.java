package model;

import common.NetworkDeviceType;

import java.util.HashSet;
import java.util.UUID;

public class RouterInterface extends NetworkDeviceModel {
    private final IPAddress ipAddress;
    private final MACAddress macAddress;

    private final RouterModel interfacesRouter;

    private final HashSet<NetworkDeviceModel> directConnections = new HashSet<>();

    public RouterInterface(UUID uuid, IPAddress ipAddress, MACAddress macAddress, RouterModel interfacesRouter) {
        super(uuid, macAddress, NetworkDeviceType.ROUTER_INTERFACE);
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.interfacesRouter = interfacesRouter;
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
}
