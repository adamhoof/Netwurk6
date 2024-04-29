package controller;

import com.google.common.eventbus.Subscribe;
import common.*;
import javafx.scene.paint.Color;
import model.*;
import view.ConnectionLine;
import view.SimulationWorkspaceView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
        GlobalEventBus.register(this);
    }

    /**
     * Handles an update event for a PC model.
     * This method is invoked when an {@link UpdateLabelsEvent} is posted on the event bus. It retrieves the {@link PCModel} object from the event and performs the following actions:
     * 1. Extracts the associated {@link NetworkDevice} from the PC model.
     * 2. If the network device is a {@link RouterInterface}, it retrieves the actual router it's connected to visually.
     * 3. Finds the corresponding {@link ConnectionLine} object in the simulation workspace view based on the PC model and network device.
     * 4. Updates the appropriate label (start or end) on the connection line based on the PC model's UUID and retrieved IP address (using the last octet).
     * 5. If no connection line is found, a message is logged indicating the issue.
     *
     * @param event the {@link UpdateLabelsEvent} containing the PC model data
     */
    @Subscribe
    public void handleUpdateLabels(UpdateLabelsEvent event) {
        PCModel pcModel = event.pcModel();

        NetworkDevice networkDevice = pcModel.getConnection();
        if (networkDevice instanceof RouterInterface routerInterface) {
            networkDevice = routerInterface.getInterfacesRouter();
        }
        ConnectionLine connectionLine = simulationWorkspaceView.getConnectionLine(pcModel, networkDevice);
        if (connectionLine == null) {
            System.out.println("no connection found for label update");
            return;
        }

        if (connectionLine.getStartDevice().getUuid() == pcModel.getUuid()) {
            connectionLine.getStartLabel().setText("." + pcModel.getIpAddress().getOctets()[3]);
        } else {
            connectionLine.getEndLabel().setText("." + pcModel.getIpAddress().getOctets()[3]);
        }
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
                routerInterface.setName(AutoNameGenerator.getInstance().generateRouterInterfaceName());
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
            simulationWorkspaceView.printToLogWindow("Can't connect to itself\n", Color.RED);
            return false;
        }
        NetworkDeviceModel firstModel = deviceStorage.get(first.getUuid());
        NetworkDeviceModel secondModel = deviceStorage.get(second.getUuid());

        if (firstModel instanceof PCModel && secondModel instanceof PCModel) {
            simulationWorkspaceView.printToLogWindow("Can't connect PC to PC\n", Color.RED);
            return false;
        }

        if ((firstModel instanceof PCModel pc1 && pc1.getConnection() != null) || secondModel instanceof PCModel pc2 && pc2.getConnection() != null) {
            simulationWorkspaceView.printToLogWindow("Can't connect PC to multiple networks\n", Color.RED);
            return false;
        }

        if (firstModel == null || secondModel == null) {
            System.out.printf("Unable to create connection: First device: %s, Second device: %s", firstModel, secondModel);
            return false;
        }

        if (firstModel instanceof RouterModel firstRouter && secondModel instanceof RouterModel secondRouter) {
            networksController.createWanLink(firstRouter, secondRouter);
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
    public Map<String, String> setupInitialLabelsForConnection(NetworkDevice first, NetworkDevice second) {
        Map<String, String> labels = new HashMap<>();
        labels.put("Middle", "");
        labels.put("Start", "");
        labels.put("End", "");
        if (first.getNetworkDeviceType() == NetworkDeviceType.ROUTER && second.getNetworkDeviceType() == NetworkDeviceType.ROUTER) {
            RouterModel firstRouterModel = deviceStorage.getRouterModel(first.getUuid());
            RouterModel secondRouterModel = deviceStorage.getRouterModel(second.getUuid());
            Network network = networksController.getSharedNetwork(firstRouterModel, secondRouterModel);
            if (network != null) {
                labels.put("Middle", network.getNetworkIpAddress().toString());
                labels.put("Start", "." + deviceStorage.getRouterModel(first.getUuid()).getIpAddressInNetwork(network).getOctets()[3]);
                labels.put("End", "." + deviceStorage.getRouterModel(second.getUuid()).getIpAddressInNetwork(network).getOctets()[3]);
            }
        } else if (first.getNetworkDeviceType() == NetworkDeviceType.PC && second.getNetworkDeviceType() == NetworkDeviceType.ROUTER) {
            RouterModel routerModel = deviceStorage.getRouterModel(second.getUuid());
            RouterInterface routerInterface = routerModel.getDirectConnectionLanInterface();

            labels.put("Start", "no_ip");
            labels.put("Middle", routerInterface.getNetwork().getNetworkIpAddress().toString());
            labels.put("End", "." + routerInterface.getIpAddress().getOctets()[3]);
        } else if (first.getNetworkDeviceType() == NetworkDeviceType.ROUTER && second.getNetworkDeviceType() == NetworkDeviceType.PC) {
            RouterModel routerModel = deviceStorage.getRouterModel(first.getUuid());
            RouterInterface routerInterface = routerModel.getDirectConnectionLanInterface();

            labels.put("Start", "." + routerInterface.getIpAddress().getOctets()[3]);
            labels.put("Middle", routerInterface.getNetwork().getNetworkIpAddress().toString());
            labels.put("End", "no_ip");
        } else if (first.getNetworkDeviceType() == NetworkDeviceType.SWITCH && second.getNetworkDeviceType() == NetworkDeviceType.ROUTER) {
            RouterModel routerModel = deviceStorage.getRouterModel(second.getUuid());
            RouterInterface routerInterface = routerModel.getLastRouterInterface();

            labels.put("Middle", routerInterface.getNetwork().getNetworkIpAddress().toString());
            labels.put("End", "." + routerInterface.getIpAddress().getOctets()[3]);
        } else if (first.getNetworkDeviceType() == NetworkDeviceType.ROUTER && second.getNetworkDeviceType() == NetworkDeviceType.SWITCH) {
            RouterModel routerModel = deviceStorage.getRouterModel(first.getUuid());
            RouterInterface routerInterface = routerModel.getLastRouterInterface();

            labels.put("Start", "." + routerInterface.getIpAddress().getOctets()[3]);
            labels.put("Middle", routerInterface.getNetwork().getNetworkIpAddress().toString());
        } else if (first.getNetworkDeviceType() == NetworkDeviceType.PC && second.getNetworkDeviceType() == NetworkDeviceType.SWITCH) {
            labels.put("Start", "no_ip");
        } else if (first.getNetworkDeviceType() == NetworkDeviceType.SWITCH && second.getNetworkDeviceType() == NetworkDeviceType.PC) {
            labels.put("End", "no_ip");
        }
        return labels;
    }

    /**
     * Determines the IP address of a PC.
     * This method retrieves the PC model associated with the provided network device using its UUID and then extracts the IP address from the PC model.
     *
     * @param pc the {@link NetworkDevice} object for which to determine the IP address
     * @return a String representing the IP address. If no IP address is found, it returns "no_ip".
     * @throws NullPointerException if the provided `second` argument is null
     */
    private String determineLabelIpPcSide(NetworkDevice pc) {
        PCModel pcModel = deviceStorage.getPcModel(pc.getUuid());

        IPAddress ipAddress = pcModel.getIpAddress();
        String stringPcIp = "";
        if (Objects.equals(ipAddress, IPAddress.nullIpAddress())) {
            stringPcIp += "no_ip";
        } else {
            stringPcIp = "." + ipAddress.getOctets()[3];
        }
        return stringPcIp;
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
