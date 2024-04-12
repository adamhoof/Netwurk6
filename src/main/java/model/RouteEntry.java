package model;

/**
 * Represents an entry in a routing table, describing a route to a particular network.
 */
public class RouteEntry {
    private final Network destinationNetwork;
    private IPAddress nextHop;
    private int hopCount;

    /**
     * Constructs a RouteEntry specifying the network, next hop IP, and hop count.
     *
     * @param destinationNetwork The destination network for this route.
     * @param nextHop            The next hop IP address towards the destination network.
     * @param hopCount           The hop count to the destination network.
     */
    public RouteEntry(Network destinationNetwork, IPAddress nextHop, int hopCount) {
        this.destinationNetwork = destinationNetwork;
        this.nextHop = nextHop;
        this.hopCount = hopCount;
    }

    /**
     * Retrieves the destination network for this route.
     *
     * @return The destination network.
     */
    public Network getDestinationNetwork() {
        return destinationNetwork;
    }

    /**
     * Retrieves the next hop IP address for this route.
     *
     * @return The next hop IP address.
     */
    public IPAddress getNextHop() {
        return nextHop;
    }

    /**
     * Retrieves the hop count for this route.
     *
     * @return The hop count.
     */
    public int getHopCount() {
        return hopCount;
    }

    /**
     * Sets the hop count for this route.
     *
     * @param hopCount The new hop count.
     */
    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    /**
     * Sets the next hop IP address for this route.
     *
     * @param nextHop The new next hop IP address.
     */
    public void setNextHop(IPAddress nextHop) {
        this.nextHop = nextHop;
    }
}
