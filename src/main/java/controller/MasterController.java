package controller;

import common.NetworkDevice;
import model.*;
import view.SimulationWorkspaceView;

public class MasterController {
    SimulationWorkspaceView simulationWorkspaceView;
    NetworkDeviceStorage deviceStorage;

    public MasterController(SimulationWorkspaceView simulationWorkspaceView, NetworkDeviceStorage deviceStorage) {
        this.simulationWorkspaceView = simulationWorkspaceView;
        this.simulationWorkspaceView.setController(this);
        this.deviceStorage = deviceStorage;
    }

    public void addDevice(NetworkDevice networkDevice) {
        NetworkDeviceModel networkDeviceModel;
        switch (networkDevice.getNetworkDeviceType()) {
            case ROUTER:
                networkDeviceModel = new RouterModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()));
                break;
            case SWITCH:
                networkDeviceModel = new SwitchModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()));
                break;
            case PC:
                networkDeviceModel = new PCModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()));
                break;
            default:
                System.out.println("incorrect network device");
                return;
        }
        deviceStorage.add(networkDeviceModel);
    }

    public boolean addConnection(NetworkDevice first, NetworkDevice second) {
        NetworkDeviceModel firstNetworkDeviceModel = deviceStorage.get(first.getUuid());
        NetworkDeviceModel secondNetworkDeviceModel = deviceStorage.get(second.getUuid());

        if (firstNetworkDeviceModel == null || secondNetworkDeviceModel == null) {
            return false;
        }
        NetworkConnection networkConnection = new NetworkConnection(firstNetworkDeviceModel, secondNetworkDeviceModel);
        firstNetworkDeviceModel.addNetworkConnection(networkConnection);
        secondNetworkDeviceModel.addNetworkConnection(networkConnection);
        return true;
    }
}
