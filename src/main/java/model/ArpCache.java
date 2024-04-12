package model;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an ARP cache, storing mappings between IP addresses and MAC addresses.
 */
public class ArpCache {
    private final ConcurrentHashMap<IPAddress, MACAddress> entries = new ConcurrentHashMap<>();

    /**
     * Retrieves the MAC address associated with the specified IP address.
     *
     * @param ipAddress the IP address whose MAC address is to be retrieved
     * @return the MAC address associated with the specified IP address
     */
    public MACAddress getMAC(IPAddress ipAddress) {
        return entries.get(ipAddress);
    }

    /**
     * Adds an entry to the ARP cache if it is not already present.
     *
     * @param ipAddress the IP address to add
     * @param mac the MAC address to associate with the IP address
     */
    public void addEntry(IPAddress ipAddress, MACAddress mac) {
        entries.putIfAbsent(ipAddress, mac);
    }
}
