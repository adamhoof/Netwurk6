package model;

import common.NetworkDeviceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkDeviceStorage {
    Map<UUID, NetworkDeviceModel> networkDeviceModels = new HashMap<>();
    ArrayList<RouterModel> routerModels = new ArrayList<>();

    public void add(NetworkDeviceModel networkDeviceModel) {
        networkDeviceModels.put(networkDeviceModel.getUuid(), networkDeviceModel);
        if (networkDeviceModel.getNetworkDeviceType() == NetworkDeviceType.ROUTER){
            routerModels.add((RouterModel) networkDeviceModel);
        }
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

}
