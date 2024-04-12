package model;

/**
 * Represents a subnet mask used in IP networking to divide the network and host portion of an address.
 */
public class SubnetMask {
    private final String bitmask;
    private final int size;

    /**
     * Constructs a SubnetMask with a given size.
     *
     * @param size The size of the subnet mask in bits.
     */
    public SubnetMask(int size) {
        this.size = size;
        this.bitmask = calculateBitmaskFromPrefixSize(size);
    }

    /**
     * Calculates the subnet mask from the prefix size.
     *
     * @param size The prefix size of the subnet mask.
     * @return The calculated bitmask as a string.
     */
    private String calculateBitmaskFromPrefixSize(int size) {
        long bitmask = 0;
        for (int i = 0; i < size; i++) {
            bitmask |= (1L << (31 - i));
        }
        return ((bitmask >> 24) & 0xFF) + "." +
                ((bitmask >> 16) & 0xFF) + "." +
                ((bitmask >> 8) & 0xFF) + "." +
                (bitmask & 0xFF);
    }

    /**
     * Returns the size of the subnet mask in bits.
     *
     * @return The size of the subnet mask.
     */
    public int getSize() {
        return size;
    }

    /**
     * Converts the subnet mask to a long representation suitable for bitwise operations.
     *
     * @return The long representation of the subnet mask.
     */
    public long toLong() {
        String[] octets = bitmask.split("\\.");
        long result = 0;
        for (String octet : octets) {
            result = (result << 8) + Integer.parseInt(octet);
        }
        return result;
    }
}
