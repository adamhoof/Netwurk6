package model;

/**
 * Represents a Frame in the network, encapsulating source and destination MAC addresses, along with a packet.
 * This class models the data link layer frame structure used in network communications.
 */
public class Frame {
    private final MACAddress sourceMac;
    private final MACAddress destinationMac;
    private final Packet packet;

    /**
     * Constructs a Frame with source MAC address, destination MAC address, and the encapsulated packet.
     *
     * @param sourceMac the source MAC address
     * @param destinationMac the destination MAC address
     * @param packet the packet encapsulated within this frame
     */
    public Frame(MACAddress sourceMac, MACAddress destinationMac, Packet packet) {
        this.sourceMac = sourceMac;
        this.destinationMac = destinationMac;
        this.packet = packet;
    }

    public MACAddress getSourceMac() {
        return sourceMac;
    }

    public MACAddress getDestinationMac() {
        return destinationMac;
    }

    public Packet getPacket() {
        return packet;
    }
}
