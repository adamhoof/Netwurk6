package model;

public class RouteEntry {
    private final Network destinationNetwork;
    private IPAddress nextHop;
    private int hopCount;

    public RouteEntry(Network destinationNetwork, IPAddress nextHop, int hopCount) {
        this.destinationNetwork = destinationNetwork;
        this.nextHop = nextHop;
        this.hopCount = hopCount;
    }

    public Network getDestinationNetwork() {
        return destinationNetwork;
    }

    public IPAddress getNextHop() {
        return nextHop;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount){
        this.hopCount = hopCount;
    }

    public void setNextHop(IPAddress nextHop){
        this.nextHop = nextHop;
    }
}
