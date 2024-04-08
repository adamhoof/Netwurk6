package controller;

import common.AutoNameGenerator;
import common.NetworkDevice;
import common.NetworkDeviceType;
import model.*;
import view.SimulationWorkspaceView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MasterController {
    SimulationWorkspaceView simulationWorkspaceView;
    NetworkDeviceStorage deviceStorage;

    NetworksController networksController;

    SimulationController simulationController;

    public MasterController(SimulationWorkspaceView simulationWorkspaceView, NetworkDeviceStorage deviceStorage, NetworksController networksController, SimulationController simulationController) {
        this.simulationWorkspaceView = simulationWorkspaceView;
        this.simulationWorkspaceView.setController(this);
        this.deviceStorage = deviceStorage;
        this.networksController = networksController;
        this.simulationController = simulationController;
    }

    public void addDevice(NetworkDevice networkDevice) {
        NetworkDeviceModel networkDeviceModel;
        switch (networkDevice.getNetworkDeviceType()) {
            case ROUTER:
                RouterModel routerModel = new RouterModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()),networkDevice.getName());
                LanNetwork network = routerModel.createLanNetwork();
                IPAddress routerIpAddress = networksController.reserveIpAddressInNetwork(network);
                RouterInterface routerInterface = new RouterInterface(UUID.randomUUID(), routerIpAddress, new MACAddress(UUID.randomUUID().toString()), routerModel, network);
                routerModel.addRouterInterface(routerInterface, network);
                routerModel.appendRoutingTable(new RouteEntry(network, routerIpAddress, 0));
                routerInterface.setName(AutoNameGenerator.generateRouterInterfaceName());
                deviceStorage.add(routerInterface);
                deviceStorage.addRouter(routerModel);
                return;
            case SWITCH:
                networkDeviceModel = new SwitchModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()),networkDevice.getName());
                deviceStorage.add(networkDeviceModel);
                break;
            case PC:
                PCModel pcModel = new PCModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()),networkDevice.getName());
                deviceStorage.addPc(pcModel);
                break;
            default:
                System.out.println("incorrect network device");
        }
    }

    public boolean addConnection(NetworkDevice first, NetworkDevice second) {
        NetworkDeviceModel firstModel = deviceStorage.get(first.getUuid());
        NetworkDeviceModel secondModel = deviceStorage.get(second.getUuid());

        if (firstModel == null || secondModel == null) {
            System.out.printf("Unable to create connection: First device: %s, Second device: %s", firstModel, secondModel);
            return false;
        }

        if (firstModel.getNetworkDeviceType() == NetworkDeviceType.ROUTER && secondModel.getNetworkDeviceType() == NetworkDeviceType.ROUTER) {
            networksController.createWanLink((RouterModel) firstModel, (RouterModel) secondModel);
            return true;
        }
        //For now, RouterModel handles the connection for the second device, so we need to return after adding the second model
        if (firstModel instanceof RouterModel routerModel) {
            return routerModel.addConnection(secondModel);
        }
        if (secondModel instanceof RouterModel routerModel) {
            return routerModel.addConnection(firstModel);
        }
        return firstModel.addConnection(secondModel) && secondModel.addConnection(firstModel);
    }

    public Map<String, String> getLabelsForConnection(NetworkDevice first, NetworkDevice second) {
        Map<String, String> labels = new HashMap<>();
        labels.put("Middle", "");
        labels.put("Start", "");
        labels.put("End", "");
        if (first.getNetworkDeviceType() == NetworkDeviceType.ROUTER && second.getNetworkDeviceType() == NetworkDeviceType.ROUTER) {
            Network network = networksController.getSharedNetwork(deviceStorage.getRouterModel(first.getUuid()), deviceStorage.getRouterModel(second.getUuid()));
            if (network != null) {
                labels.put("Middle", network.getNetworkIpAddress().toString());
                labels.put("Start", "." + deviceStorage.getRouterModel(first.getUuid()).getIpAddressInNetwork(network).getOctets()[3]);
                labels.put("End", "." + deviceStorage.getRouterModel(second.getUuid()).getIpAddressInNetwork(network).getOctets()[3]);
            }
        }
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
