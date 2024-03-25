package model;

import java.util.ArrayList;
import java.util.List;

public class RoutingTable {
    private final List<RouteEntry> entries;

    public RoutingTable() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(RouteEntry entry) {
        entries.add(entry);
    }

    public void removeEntry(RouteEntry entry) {
        entries.remove(entry);
    }

    public List<RouteEntry> getEntries() {
        return entries;
    }
}
