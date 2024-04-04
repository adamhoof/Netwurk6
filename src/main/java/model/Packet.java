package model;

public class Packet {
    private final IPAddress sourceIp;
    private final IPAddress destinationIp;
    private final Message message;


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