package model;

public class ArpEntry {
    MACAddress mac;
    IPAddress ipAddress;

    public MACAddress getMac() {
        return mac;
    }

    public IPAddress getIpAddress() {
        return ipAddress;
    }
}
