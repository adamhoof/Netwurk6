package controller;

import common.NetworkDevice;
import common.NetworkDeviceType;
import model.*;
import view.SimulationWorkspaceView;
import java.util.HashMap;
import java.util.Map;

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

    public Map<String, String> getLabelsForConnection(NetworkDevice first, NetworkDevice second) {
        Network network = networksController.getSharedNetwork(deviceStorage.getRouterModel(first.getUuid()), deviceStorage.getRouterModel(second.getUuid()));
        if (network == null) {
            return null;
        }
        Map<String, String> labels = new HashMap<>();
        labels.put("Middle", network.getNetworkIpAddress().toString());
        labels.put("Start", "." + deviceStorage.getRouterModel(first.getUuid()).getIpAddressInNetwork(network).getOctets()[3]);
        labels.put("End", "." + deviceStorage.getRouterModel(second.getUuid()).getIpAddressInNetwork(network).getOctets()[3]);
        return labels;
    }

    public String getDeviceConfigurations(NetworkDevice networkDevice) {
        StringBuilder configuration = new StringBuilder();
        if (networkDevice.getNetworkDeviceType() == NetworkDeviceType.ROUTER) {
            configuration.append(String.format("%-15s | %-15s | %-8s | %-18s\n", "Dst network", "Next hop", "Hop cnt", "IP in Dst network"));
            RouterModel routerModel = deviceStorage.getRouterModel(networkDevice.getUuid());
            for (RouteEntry routeEntry : routerModel.getRoutingTable().getEntries()) {
                configuration.append(String.format("%-15s | %-15s | %-8s | %-18s\n",
                        routeEntry.getDestinationNetwork().getNetworkIpAddress(),
                        routeEntry.getNextHop(),
                        routeEntry.getHopCount(),
                        routerModel.getIpAddressInNetwork(routeEntry.getDestinationNetwork())
                ));
            }
        }
        return configuration.toString();
    }

    public void startSimulation() {
        simulationController.startSimulation();
    }
}
