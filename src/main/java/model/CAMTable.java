package model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a CAM table which stores multiple entries of MAC addresses and their associated ports.
 */
public class CAMTable {
    private final Set<CamEntry> entries;

    /**
     * Constructs a new CAM table.
     */
    public CAMTable() {
        this.entries = new HashSet<>();
    }

    /**
     * Adds a new entry to the CAM table.
     *
     * @param macAddress the MAC address to add
     * @param port the port number associated with the MAC address
     */
    public void addEntry(MACAddress macAddress, int port) {
        entries.add(new CamEntry(macAddress, port));
    }

    /**
     * Checks if an entry for the specified MAC address exists in the CAM table.
     *
     * @param macAddress the MAC address to check for
     * @return true if the table contains an entry for the specified MAC address, otherwise false
     */
    public boolean containsEntry(MACAddress macAddress) {
        for (CamEntry camEntry : entries) {
            if (camEntry.macAddress == macAddress) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes an entry from the CAM table.
     *
     * @param macAddress the MAC address of the entry to be removed
     */
    public void removeEntry(MACAddress macAddress) {
        entries.removeIf(camEntry -> camEntry.macAddress.equals(macAddress));
    }

    /**
     * Gets all entries in the CAM table.
     *
     * @return a set containing all the entries in the CAM table
     */
    public Set<CamEntry> getEntries() {
        return entries;
    }
}
