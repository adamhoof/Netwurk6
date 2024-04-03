package model;

import java.util.HashSet;
import java.util.Set;

public class CAMTable {
    private Set<MACAddress> entries;

    public CAMTable() {
        this.entries = new HashSet<>();
    }

    public void addEntry(MACAddress macAddress) {
        entries.add(macAddress);
    }

    public boolean containsEntry(MACAddress macAddress) {
        return entries.contains(macAddress);
    }

    public void removeEntry(MACAddress macAddress) {
        entries.remove(macAddress);
    }

    public Set<MACAddress> getEntries() {
        return new HashSet<>(entries);
    }
}
