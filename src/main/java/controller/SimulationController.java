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
                    connectedRouter.receiveRoutingTable(router.getRoutingTable(), router.getIpAddressInNetwork(sharedNetwork));
                }
            }
        }
    }

    public void pickRandomLanCommunication() {
        ArrayList<PCModel> pcModels = storage.getPcModels();
        if (pcModels.isEmpty()) {
            System.out.println("No pc models available");
            return;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(pcModels.size());
        PCModel initiatorPcModel = pcModels.get(randomIndex);

        PCModel recipientPcModel = initiatorPcModel;
        while (initiatorPcModel == recipientPcModel) {
            randomIndex = random.nextInt(pcModels.size());
            recipientPcModel = pcModels.get(randomIndex);
        }
        System.out.printf("%s wants to communicate with %s\n", initiatorPcModel, recipientPcModel);
        initiateCommunication(initiatorPcModel, recipientPcModel);
    }

    public void initiateCommunication(PCModel initiator, PCModel recipient) {
        NetworkDeviceModel next = initiator.getConnection();
        if (!initiator.isConfigured()) {
            System.out.printf("Oh boi %s is not configured\n", initiator);
            sendDhcpDiscovery(new NetworkConnection(initiator, next), initiator.getMacAddress());
            return;
        }

        if (networksController.isSameNetwork(initiator, recipient)) {
            MACAddress recipientMac = initiator.getArpCache().getMAC(recipient.getIpAddress());

            if (recipientMac != null) {
                sendPacket(new NetworkConnection(initiator, next), initiator.getMacAddress(), recipientMac, new Packet(initiator.getIpAddress(), recipient.getIpAddress(), new StringMessage()));
            } else {
                sendArpRequest(new NetworkConnection(initiator, next), initiator.getMacAddress(), initiator.getIpAddress(), recipient.getIpAddress());
            }
        } else {
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

    public void sendDhcpDiscovery(NetworkConnection networkConnection, MACAddress sourceMac) {
        sendPacket(networkConnection, sourceMac, MACAddress.ipv4Broadcast(), new Packet(null, null, new DhcpDiscoverMessage()));
    }

    public void sendArpRequest(NetworkConnection networkConnection, MACAddress senderMac, IPAddress senderIp, IPAddress targetIp) {
        sendPacket(networkConnection, senderMac, MACAddress.ipv4Broadcast(), new Packet(senderIp, targetIp, new ArpRequestMessage()));
    }

    public void startPacketProcessing() {
        threadPool.submit(() -> {
            while (simulationRunning) {
                try {
                    Pair<NetworkConnection, Frame> frameThroughNetworkConnection = outboundQueue.take();
                    sendFrame(frameThroughNetworkConnection.getKey(), frameThroughNetworkConnection.getValue());
                    //TODO make this thread to work
                    /*threadPool.submit(() -> forwardToNextDevice(frameThroughNetworkConnection.getKey(), frameThroughNetworkConnection.getValue()));*/
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
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
        if (frame.getDestinationMac().equals(MACAddress.ipv4Broadcast())) {
            broadcastFrame(switchModel, connectedDevice, frame, false);
            return;
        }

        if (!switchModel.knowsMacAddress(frame.getDestinationMac())) {
            broadcastFrame(switchModel, connectedDevice, frame, true);

        } else {
            int outgoingPort = switchModel.getPort(frame.getDestinationMac());
            for (SwitchConnection switchConnection : switchModel.getSwitchConnections()) {
                if (switchConnection.getNetworkDeviceModel() == connectedDevice) {
                    //Retrieve ingoing port
                    switchModel.learnMacAddress(connectedDevice.getMacAddress(), switchConnection.getPort());
                }
            }
            for (SwitchConnection switchConnection : switchModel.getSwitchConnections()) {
                if (switchConnection.getPort() == outgoingPort) {
                    sendFrame(new NetworkConnection(switchModel, switchConnection.getNetworkDeviceModel()), frame);
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

            Rectangle square = new Rectangle(10, 10);
            square.setFill(Color.RED);
            simulationWorkspaceView.addNode(square);

            Path path = new Path();
            if (connectionLine.getStartDevice().getUuid() == animationStartNetworkDevice.getUuid()) {
                path.getElements().add(new MoveTo(connectionLine.getStartX(), connectionLine.getStartY()));
                path.getElements().add(new LineTo(connectionLine.getEndX(), connectionLine.getEndY()));
            } else if (connectionLine.getStartDevice().getUuid() == animationEndNetworkDevice.getUuid()) {
                path.getElements().add(new MoveTo(connectionLine.getEndX(), connectionLine.getEndY()));
                path.getElements().add(new LineTo(connectionLine.getStartX(), connectionLine.getStartY()));
            }

            PathTransition pathTransition = new PathTransition();
            pathTransition.setDuration(Duration.seconds(0.8));
            pathTransition.setPath(path);
            pathTransition.setNode(square);
            pathTransition.setOrientation(PathTransition.OrientationType.NONE);
            pathTransition.setCycleCount(1);
            pathTransition.setAutoReverse(false);

            pathTransition.setOnFinished(event -> {
                simulationWorkspaceView.removeNode(square);
                forwardToNextDevice(networkConnection, frame);
            });

            pathTransition.play();
        });
    }
}


