package model;

import java.util.ArrayList;

public class ArpCache {
    ArrayList<ArpEntry> entries = new ArrayList<>();

    public MACAddress getMAC(IPAddress ipAddress) {
        for (ArpEntry arpEntry : entries) {
            if (arpEntry.getIpAddress() != ipAddress) {
                continue;
            }
            return arpEntry.getMac();
        }
        return null;
    }
}
