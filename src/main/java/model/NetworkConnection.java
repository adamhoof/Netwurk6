package model;

import common.Connection;

/**
 * Represents a connection between two network devices within the simulation.
 * Implements the Connection interface to define the connectivity between two network entities.
 */
public class NetworkConnection implements Connection {
    private final NetworkDeviceModel startDevice;
    private final NetworkDeviceModel endDevice;

    /**
     * Constructs a NetworkConnection between two network devices.
     *
     * @param first the starting device of the connection
     * @param second the ending device of the connection
     */
    public NetworkConnection(NetworkDeviceModel first, NetworkDeviceModel second){
        startDevice = first;
        endDevice = second;
    }

    @Override
    public NetworkDeviceModel getStartDevice() {
        return startDevice;
    }

    @Override
    public NetworkDeviceModel getEndDevice() {
        return endDevice;
    }
}
