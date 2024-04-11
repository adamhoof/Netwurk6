package model;

import java.util.concurrent.ConcurrentHashMap;

public class ArpCache {
    private final ConcurrentHashMap<IPAddress, MACAddress> entries = new ConcurrentHashMap<>();

    public MACAddress getMAC(IPAddress ipAddress) {
        return entries.get(ipAddress);
    }

    public void addEntry(IPAddress ipAddress, MACAddress mac) {
        entries.putIfAbsent(ipAddress, mac);
    }
}
