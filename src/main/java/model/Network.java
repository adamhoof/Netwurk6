package model;

import java.util.HashMap;
import java.util.Map;

public class Network {
    private final IPAddress networkIpAddress;
    private final SubnetMask subnetMask;

    private final NetworkType networkType;
    Map<Long, IPAddress> usedIpAddresses = new HashMap<>();
    private IPAddress currentAvailableAddress;

    public Network(IPAddress networkIpAddress, SubnetMask subnetMask, NetworkType networkType) {
        this.networkIpAddress = networkIpAddress;
        this.subnetMask = subnetMask;
        this.networkType = networkType;
        this.currentAvailableAddress = IPAddress.longToIPAddress(networkIpAddress.toLong() + 1);
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

        // TODO Reset and search from the start if needed
        currentAvailableAddress = IPAddress.longToIPAddress(currentAvailableAddress.toLong() + 1);
        return null;
    }

    public NetworkType getNetworkType(){
        return networkType;
    }

    public IPAddress getNetworkIpAddress(){
        return networkIpAddress;
    }


    private long getBroadcastAddressLong() {
        long networkAddressLong = networkIpAddress.toLong();
        long subnetMaskLong = subnetMask.toLong();
        return (networkAddressLong & subnetMaskLong) | (~subnetMaskLong & 0xFFFFFFFFL);
    }
}
