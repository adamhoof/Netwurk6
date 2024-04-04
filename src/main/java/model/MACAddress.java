package model;

public class MACAddress {
    String address;

    public MACAddress(String address) {
        this.address = address;
    }

    public static MACAddress ipv4Broadcast() {
        return new MACAddress("255.255.255.255");
    }

    @Override
    public String toString() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MACAddress that = (MACAddress) o;

        return address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
}
