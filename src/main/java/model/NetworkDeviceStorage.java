package model;

import common.NetworkDeviceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkDeviceStorage {
    Map<UUID, NetworkDeviceModel> networkDeviceModels = new HashMap<>();
    ArrayList<RouterModel> routerModels = new ArrayList<>();

    ArrayList<PCModel> pcModels = new ArrayList<>();

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

    public ArrayList<PCModel> getPcModels() {
        return pcModels;
    }

}
