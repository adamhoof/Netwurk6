package model;

public class SubnetMask {
    private final String bitmask;
    private final int size;

    public SubnetMask(int size) {
        this.size = size;
        this.bitmask = calculateBitmaskFromPrefixSize(size);
    }

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

    public int getSize() {
        return size;
    }

    public long toLong() {
        String[] octets = bitmask.split("\\.");
        long result = 0;
        for (String octet : octets) {
            result = (result << 8) + Integer.parseInt(octet);
        }
        return result;
    }
}
