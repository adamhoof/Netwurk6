package model;

public class RouterInterface {
    IPAddress ipAddress;
    MACAddress macAddress;

    public RouterInterface(IPAddress ipAddress, MACAddress macAddress) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
    }
}
