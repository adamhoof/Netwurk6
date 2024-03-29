package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkDeviceStorage {
    Map<UUID, NetworkDeviceModel> networkDeviceModels = new HashMap<>();
    ArrayList<RouterModel> routerModels = new ArrayList<>();
    Map<RouterModel, ArrayList<RouterModel>> routersConnections;

    public void add(NetworkDeviceModel networkDeviceModel) {
        networkDeviceModels.put(networkDeviceModel.getUuid(), networkDeviceModel);
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

    public ArrayList<RouterModel> getRoutersConnections(RouterModel routerModel) {
        return routersConnections.get(routerModel);
    }
}
