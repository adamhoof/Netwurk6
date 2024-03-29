package controller;

import common.NetworkDevice;
import common.NetworkDeviceProperties;
import common.NetworkDeviceType;
import model.*;
import view.SimulationWorkspaceView;

public class MasterController {
    SimulationWorkspaceView simulationWorkspaceView;
    NetworkDeviceStorage deviceStorage;

    NetworksController networksController;

    SimulationController simulationController;

    public MasterController(SimulationWorkspaceView simulationWorkspaceView, NetworkDeviceStorage deviceStorage, NetworksController networksController) {
        this.simulationWorkspaceView = simulationWorkspaceView;
        this.simulationWorkspaceView.setController(this);
        this.deviceStorage = deviceStorage;
        this.networksController = networksController;
        simulationController = new SimulationController(deviceStorage, networksController);
    }

    public void addDevice(NetworkDevice networkDevice) {
        NetworkDeviceModel networkDeviceModel;
        switch (networkDevice.getNetworkDeviceType()) {
            case ROUTER:
                networkDeviceModel = new RouterModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()));
                LanNetwork network = networksController.createDefaultLanNetwork();
                IPAddress routerIpAddress = networksController.reserveIpAddress(network);
                ((RouterModel) networkDeviceModel).addIpAddressInNetwork(routerIpAddress, network);
                ((RouterModel) networkDeviceModel).appendRoutingTable(new RouteEntry(network, routerIpAddress, 0));
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

        if (firstNetworkDeviceModel.getNetworkDeviceType() == NetworkDeviceType.ROUTER && secondNetworkDeviceModel.getNetworkDeviceType() == NetworkDeviceType.ROUTER) {
            networksController.createWanLink((RouterModel) firstNetworkDeviceModel, (RouterModel) secondNetworkDeviceModel);
        }

        networksController.addNetworkConnection(firstNetworkDeviceModel, secondNetworkDeviceModel);
        return true;
    }
    public void startSimulation() {
        simulationController.startSimulation();
    }

}
