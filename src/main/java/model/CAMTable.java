package model;

import java.util.HashSet;
import java.util.Set;

public class CAMTable {
    private final Set<CamEntry> entries;

    public CAMTable() {
        this.entries = new HashSet<>();
    }

    public void addEntry(MACAddress macAddress, int port) {
        entries.add(new CamEntry(macAddress, port));
    }

    public boolean containsEntry(MACAddress macAddress) {
        for (CamEntry camEntry : entries) {
            if (camEntry.macAddress == macAddress) {
                return true;
            }
        }
        return false;
    }

    public void removeEntry(MACAddress macAddress) {
        for (CamEntry camEntry : entries) {
            if (!(camEntry.macAddress == macAddress)) {
                continue;
            }
            entries.remove(camEntry);
        }
    }

    public Set<CamEntry> getEntries() {
        return entries;
    }
}
