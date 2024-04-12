package model;

/**
 * Represents a MAC address in a network environment.
 * This class provides basic functionality to handle MAC addresses including creating a broadcast address.
 */
public class MACAddress {
    String address;

    /**
     * Constructs a MACAddress with the specified address.
     *
     * @param address the string representation of the MAC address
     */
    public MACAddress(String address) {
        this.address = address;
    }

    /**
     * Provides the IPv4 broadcast address.
     *
     * @return a MACAddress object representing the IPv4 broadcast address
     */
    public static MACAddress ipv4Broadcast() {
        return new MACAddress("255.255.255.255");
    }

    /**
     * Returns the string representation of the MAC address.
     *
     * @return the MAC address as a string
     */
    @Override
    public String toString() {
        return address;
    }

    /**
     * Compares this MACAddress to the specified object. The result is true if and only if
     * the argument is not null and is a MACAddress object that represents the same address as this object.
     *
     * @param o the object to compare this MACAddress against
     * @return true if the given object represents a MACAddress equivalent to this address, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MACAddress that = (MACAddress) o;

        return address.equals(that.address);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return address.hashCode();
    }
}
