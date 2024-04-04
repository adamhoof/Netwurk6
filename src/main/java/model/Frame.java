package model;

public class Frame {
    private final MACAddress sourceMac;
    private final MACAddress destinationMac;
    private final Packet packet;

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