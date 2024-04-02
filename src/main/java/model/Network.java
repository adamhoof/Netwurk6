package model;

import java.util.ArrayList;
import java.util.HashMap;

public class Network {
    private final IPAddress networkIpAddress;
    private final SubnetMask subnetMask;

    private final NetworkType networkType;
    HashMap<Long, IPAddress> usedIpAddresses = new HashMap<>();

    ArrayList<NetworkDeviceModel> devicesInNetwork = new ArrayList<>();
    private IPAddress currentAvailableAddress;

    public Network(IPAddress networkIpAddress, SubnetMask subnetMask, NetworkType networkType) {
        this.networkIpAddress = networkIpAddress;
        this.subnetMask = subnetMask;
        this.networkType = networkType;
        this.currentAvailableAddress = IPAddress.longToIPAddress(networkIpAddress.toLong() + 1);
    }

    public void addDevice(NetworkDeviceModel networkDeviceModel) {
        devicesInNetwork.add(networkDeviceModel);
    }

    public ArrayList<NetworkDeviceModel> getDevicesInNetwork() {
        return devicesInNetwork;
    }

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

        // TODO Reset and search from the start
        currentAvailableAddress = IPAddress.longToIPAddress(currentAvailableAddress.toLong() + 1);
        return null;
    }

    public NetworkType getNetworkType() {
        return networkType;
    }

    public IPAddress getNetworkIpAddress() {
        return networkIpAddress;
    }


    private long getBroadcastAddressLong() {
        long networkAddressLong = networkIpAddress.toLong();
        long subnetMaskLong = subnetMask.toLong();
        return (networkAddressLong & subnetMaskLong) | (~subnetMaskLong & 0xFFFFFFFFL);
    }

    public SubnetMask getSubnetMask() {
        return subnetMask;
    }
}
