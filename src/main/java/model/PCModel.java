package model;

import common.NetworkDeviceType;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PCModel extends NetworkDeviceModel {
    private IPAddress ipAddress;

    private Network network;
    private IPAddress defaultGateway;
    private SubnetMask subnetMask;
    private final ArpCache arpCache;

    private final AtomicBoolean isConfigured = new AtomicBoolean(false);

    private final AtomicBoolean configurationInProgress = new AtomicBoolean(false);

    private NetworkDeviceModel connection;

    public PCModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.PC);
        this.arpCache = new ArpCache();
    }

    public PCModel(UUID uuid, MACAddress macAddress, String name) {
        super(uuid, macAddress, NetworkDeviceType.PC, name);
        this.arpCache = new ArpCache();
    }

    public Network getNetwork() {
        return network;
    }

    public IPAddress getIpAddress() {
        return ipAddress;
    }

    public SubnetMask getSubnetMask() {
        return subnetMask;
    }

    public ArpCache getArpCache() {
        return arpCache;
    }

    public IPAddress getDefaultGateway() {
        return defaultGateway;
    }

    public boolean isConfigured() {
        return isConfigured.get();
    }


    @Override
    public boolean addConnection(NetworkDeviceModel networkDeviceModel) {
        if (connection != null) {
            return false;
        }
        connection = networkDeviceModel;
        return true;
    }

    public NetworkDeviceModel getConnection() {
        return connection;
    }

   public boolean isConfigurationInProgress(){
        return configurationInProgress.get();
   }

   public void setConfigurationInProgress(){this.configurationInProgress.set(true);}
    public void configure(IPAddress ipAddress, IPAddress defaultGateway, SubnetMask subnetMask) {
        this.ipAddress = ipAddress;
        this.defaultGateway = defaultGateway;
        this.subnetMask = subnetMask;
        isConfigured.set(true);
        configurationInProgress.set(false);
    }

    public void updateArp(IPAddress ipAddress, MACAddress macAddress) {
        arpCache.addEntry(ipAddress, macAddress);
    }

    public MACAddress queryArp(IPAddress ipAddress) {
        return getArpCache().getMAC(ipAddress);
    }
}