package model;

import java.util.ArrayList;
import java.util.List;

public class NetworkDeviceStorage {
    List<NetworkDeviceModel> networkDeviceModels = new ArrayList<>();

    public void add(NetworkDeviceModel networkDeviceModel) {
        networkDeviceModels.add(networkDeviceModel);
    }
}
