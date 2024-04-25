package model;

/**
 * Represents a Routing Information Protocol (RIP) message that contains a routing table.
 * Used for exchanging routing information between routers in a network.
 */
public class RipMessage implements Message {
    private final RoutingTable routingTable;

    /**
     * Constructs a RipMessage with a specific routing table.
     *
     * @param routingTable The routing table to be included in the RIP message.
     */
    public RipMessage(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    /**
     * Retrieves the routing table contained in this RIP message.
     *
     * @return The routing table.
     */
    public RoutingTable getRoutingTable() {
        return routingTable;
    }
}
