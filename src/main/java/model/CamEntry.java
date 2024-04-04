package model;

public class CamEntry {
    MACAddress macAddress;
    int port;

    public CamEntry(MACAddress macAddress, int port) {
        this.macAddress = macAddress;
        this.port = port;
    }
}
