package model;

public class ArpResponseMessage implements Message {
    MACAddress requestedMacAddress;

    public ArpResponseMessage(MACAddress requestedMacAddress) {
        this.requestedMacAddress = requestedMacAddress;
    }

    public MACAddress getRequestedMacAddress() {
        return requestedMacAddress;
    }
}
