package controller;

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

    int pcNameCounter = 0;
    int switchNameCounter = 0;
    int routerNameCounter = 0;

    public MasterController(SimulationWorkspaceView simulationWorkspaceView, NetworkDeviceStorage deviceStorage, NetworksController networksController) {
        this.simulationWorkspaceView = simulationWorkspaceView;
        this.simulationWorkspaceView.setController(this);
        this.deviceStorage = deviceStorage;
        this.networksController = networksController;
        simulationController = new SimulationController(simulationWorkspaceView, deviceStorage, networksController);
    }

    public void addDevice(NetworkDevice networkDevice) {
        NetworkDeviceModel networkDeviceModel;
        switch (networkDevice.getNetworkDeviceType()) {
            case ROUTER:
                RouterModel routerModel = new RouterModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()));
                LanNetwork network = routerModel.createLanNetwork();
                IPAddress routerIpAddress = networksController.reserveIpAddress(network);
                RouterInterface routerInterface = new RouterInterface(UUID.randomUUID(), routerIpAddress, new MACAddress(UUID.randomUUID().toString()));
                routerModel.addRouterInterface(routerInterface, network);
                routerModel.appendRoutingTable(new RouteEntry(network, routerIpAddress, 0));
                routerModel.setName("Router" + routerNameCounter++);
                deviceStorage.add(routerInterface);
                deviceStorage.addRouter(routerModel);
                return;
            case SWITCH:
                networkDeviceModel = new SwitchModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()));
                networkDeviceModel.setName("Switch" + switchNameCounter++);
                deviceStorage.add(networkDeviceModel);
                break;
            case PC:
                PCModel pcModel = new PCModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()));
                pcModel.setName("PC" + pcNameCounter++);
                deviceStorage.addPc(pcModel);
                break;
            default:
                System.out.println("incorrect network device");
        }
    }

    public boolean addConnection(NetworkDevice first, NetworkDevice second) {
        if (first.getNetworkDeviceType() == NetworkDeviceType.ROUTER && second.getNetworkDeviceType() == NetworkDeviceType.ROUTER) {
            RouterModel firstRouter = deviceStorage.getRouterModel(first.getUuid());
            RouterModel secondRouter = deviceStorage.getRouterModel(second.getUuid());
            networksController.createWanLink(firstRouter, secondRouter);

        }
        NetworkDeviceModel firstModel = deviceStorage.get(first.getUuid());
        NetworkDeviceModel secondModel = deviceStorage.get(second.getUuid());

        if (firstModel == null || secondModel == null) {
            System.out.printf("Unable to create connection: First device: %s, Second device: %s", firstModel, secondModel);
            return false;
        }

        if (firstModel instanceof RouterModel routerModel) {
            if (secondModel instanceof PCModel pcModel) {
                RouterInterface routerInterface = routerModel.getDirectConnectionLanInterface();
                return pcModel.addConnection(routerInterface) && routerInterface.addConnection(secondModel);
            } else if (second instanceof SwitchModel switchModel) {
                LanNetwork lanNetwork = routerModel.createLanNetwork();
                RouterInterface routerInterface = routerModel.getNetworksRouterInterface(lanNetwork);
                return switchModel.addConnection(routerInterface) && routerInterface.addConnection(switchModel);
            }
        }
        if (secondModel instanceof RouterModel routerModel) {
            if (firstModel instanceof PCModel pcModel) {
                RouterInterface routerInterface = routerModel.getDirectConnectionLanInterface();
                return pcModel.addConnection(routerInterface) && routerInterface.addConnection(secondModel);
            } else if (firstModel instanceof SwitchModel switchModel) {
                LanNetwork lanNetwork = routerModel.createLanNetwork();
                RouterInterface routerInterface = routerModel.getNetworksRouterInterface(lanNetwork);
                return switchModel.addConnection(routerInterface) && routerInterface.addConnection(switchModel);
            }
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
