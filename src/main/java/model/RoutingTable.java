package model;

import java.util.ArrayList;

/**
 * Represents a routing table containing routes in the form of RouteEntry objects.
 */
public class RoutingTable {
    private final ArrayList<RouteEntry> entries;

    /**
     * Constructs a new empty RoutingTable.
     */
    public RoutingTable() {
        this.entries = new ArrayList<>();
    }

    /**
     * Adds a routing entry to the routing table.
     *
     * @param entry The RouteEntry to add.
     */
    public void addEntry(RouteEntry entry) {
        entries.add(entry);
    }

    /**
     * Removes a routing entry from the routing table.
     *
     * @param entry The RouteEntry to remove.
     */
    public void removeEntry(RouteEntry entry) {
        entries.remove(entry);
    }

    /**
     * Returns a list of all routing entries in the routing table.
     *
     * @return ArrayList of RouteEntry objects.
     */
    public ArrayList<RouteEntry> getEntries() {
        return entries;
    }
}
