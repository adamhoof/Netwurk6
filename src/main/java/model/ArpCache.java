package model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ArpCache {
    private final Map<IPAddress, MACAddress> entries = new ConcurrentHashMap<>();

    public MACAddress getMAC(IPAddress ipAddress) {
        return entries.get(ipAddress);
    }

    public void addEntry(IPAddress ipAddress, MACAddress mac) {
        entries.putIfAbsent(ipAddress, mac);
    }

    public Map<IPAddress, MACAddress> getEntriesSnapshot() {
        return new ConcurrentHashMap<>(entries);
    }
}
