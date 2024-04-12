package model;

/**
 * Represents a connection in a network switch, linking a network device model to a specific port.
 */
public class SwitchConnection {
    NetworkDeviceModel networkDeviceModel;
    int port;

    /**
     * Constructs a SwitchConnection linking a network device model to a port.
     *
     * @param networkDeviceModel The network device connected through this connection.
     * @param port The port number of the connection.
     */
    public SwitchConnection(NetworkDeviceModel networkDeviceModel, int port) {
        this.networkDeviceModel = networkDeviceModel;
        this.port = port;
    }

    /**
     * Retrieves the network device model associated with this connection.
     *
     * @return The connected network device model.
     */
    public NetworkDeviceModel getNetworkDeviceModel() {
        return networkDeviceModel;
    }

    /**
     * Retrieves the port number of this connection.
     *
     * @return The port number.
     */
    public int getPort() {
        return port;
    }
}
