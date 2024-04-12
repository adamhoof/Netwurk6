package model;

/**
 * Represents a packet in the network simulation.
 * Contains source and destination IP addresses, and a message payload.
 */
public class Packet {
    private final IPAddress sourceIp;
    private final IPAddress destinationIp;
    private final Message message;

    /**
     * Constructs a Packet with source and destination IP addresses and a message.
     *
     * @param sourceIp        the source IP address of the packet.
     * @param destinationIp   the destination IP address of the packet.
     * @param message         the message payload of the packet.
     */
    public Packet(IPAddress sourceIp, IPAddress destinationIp, Message message) {
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.message = message;
    }

    public IPAddress getSourceIp() {
        return sourceIp;
    }

    public IPAddress getDestinationIp() {
        return destinationIp;
    }

    public Message getMessage() {
        return message;
    }
}
