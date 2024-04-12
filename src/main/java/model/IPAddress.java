package model;

import java.util.Arrays;

/**
 * Represents an IP address in IPv4 format, storing each octet as an integer.
 * Provides utility functions for IP address manipulation and conversion between formats.
 */
public class IPAddress {
    private int[] octets = new int[4];

    /**
     * Constructs an IPAddress using four integers representing the octets.
     *
     * @param firstOctet the first octet of the IP address
     * @param secondOctet the second octet of the IP address
     * @param thirdOctet the third octet of the IP address
     * @param fourthOctet the fourth octet of the IP address
     */
    public IPAddress(int firstOctet, int secondOctet, int thirdOctet, int fourthOctet) {
        octets[0] = firstOctet;
        octets[1] = secondOctet;
        octets[2] = thirdOctet;
        octets[3] = fourthOctet;
    }

    /**
     * Constructs an IPAddress by copying another IPAddress.
     *
     * @param ipAddress the IPAddress to copy
     */
    public IPAddress(IPAddress ipAddress) {
        this(ipAddress.octets[0], ipAddress.octets[1], ipAddress.octets[2], ipAddress.octets[3]);
    }

    /**
     * Increments a specified octet by a given amount.
     *
     * @param octet the octet to increment (1 to 4)
     * @param increment the amount to add to the octet
     */
    public void incrementOctet(int octet, int increment) {
        if (octet >= 1 && octet <= 4) {
            octets[octet - 1] += increment;
        }
    }

    /**
     * Converts this IP address to a long integer representation.
     *
     * @return the long integer representation of this IP address
     */
    public long toLong() {
        long ipAsLong = 0;
        for (int i = 0; i < octets.length; i++) {
            ipAsLong = (ipAsLong << 8) + octets[i];
        }
        return ipAsLong;
    }

    /**
     * Converts a long integer to an IPAddress object.
     *
     * @param ipAsLong the long integer representation of the IP address
     * @return an IPAddress object corresponding to the given long integer
     */
    public static IPAddress longToIPAddress(long ipAsLong) {
        int octet1 = (int) ((ipAsLong >> 24) & 0xFF);
        int octet2 = (int) ((ipAsLong >> 16) & 0xFF);
        int octet3 = (int) ((ipAsLong >> 8) & 0xFF);
        int octet4 = (int) (ipAsLong & 0xFF);
        return new IPAddress(octet1, octet2, octet3, octet4);
    }

    public int[] getOctets() {
        return octets;
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d.%d", octets[0], octets[1], octets[2], octets[3]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPAddress ipAddress = (IPAddress) o;
        return Arrays.equals(octets, ipAddress.octets);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(octets);
    }

    /**
     * Provides a 'null' or zeroed-out IPAddress commonly used as a default.
     *
     * @return an IPAddress with all octets set to zero
     */
    public static IPAddress nullIpAddress() {
        return new IPAddress(0, 0, 0, 0);
    }
}
