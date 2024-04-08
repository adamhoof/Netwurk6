package model;

public class RipMessage implements Message {

    RoutingTable routingTable;

    public RipMessage(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }
}
