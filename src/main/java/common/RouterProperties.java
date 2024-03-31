package common;

public class RouterProperties extends NetworkDeviceProperties {
    public String IPaddress;

    public RouterProperties(String IPaddress, String MACAddress) {
        this.MACaddress = MACAddress;
        this.IPaddress = IPaddress;
    }

    public String getIPaddress() {
        return IPaddress;
    }
}
