package model;

import java.util.ArrayList;

public class RoutingTable {
    private final ArrayList<RouteEntry> entries;

    public RoutingTable() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(RouteEntry entry) {
        entries.add(entry);
    }

    public void removeEntry(RouteEntry entry) {
        entries.remove(entry);
    }
    public ArrayList<RouteEntry> getEntries() {
        return entries;
    }
}
