package model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a generic network structure in a simulation environment.
 * This class provides the foundational attributes and operations for network management.
 */
public class Network {
    private final IPAddress networkIpAddress;
    private final SubnetMask subnetMask;
    private final NetworkType networkType;
    HashMap<Long, IPAddress> usedIpAddresses = new HashMap<>();
    ArrayList<NetworkDeviceModel> devicesInNetwork = new ArrayList<>();
    private IPAddress currentAvailableAddress;

    /**
     * Constructs a Network with specified network IP address, subnet mask, and type.
     *
     * @param networkIpAddress the IP address of the network
     * @param subnetMask the subnet mask of the network
     * @param networkType the type of the network (LAN, WAN, etc.)
     */
    public Network(IPAddress networkIpAddress, SubnetMask subnetMask, NetworkType networkType) {
        this.networkIpAddress = networkIpAddress;
        this.subnetMask = subnetMask;
        this.networkType = networkType;
        this.currentAvailableAddress = IPAddress.longToIPAddress(networkIpAddress.toLong() + 1);
    }

    /**
     * Adds a device to the network.
     *
     * @param networkDeviceModel the device model to be added to the network
     */
    public void addDevice(NetworkDeviceModel networkDeviceModel) {
        devicesInNetwork.add(networkDeviceModel);
    }

    public ArrayList<NetworkDeviceModel> getDevicesInNetwork() {
        return devicesInNetwork;
    }

    /**
     * Retrieves the next available IP address within the network.
     *
     * @return the next available IP address, or null if no addresses are available
     */
    public IPAddress getNextAvailableIpAddress() {
        long broadcastAddressLong = getBroadcastAddressLong();
        while (currentAvailableAddress.toLong() < broadcastAddressLong) {
            if (!usedIpAddresses.containsKey(currentAvailableAddress.toLong())) {
                IPAddress nextIp = IPAddress.longToIPAddress(currentAvailableAddress.toLong());
                usedIpAddresses.put(currentAvailableAddress.toLong(), nextIp);
                currentAvailableAddress = IPAddress.longToIPAddress(currentAvailableAddress.toLong() + 1);
                return nextIp;
            }
            currentAvailableAddress = IPAddress.longToIPAddress(currentAvailableAddress.toLong() + 1);
        }
        return null;  // Reset and search from the start if needed
    }

    public NetworkType getNetworkType() {
        return networkType;
    }

    public IPAddress getNetworkIpAddress() {
        return networkIpAddress;
    }

    /**
     * Calculates the broadcast address for the network as a long integer.
     *
     * @return the broadcast address as a long integer
     */
    private long getBroadcastAddressLong() {
        long networkAddressLong = networkIpAddress.toLong();
        long subnetMaskLong = subnetMask.toLong();
        return (networkAddressLong & subnetMaskLong) | (~subnetMaskLong & 0xFFFFFFFFL);
    }

    public SubnetMask getSubnetMask() {
        return subnetMask;
    }
}
