package model;

public class ArpRequestMessage implements Message {
    IPAddress requestedIpAddress;
    IPAddress requesterIpAddress;
    MACAddress requesterMacAddress;

    public ArpRequestMessage(IPAddress requestedIpAddress, IPAddress requesterIpAddress, MACAddress requesterMacAddress) {
        this.requestedIpAddress = requestedIpAddress;
        this.requesterMacAddress = requesterMacAddress;
        this.requesterIpAddress = requesterIpAddress;
    }

    public IPAddress getRequestedIpAddress() {
        return requestedIpAddress;
    }

    public MACAddress getRequesterMacAddress() {
        return requesterMacAddress;
    }

    public IPAddress getRequesterIpAddress() {
        return requesterIpAddress;
    }
}
