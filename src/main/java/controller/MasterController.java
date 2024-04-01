package controller;

import common.NetworkDevice;
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
                RouterModel routerModel = new RouterModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()));
                LanNetwork network = networksController.createDefaultLanNetwork();
                IPAddress routerIpAddress = networksController.reserveIpAddress(network);
                routerModel.addIpAddressInNetwork(routerIpAddress, network);
                routerModel.appendRoutingTable(new RouteEntry(network, routerIpAddress, 0));
                deviceStorage.add(routerModel);
                return;
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
    }

    public boolean addConnection(NetworkDevice first, NetworkDevice second) {
        if (first.getNetworkDeviceType() == NetworkDeviceType.ROUTER && second.getNetworkDeviceType() == NetworkDeviceType.ROUTER) {
            RouterModel firstRouter = deviceStorage.getRouterModel(first.getUuid());
            RouterModel secondRouter = deviceStorage.getRouterModel(second.getUuid());
            networksController.createWanLink(firstRouter, secondRouter);
        }
        return true;
    }

    public String getDeviceConfigurations(NetworkDevice networkDevice) {
        StringBuilder configuration = new StringBuilder();
        if (networkDevice.getNetworkDeviceType() == NetworkDeviceType.ROUTER) {
            RouterModel routerModel = deviceStorage.getRouterModel(networkDevice.getUuid());
            for (RouterModel router : networksController.getRoutersRipConnections(routerModel)) {
                configuration.append(routerModel.ipAddressInNetwork(networksController.getSharedNetwork(router, routerModel))).append("\n");
            }
        }
        return configuration.toString();
    }

    public void startSimulation() {
        simulationController.startSimulation();
    }
}
