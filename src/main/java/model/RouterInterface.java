package model;

import common.NetworkDeviceType;

import java.util.HashSet;
import java.util.UUID;

/**
 * Represents an interface on a router within the network simulation.
 * This interface connects the router to various networks and handles the interactions with other devices.
 */
public class RouterInterface extends NetworkDeviceModel {
    private final IPAddress ipAddress;
    private final MACAddress macAddress;
    private final RouterModel interfacesRouter;
    private final HashSet<NetworkDeviceModel> directConnections = new HashSet<>();
    private final Network network;

    /**
     * Constructs a RouterInterface with specified parameters.
     *
     * @param uuid             Unique identifier for this interface.
     * @param ipAddress        IP address assigned to this interface.
     * @param macAddress       MAC address assigned to this interface.
     * @param interfacesRouter Router to which this interface belongs.
     * @param network          Network to which this interface is connected.
     */
    public RouterInterface(UUID uuid, IPAddress ipAddress, MACAddress macAddress, RouterModel interfacesRouter, Network network) {
        super(uuid, macAddress, NetworkDeviceType.ROUTER_INTERFACE);
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.interfacesRouter = interfacesRouter;
        this.network = network;
    }

    /**
     * Gets the IP address of this interface.
     *
     * @return The IP address.
     */
    public IPAddress getIpAddress() {
        return ipAddress;
    }

    /**
     * Gets the router this interface is part of.
     *
     * @return The router model.
     */
    public RouterModel getInterfacesRouter() {
        return interfacesRouter;
    }

    /**
     * Adds a network device model to the direct connections of this router interface.
     *
     * @param networkDeviceModel The network device model to connect.
     * @return true if the connection was successful.
     */
    public boolean addConnection(NetworkDeviceModel networkDeviceModel) {
        directConnections.add(networkDeviceModel);
        return true;
    }

    /**
     * Retrieves the first directly connected device to this router interface.
     *
     * @return The first connected network device model.
     */
    public NetworkDeviceModel getFirstConnectedDevice() {
        return directConnections.iterator().next();
    }

    /**
     * Gets the network this router interface is connected to.
     *
     * @return The network.
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Retrieves all direct connections to this router interface.
     *
     * @return A set of all directly connected devices.
     */
    public synchronized HashSet<NetworkDeviceModel> getDirectConnections() {
        return directConnections;
    }
}
