package model;

import java.util.*;

public class NetworkDeviceStorage {
    Map<UUID, NetworkDeviceModel> networkDeviceModels = new HashMap<>();

    public void add(NetworkDeviceModel networkDeviceModel) {
        networkDeviceModels.put(networkDeviceModel.getUuid(), networkDeviceModel);
    }

    public NetworkDeviceModel get(UUID uuid) {
        if (!networkDeviceModels.containsKey(uuid)) {
            return null;
        }
        return networkDeviceModels.get(uuid);
    }
}
