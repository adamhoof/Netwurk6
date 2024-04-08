package model;

public class DhcpDiscoverMessage implements Message {
    MACAddress sourceMac;

    public DhcpDiscoverMessage(MACAddress sourceMac){
        this.sourceMac = sourceMac;
    }

    public MACAddress getSourceMac() {
        return sourceMac;
    }
}
