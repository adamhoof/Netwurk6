package model;

public class ArpEntry {
    MACAddress mac;
    IPAddress ipAddress;

    ArpEntry(IPAddress ipAddress, MACAddress macAddress) {
        this.ipAddress = ipAddress;
        this.mac = macAddress;
    }

    public MACAddress getMac() {
        return mac;
    }

    public IPAddress getIpAddress() {
        return ipAddress;
    }
}
