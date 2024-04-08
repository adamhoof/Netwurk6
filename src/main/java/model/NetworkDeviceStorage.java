package model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkDeviceStorage {
    Map<UUID, NetworkDeviceModel> networkDeviceModels = new HashMap<>();
    ArrayList<RouterModel> routerModels = new ArrayList<>();

    List<PCModel> pcModels = new CopyOnWriteArrayList<>();



    public void add(NetworkDeviceModel networkDeviceModel) {
        networkDeviceModels.put(networkDeviceModel.getUuid(), networkDeviceModel);
    }

    public void addRouter(RouterModel routerModel) {
        networkDeviceModels.put(routerModel.getUuid(), routerModel);
        routerModels.add(routerModel);
    }

    public void addPc(PCModel pcModel) {
        networkDeviceModels.put(pcModel.getUuid(), pcModel);
        pcModels.add(pcModel);
    }

    public NetworkDeviceModel get(UUID uuid) {
        if (!networkDeviceModels.containsKey(uuid)) {
            return null;
        }
        return networkDeviceModels.get(uuid);
    }

    public ArrayList<RouterModel> getRouterModels() {
        return routerModels;
    }

    public RouterModel getRouterModel(UUID uuid) {
        if (!networkDeviceModels.containsKey(uuid)) {
            return null;
        }
        return (RouterModel) networkDeviceModels.get(uuid);
    }

    public PCModel getPcModel(UUID uuid) {
        if (!networkDeviceModels.containsKey(uuid)) {
            return null;
        }
        return (PCModel) networkDeviceModels.get(uuid);
    }

    public NetworkDeviceModel getNetworkDeviceByMac(MACAddress macAddress) {
        UUID uuidFromMac = UUID.fromString(macAddress.toString());
        if (!networkDeviceModels.containsKey(uuidFromMac)) {
            return null;
        }
        return networkDeviceModels.get(uuidFromMac);
    }
    public ArrayList<PCModel> getPcModels() {
        return new ArrayList<>(pcModels);
    }
}
