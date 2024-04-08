package controller;

import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.Pair;
import model.*;
import view.ConnectionLine;
import view.SimulationWorkspaceView;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

public class SimulationController {
    private final ScheduledExecutorService threadPool;

    private final BlockingQueue<Pair<NetworkConnection, Frame>> outboundQueue;

    private final NetworkDeviceStorage storage;

    private final NetworksController networksController;

    private final SimulationWorkspaceView simulationWorkspaceView;

    private boolean simulationRunning = false;

    public SimulationController(SimulationWorkspaceView simulationWorkspaceView, NetworkDeviceStorage storage, NetworksController networksController) {
        this.outboundQueue = new LinkedBlockingQueue<>();
        this.threadPool = Executors.newScheduledThreadPool(50);
        this.storage = storage;
        this.networksController = networksController;
        this.simulationWorkspaceView = simulationWorkspaceView;
    }

    public Pair<NetworkConnection, Frame> receiveFrame() {
        try {
            return outboundQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void startSimulation() {
        if (simulationRunning) {
            return;
        }
        simulationRunning = true;

        threadPool.scheduleAtFixedRate(this::startRip, 0, 30, TimeUnit.SECONDS);
        startPacketProcessing();
        threadPool.scheduleAtFixedRate(this::pickRandomLanCommunication, 0, 5, TimeUnit.SECONDS);
    }

    public void stopSimulation() {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
    }

    private void startRip() {
        for (RouterModel router : storage.getRouterModels()) {
            for (RouterModel connectedRouter : networksController.getRoutersRipConnections(router)) {
                Network sharedNetwork = networksController.getSharedNetwork(router, connectedRouter);
                if (sharedNetwork != null) {
                    sendFrame(
                            new NetworkConnection(router.getNetworksRouterInterface(sharedNetwork), connectedRouter.getNetworksRouterInterface(sharedNetwork)),
                            new Frame(router.getMacAddress(), connectedRouter.getMacAddress(),
                                    new Packet(router.getNetworksRouterInterface(sharedNetwork).getIpAddress(), connectedRouter.getNetworksRouterInterface(sharedNetwork).getIpAddress(), new RipMessage(router.getRoutingTable()))));
                    /*connectedRouter.receiveRoutingTable(router.getRoutingTable(), router.getIpAddressInNetwork(sharedNetwork));*/
                }
            }
        }
    }

    public void pickRandomLanCommunication() {
        System.out.println("Picking PC communication");
        ArrayList<PCModel> pcModels = storage.getPcModels();
        if (pcModels.isEmpty()) {
            System.out.println("No pc models available");
            return;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(pcModels.size());
        PCModel initiatorPcModel = pcModels.get(randomIndex);
        while (initiatorPcModel.isConfigurationInProgress() || initiatorPcModel.getConnection() == null) {
            System.out.println("pc is in configuration process, picking another one");
            randomIndex = random.nextInt(pcModels.size());
            initiatorPcModel = pcModels.get(randomIndex);
        }

        PCModel recipientPcModel = initiatorPcModel;
        while (initiatorPcModel == recipientPcModel) {
            randomIndex = random.nextInt(pcModels.size());
            recipientPcModel = pcModels.get(randomIndex);
        }
        //TODO take a look whether the pc is in some router subnet => configure first, or if he has none (like only switch as the lan interface with no router anywhere) => allow no configuration to happen
        System.out.printf("%s wants to communicate with %s\n", initiatorPcModel, recipientPcModel);
        initiateCommunication(initiatorPcModel, recipientPcModel);
    }

    public void initiateCommunication(PCModel initiator, PCModel recipient) {
        if (initiator == null || recipient == null) {
            System.out.printf("initiator is %s, recipient %s is\n", initiator, recipient);
            return;
        }
        NetworkDeviceModel next = initiator.getConnection();
        if (!initiator.isConfigured()) {
            System.out.printf("Oh boi %s is not configured\n", initiator);
            initiator.setConfigurationInProgress();
            System.out.printf("Sending DHCP DISCOVERY from %s\n", initiator);
            sendDhcpDiscovery(new NetworkConnection(initiator, next), initiator.getMacAddress());
        }

        if (!recipient.isConfigured()) {
            System.out.printf("%s not configured", recipient);
            return;
        }

        if (networksController.isSameNetwork(initiator, recipient)) {
            System.out.printf("%s and %s ARE on the same network", initiator, recipient);
            MACAddress recipientMac = initiator.getArpCache().getMAC(recipient.getIpAddress());

            if (recipientMac != null) {
                System.out.printf("%s is sending packet request via %s", initiator, next);
                sendPacket(new NetworkConnection(initiator, next), initiator.getMacAddress(), recipientMac, new Packet(initiator.getIpAddress(), recipient.getIpAddress(), new StringMessage()));
            } else {
                System.out.printf("%s is sending arp request via %s", initiator, next);
                sendArpRequest(new NetworkConnection(initiator, next), initiator.getMacAddress(), initiator.getIpAddress(), recipient.getIpAddress());
            }
        } else {
            System.out.printf("%s and %s AREN'T on the same network", initiator, recipient);
            MACAddress defaultGatewayMac = initiator.getArpCache().getMAC(initiator.getDefaultGateway());
            if (defaultGatewayMac != null) {
                sendPacket(new NetworkConnection(initiator, next), initiator.getMacAddress(), defaultGatewayMac, new Packet(initiator.getIpAddress(), initiator.getDefaultGateway(), new StringMessage()));
            } else {
                sendArpRequest(new NetworkConnection(initiator, next), initiator.getMacAddress(), initiator.getIpAddress(), initiator.getDefaultGateway());
            }
        }
    }

    public void sendPacket(NetworkConnection networkConnection, MACAddress sourceMac, MACAddress destinationMac, Packet packet) {
        Frame ethernetFrame = new Frame(sourceMac, destinationMac, packet);
        outboundQueue.add(new Pair<>(networkConnection, ethernetFrame));
    }


    public void startPacketProcessing() {
        threadPool.submit(() -> {
            while (simulationRunning) {
                Pair<NetworkConnection, Frame> frameThroughNetworkConnection = receiveFrame();
                /*threadPool.submit(() -> sendFrame(frameThroughNetworkConnection.getKey(), frameThroughNetworkConnection.getValue()));*/
                /*sendFrame(frameThroughNetworkConnection.getKey(), frameThroughNetworkConnection.getValue());*/
                forwardToNextDevice(frameThroughNetworkConnection.getKey(), frameThroughNetworkConnection.getValue());

            }
        });
    }

    public void forwardToNextDevice(NetworkConnection networkConnection, Frame frame) {
        if (networkConnection.getEndDevice() instanceof PCModel pc) {
            handleFrameOnPc(pc, networkConnection, frame);

        } else if (networkConnection.getEndDevice() instanceof SwitchModel switchModel) {
            handleFrameOnSwitch(switchModel, networkConnection, frame);

        } else if (networkConnection.getEndDevice() instanceof RouterInterface routerInterface) {
            handleFrameOnRouter(routerInterface, networkConnection, frame);
        }
    }

    public void handleFrameOnPc(PCModel pc, NetworkConnection networkConnection, Frame frame) {
        System.out.printf("Received packet on PC!\nInitiator: %s, Recipient: %s\n", networkConnection.getStartDevice(), networkConnection.getEndDevice());
        if (frame.getDestinationMac() == pc.getMacAddress()) {
            if (frame.getPacket().getMessage() instanceof StringMessage stringMessage) {
                System.out.printf("Hi i am %s and i received this message: %s\n", pc.getMacAddress(), stringMessage.getBody());
            } else if (frame.getPacket().getMessage() instanceof DhcpOfferMessage dhcpOfferMessage) {
                System.out.println("I received dhcp offer!");
                pc.configure(dhcpOfferMessage.getOfferedIpAddress(), dhcpOfferMessage.getDefaultGateway(), dhcpOfferMessage.getSubnetMask());
                pc.updateArp(dhcpOfferMessage.getDefaultGateway(), frame.getSourceMac());
                sendDhcpResponse(new NetworkConnection(pc, networkConnection.getStartDevice()),
                        pc.getMacAddress(),
                        pc.queryArp(pc.getDefaultGateway()),
                        pc.getIpAddress(),
                        pc.getDefaultGateway(),
                        new DhcpResponseMessage());
            } else if (frame.getPacket().getMessage() instanceof DhcpAckMessage) {
                System.out.printf("i %s received dhcp ack!", pc);
                System.out.printf("configuration state: %s\n", pc.isConfigured());
                System.out.printf("configuration in progress: %s\n", pc.isConfigurationInProgress());
                return;
            }
        }
    }

    public void handleFrameOnSwitch(SwitchModel switchModel, NetworkConnection networkConnection, Frame frame) {
        System.out.printf("Received packet on SWITCH!\nInitiator: %s, Recipient: %s\n", networkConnection.getStartDevice(), networkConnection.getEndDevice());
        NetworkDeviceModel connectedDevice = networkConnection.getStartDevice();
        if (!switchModel.knowsMacAddress(frame.getDestinationMac())) {
            for (SwitchConnection switchConnection : switchModel.getSwitchConnections()) {
                if ((switchConnection.getNetworkDeviceModel() == connectedDevice)) {
                    //Do not forward frame to the source device
                    switchModel.learnMacAddress(frame.getSourceMac(), switchConnection.getPort());
                    continue;
                }
                outboundQueue.add(new Pair<>(new NetworkConnection(switchModel, switchConnection.getNetworkDeviceModel()), frame));
                /*forwardToNextDevice(new NetworkConnection(switchModel,switchConnection.getNetworkDeviceModel()), frame);*/
                /*sendFrame(new NetworkConnection(switchModel, switchConnection.getNetworkDeviceModel()), frame);*/
            }
        } else {
            int outgoingPort = switchModel.getPort(frame.getDestinationMac());
            for (SwitchConnection switchConnection : switchModel.getSwitchConnections()) {
                if (switchConnection.getNetworkDeviceModel() == connectedDevice) {
                    switchModel.learnMacAddress(frame.getSourceMac(), switchConnection.getPort());
                }
            }
            for (SwitchConnection switchConnection : switchModel.getSwitchConnections()) {
                if (switchConnection.getPort() == outgoingPort) {
                    outboundQueue.add(new Pair<>(new NetworkConnection(switchModel, switchConnection.getNetworkDeviceModel()), frame));
                    /*forwardToNextDevice(new NetworkConnection(switchModel, switchConnection.getNetworkDeviceModel()), frame);*/
                    /*sendFrame(new NetworkConnection(switchModel, switchConnection.getNetworkDeviceModel()), frame);*/
                }
            }
        }
    }

    public void handleFrameOnRouter(RouterInterface routerInterface, NetworkConnection networkConnection, Frame frame) {
        System.out.printf("Received packet on ROUTER!\nInitiator: %s, Recipient: %s\n", networkConnection.getStartDevice(), networkConnection.getEndDevice());

        if (frame.getPacket().getMessage() instanceof RipMessage ripMessage) {
            System.out.printf("i %s received rip message from %s", routerInterface, networkConnection.getStartDevice());
            routerInterface.getInterfacesRouter().receiveRoutingTable(ripMessage.getRoutingTable(), frame.getPacket().getSourceIp());
        } else if (frame.getPacket().getMessage() instanceof DhcpDiscoverMessage dhcpDiscoverMessage) {
            IPAddress offeredIpAddress = networksController.reserveIpAddressInNetwork(routerInterface.getNetwork());
            IPAddress defaultGateway = routerInterface.getIpAddress();
            SubnetMask subnetMask = routerInterface.getNetwork().getSubnetMask();
            sendDhcpOffer(new NetworkConnection(routerInterface, networkConnection.getStartDevice()),
                    routerInterface.getMacAddress(),
                    dhcpDiscoverMessage.getSourceMac(),
                    routerInterface.getIpAddress(),
                    new DhcpOfferMessage(offeredIpAddress, defaultGateway, subnetMask));
        } else if (frame.getPacket().getMessage() instanceof DhcpResponseMessage) {
            sendDhcpAck(new NetworkConnection(routerInterface, networkConnection.getStartDevice()),
                    routerInterface.getMacAddress(),
                    frame.getSourceMac(),
                    routerInterface.getIpAddress(),
                    frame.getPacket().getSourceIp(),
                    new DhcpAckMessage()
            );
        }
    }

    public void sendDhcpDiscovery(NetworkConnection networkConnection, MACAddress sourceMac) {
        sendPacket(networkConnection, sourceMac, MACAddress.ipv4Broadcast(), new Packet(null, null, new DhcpDiscoverMessage(sourceMac)));
    }

    public void sendArpRequest(NetworkConnection networkConnection, MACAddress senderMac, IPAddress senderIp, IPAddress targetIp) {
        sendPacket(networkConnection, senderMac, MACAddress.ipv4Broadcast(), new Packet(senderIp, targetIp, new ArpRequestMessage()));
    }

    public void sendDhcpOffer(NetworkConnection networkConnection, MACAddress sourceMac, MACAddress dstMac, IPAddress sourceIpAddress, DhcpOfferMessage dhcpOfferMessage) {
        sendPacket(networkConnection, sourceMac, dstMac, new Packet(sourceIpAddress, IPAddress.nullIpAddress(), dhcpOfferMessage));
    }

    public void sendDhcpResponse(NetworkConnection networkConnection, MACAddress sourceMac, MACAddress dstMac, IPAddress sourceIpAddress, IPAddress dstIpAddress, DhcpResponseMessage dhcpResponseMessage) {
        sendPacket(networkConnection, sourceMac, dstMac, new Packet(sourceIpAddress, dstIpAddress, dhcpResponseMessage));
    }

    public void sendDhcpAck(NetworkConnection networkConnection, MACAddress sourceMac, MACAddress dstMac, IPAddress sourceIpAddress, IPAddress dstIpAddress, DhcpAckMessage dhcpAckMessage) {
        sendPacket(networkConnection, sourceMac, dstMac, new Packet(sourceIpAddress, dstIpAddress, dhcpAckMessage));
    }

    private void sendFrame(NetworkConnection networkConnection, Frame frame) {
        Platform.runLater(() -> {
            NetworkDeviceModel animationStartNetworkDevice = networkConnection.getStartDevice();
            NetworkDeviceModel animationEndNetworkDevice = networkConnection.getEndDevice();

            if (networkConnection.getStartDevice() instanceof RouterInterface routerInterface) {
                animationStartNetworkDevice = routerInterface.getInterfacesRouter();
            }
            if (networkConnection.getEndDevice() instanceof RouterInterface routerInterface) {
                animationEndNetworkDevice = routerInterface.getInterfacesRouter();
            }

            ConnectionLine connectionLine = simulationWorkspaceView.getConnectionLine(animationStartNetworkDevice, animationEndNetworkDevice);

            Rectangle visualFrame = createVisualFrame(frame);

            simulationWorkspaceView.addNode(visualFrame);

            Path path = new Path();
            if (connectionLine.getStartDevice().getUuid() == animationStartNetworkDevice.getUuid()) {
                path.getElements().add(new MoveTo(connectionLine.getStartX(), connectionLine.getStartY()));
                path.getElements().add(new LineTo(connectionLine.getEndX(), connectionLine.getEndY()));
            } else if (connectionLine.getStartDevice().getUuid() == animationEndNetworkDevice.getUuid()) {
                path.getElements().add(new MoveTo(connectionLine.getEndX(), connectionLine.getEndY()));
                path.getElements().add(new LineTo(connectionLine.getStartX(), connectionLine.getStartY()));
            }

            PathTransition pathTransition = new PathTransition();
            pathTransition.setDuration(Duration.seconds(1.5));
            pathTransition.setPath(path);
            pathTransition.setNode(visualFrame);
            pathTransition.setOrientation(PathTransition.OrientationType.NONE);
            pathTransition.setCycleCount(1);
            pathTransition.setAutoReverse(false);

            pathTransition.setOnFinished(event -> {
                simulationWorkspaceView.removeNode(visualFrame);
                forwardToNextDevice(networkConnection, frame);
            });

            pathTransition.play();
        });
    }

    public Rectangle createVisualFrame(Frame frame) {
        Rectangle rectangle = new Rectangle(10, 10);

        if (frame.getPacket().getMessage() instanceof DhcpDiscoverMessage) {
            rectangle.setFill(Color.DARKRED);
        } else if (frame.getPacket().getMessage() instanceof DhcpOfferMessage) {
            rectangle.setFill(Color.RED);
        } else if (frame.getPacket().getMessage() instanceof DhcpResponseMessage) {
            rectangle.setFill(Color.ORANGE);
        } else if (frame.getPacket().getMessage() instanceof DhcpAckMessage) {
            rectangle.setFill(Color.YELLOW);
        } else if (frame.getPacket().getMessage() instanceof StringMessage) {
            rectangle.setFill(Color.BLUE);
        }
        return rectangle;
    }
}


