package controller;

import common.AutoNameGenerator;
import common.NetworkDevice;
import common.NetworkDeviceType;
import model.*;
import view.SimulationWorkspaceView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main controller class that orchestrates interactions between the simulation's UI and the underlying network models.
 */
public class MasterController {
    SimulationWorkspaceView simulationWorkspaceView;
    NetworkDeviceStorage deviceStorage;

    NetworksController networksController;

    SimulationController simulationController;

    /**
     * Constructs a MasterController that integrates various components of the network simulation.
     *
     * @param simulationWorkspaceView The main UI view for the simulation.
     * @param deviceStorage           Storage for network devices.
     * @param networksController      Controller for network-related operations.
     * @param simulationController    Controller for simulation operations.
     */
    public MasterController(SimulationWorkspaceView simulationWorkspaceView, NetworkDeviceStorage deviceStorage, NetworksController networksController, SimulationController simulationController) {
        this.simulationWorkspaceView = simulationWorkspaceView;
        this.simulationWorkspaceView.setController(this);
        this.deviceStorage = deviceStorage;
        this.networksController = networksController;
        this.simulationController = simulationController;
    }

    /**
     * Adds a network device to the simulation.
     *
     * @param networkDevice The network device to add.
     */
    public void addDevice(NetworkDevice networkDevice) {
        NetworkDeviceModel networkDeviceModel;
        switch (networkDevice.getNetworkDeviceType()) {
            case ROUTER:
                RouterModel routerModel = new RouterModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()), networkDevice.getName());
                LanNetwork network = routerModel.createLanNetwork();
                IPAddress routerIpAddress = networksController.reserveIpAddressInNetwork(network);
                RouterInterface routerInterface = new RouterInterface(UUID.randomUUID(), routerIpAddress, new MACAddress(UUID.randomUUID().toString()), routerModel, network);
                routerInterface.setName(AutoNameGenerator.generateRouterInterfaceName());
                routerModel.addRouterInterface(routerInterface, network);
                routerModel.appendRoutingTable(new RouteEntry(network, routerIpAddress, 0));
                deviceStorage.add(routerInterface);
                deviceStorage.addRouter(routerModel);
                return;
            case SWITCH:
                networkDeviceModel = new SwitchModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()), networkDevice.getName());
                deviceStorage.add(networkDeviceModel);
                break;
            case PC:
                PCModel pcModel = new PCModel(networkDevice.getUuid(), new MACAddress(networkDevice.getUuid().toString()), networkDevice.getName());
                deviceStorage.addPc(pcModel);
                break;
            default:
                System.out.println("incorrect network device");
        }
    }

    /**
     * Attempts to establish a connection between two network devices.
     *
     * @param first  The first network device.
     * @param second The second network device.
     * @return true if the connection is successful, false otherwise.
     */
    public boolean addConnection(NetworkDevice first, NetworkDevice second) {
        if (first.getUuid() == second.getUuid()) {
            return false;
        }
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

    /**
     * Retrieves label information for a connection between two network devices.
     *
     * @param first  The first network device.
     * @param second The second network device.
     * @return A map containing label descriptions.
     */
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

    /**
     * Retrieves the configuration details of a specific network device.
     *
     * @param networkDevice The network device for which configuration details are requested.
     * @return Configuration details as a formatted string.
     */
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

    /**
     * Starts the network simulation.
     */
    public void startSimulation() {
        simulationController.startSimulation();
    }

    /**
     * Checks if the simulation has started.
     *
     * @return true if the simulation has started, false otherwise.
     */
    public boolean simulationStarted() {
        return simulationController.simulationStarted();
    }

    /**
     * Resumes the paused network simulation.
     */
    public void resumeSimulation() {
        simulationController.resumeSimulation();
    }

    /**
     * Pauses the ongoing network simulation.
     */
    public void pauseSimulation() {
        simulationController.pauseSimulation();
    }

    /**
     * Checks if the simulation is paused.
     *
     * @return true if the simulation is paused, false otherwise.
     */
    public boolean simulationPaused() {
        return simulationController.isPaused();
    }
}
