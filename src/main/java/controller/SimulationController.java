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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import view.ConnectionLine;
import view.SimulationWorkspaceView;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimulationController {
    private final ScheduledExecutorService threadPool;

    private final BlockingQueue<Pair<NetworkConnection, Frame>> outboundQueue;

    private final NetworkDeviceStorage storage;

    private final NetworksController networksController;

    private final SimulationWorkspaceView simulationWorkspaceView;

    private final AtomicBoolean simulationStarted = new AtomicBoolean(false);

    private final AtomicBoolean isPaused = new AtomicBoolean(true);

    private final Semaphore pauseSemaphore = new Semaphore(1);

    private ScheduledFuture<?> randomCommunicationTaskHandle;
    private ScheduledFuture<?> ripTaskHandle;

    private static final Logger logger = LogManager.getLogger(SimulationController.class);

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
        if (simulationStarted.get()) {
            return;
        }
        isPaused.set(false);
        simulationStarted.set(true);

        /*ripTaskHandle =threadPool.scheduleAtFixedRate(this::startRip, 0, 30, TimeUnit.SECONDS);*/
        startPacketProcessing();
        randomCommunicationTaskHandle = threadPool.scheduleAtFixedRate(this::pickRandomLanCommunication, 0, 5, TimeUnit.SECONDS);
    }

    public void pauseSimulation() {
        if (isPaused.get()) {
            return;
        }
        isPaused.set(true);
        pauseSemaphore.acquireUninterruptibly();

        if (ripTaskHandle != null) {
            ripTaskHandle.cancel(true);
        }
        if (randomCommunicationTaskHandle != null) {
            randomCommunicationTaskHandle.cancel(true);
        }
    }

    public void resumeSimulation() {
        isPaused.set(false);
        pauseSemaphore.release();
        ripTaskHandle = threadPool.scheduleAtFixedRate(this::startRip, 10, 30, TimeUnit.SECONDS);
        randomCommunicationTaskHandle = threadPool.scheduleAtFixedRate(this::pickRandomLanCommunication, 5, 10, TimeUnit.SECONDS);
    }

    private void startRip() {
        for (RouterModel router : storage.getRouterModels()) {
            for (RouterModel connectedRouter : networksController.getRoutersRipConnections(router)) {
                Network sharedNetwork = networksController.getSharedNetwork(router, connectedRouter);
                if (sharedNetwork != null) {
                    sendFrameWithAnimation(
                            new NetworkConnection(router.getNetworksRouterInterface(sharedNetwork), connectedRouter.getNetworksRouterInterface(sharedNetwork)),
                            new Frame(router.getMacAddress(), connectedRouter.getMacAddress(),
                                    new Packet(router.getNetworksRouterInterface(sharedNetwork).getIpAddress(), connectedRouter.getNetworksRouterInterface(sharedNetwork).getIpAddress(), new RipMessage(router.getRoutingTable()))));
                    /*connectedRouter.receiveRoutingTable(router.getRoutingTable(), router.getIpAddressInNetwork(sharedNetwork));*/
                }
            }
        }
    }

    public void pickRandomLanCommunication() {
        logger.debug("Picking PC communication");
        ArrayList<PCModel> pcModels = storage.getPcModels();
        if (pcModels.isEmpty()) {
            logger.warn("No pc models available");
            return;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(pcModels.size());
        PCModel initiatorPcModel = pcModels.get(randomIndex);
        while (initiatorPcModel.isConfigurationInProgress() || initiatorPcModel.getConnection() == null) {
            logger.debug("Initiator {} is in configuration process, picking another one", initiatorPcModel);
            randomIndex = random.nextInt(pcModels.size());
            initiatorPcModel = pcModels.get(randomIndex);
        }

        PCModel recipientPcModel = initiatorPcModel;
        while (initiatorPcModel == recipientPcModel) {
            randomIndex = random.nextInt(pcModels.size());
            recipientPcModel = pcModels.get(randomIndex);
        }
        //TODO take a look whether the pc is in some router subnet => configure first, or if he has none (like only switch as the lan interface with no router anywhere) => allow no configuration to happen
        logger.debug("Initiator {} wants to communicate with recipient {}", initiatorPcModel, recipientPcModel);
        PCModel finalInitiatorPcModel = initiatorPcModel;
        PCModel finalRecipientPcModel = recipientPcModel;
        threadPool.submit(() -> {
            initiateCommunication(finalInitiatorPcModel, finalRecipientPcModel);
        });
    }

    public void initiateCommunication(PCModel initiator, PCModel recipient) {
        if (initiator == null || recipient == null) {
            logger.fatal("initiator is {}, recipient {} is", initiator, recipient);
            return;
        }

        logger.debug("Initiating communication, initiator: {}, recipient {}", initiator, recipient);

        NetworkDeviceModel next = initiator.getConnection();
        if (!initiator.isConfigured()) {
            logger.info("initiator {} is not configured => sending DHCP discovery", initiator);
            initiator.setConfigurationInProgress();
            sendDhcpDiscovery(new NetworkConnection(initiator, next), initiator.getMacAddress());
            return;
        }

        if (!recipient.isConfigured()) {
            logger.debug("Recipient {} is not configured", recipient);
            return;
        }

        if (networksController.isSameNetwork(initiator, recipient)) {
            logger.warn("Initiator {}, ip {} and recipient {}, ip {} ARE on the same network", initiator, initiator.getIpAddress(), recipient, recipient.getIpAddress());
            MACAddress recipientMac = initiator.queryArp(recipient.getIpAddress());

            if (recipientMac != null) {
                logger.info("Initiator {}, ip {} KNOWS recipient mac, sending direct string message, network communication: {} -> {}", initiator, initiator.getIpAddress(), initiator, next);
                sendPacket(new NetworkConnection(initiator, next), initiator.getMacAddress(), recipientMac, new Packet(initiator.getIpAddress(), recipient.getIpAddress(), new StringMessage("googa")));
            } else {
                logger.info("Initiator DOESN'T KNOW recipient mac, sending ARP request, network communication: {} -> {}", initiator, next);
                sendArpRequest(new NetworkConnection(initiator, next), initiator.getMacAddress(), initiator.getIpAddress(), recipient.getIpAddress());
            }
        } else {
            logger.warn("Initiator {}, ip {} and recipient {}, ip {} AREN'T on the same network", initiator, initiator.getIpAddress(), recipient, recipient.getIpAddress());
            MACAddress defaultGatewayMac = initiator.queryArp(initiator.getDefaultGateway());
            if (defaultGatewayMac != null) {
                logger.info("Initiator {}, ip {} KNOWS default gateway mac (ip {}), sending string message, network communication: {} -> {}", initiator, initiator.getIpAddress(), initiator.getDefaultGateway(), initiator, next);
                sendPacket(new NetworkConnection(initiator, next),
                        initiator.getMacAddress(),
                        defaultGatewayMac,
                        new Packet(initiator.getIpAddress(), recipient.getIpAddress(), new StringMessage("fuck")));
            } else {
                logger.info("Initiator {}, ip {} DOESN'T KNOW default gateway mac, sending arp request, network communication: {} -> {}", initiator, initiator.getIpAddress(), initiator, next);
                sendPacket(new NetworkConnection(initiator, next),
                        initiator.getMacAddress(),
                        MACAddress.ipv4Broadcast(),
                        new Packet(initiator.getIpAddress(), initiator.getDefaultGateway(), new ArpRequestMessage(initiator.getDefaultGateway(), initiator.getIpAddress(), initiator.getMacAddress())));
            }
        }
    }

    public void sendPacket(NetworkConnection networkConnection, MACAddress sourceMac, MACAddress destinationMac, Packet packet) {
        Frame ethernetFrame = new Frame(sourceMac, destinationMac, packet);
        outboundQueue.add(new Pair<>(networkConnection, ethernetFrame));
    }


    public void startPacketProcessing() {
        threadPool.submit(() -> {
            while (simulationStarted.get()) {
                try {
                    pauseSemaphore.acquire(); // Block here if simulation is paused
                    pauseSemaphore.release();

                    Pair<NetworkConnection, Frame> frameThroughNetworkConnection = receiveFrame();
                    sendFrameWithAnimation(frameThroughNetworkConnection.getKey(), frameThroughNetworkConnection.getValue());

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                    logger.error("Thread was interrupted.", e);
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
        if (frame.getDestinationMac() == pc.getMacAddress() || frame.getPacket().getDestinationIp() == pc.getIpAddress()) {
            if (frame.getPacket().getMessage() instanceof StringMessage stringMessage) {
                logger.debug("Recipient {}, ip {} received STRING MESSAGE, body -> {}", pc, pc.getIpAddress(), stringMessage.getBody());
            } else if (frame.getPacket().getMessage() instanceof DhcpOfferMessage dhcpOfferMessage) {
                logger.debug("Recipient {}, ip {} received DHCP OFFER MESSAGE, body -> DG {}, Offered ip {}, Subnetmask {}", pc, pc.getIpAddress(), dhcpOfferMessage.getDefaultGateway(), dhcpOfferMessage.getOfferedIpAddress(), dhcpOfferMessage.getSubnetMask());
                pc.configure(dhcpOfferMessage.getOfferedIpAddress(), dhcpOfferMessage.getDefaultGateway(), dhcpOfferMessage.getSubnetMask());
                pc.updateArp(dhcpOfferMessage.getDefaultGateway(), frame.getSourceMac());
                sendDhcpResponse(new NetworkConnection(pc, networkConnection.getStartDevice()),
                        pc.getMacAddress(),
                        pc.queryArp(pc.getDefaultGateway()),
                        pc.getIpAddress(),
                        pc.getDefaultGateway(),
                        new DhcpResponseMessage());
            } else if (frame.getPacket().getMessage() instanceof DhcpAckMessage) {
                logger.debug("Recipient {}, ip {}, received DHCP ACK MESSAGE, conf state: {}, conf in progress {}", pc, pc.getIpAddress(), pc.isConfigured(), pc.isConfigurationInProgress());
            } else if (frame.getPacket().getMessage() instanceof ArpRequestMessage arpRequestMessage && arpRequestMessage.getRequestedIpAddress() == pc.getIpAddress()) {
                logger.debug("Recipient {}, ip {} received ARP REQUEST MESSAGE", pc, pc.getIpAddress());
                sendPacket(new NetworkConnection(pc, pc.getConnection()),
                        pc.getMacAddress(),
                        arpRequestMessage.getRequesterMacAddress(),
                        new Packet(pc.getIpAddress(), arpRequestMessage.getRequesterIpAddress(), new ArpResponseMessage(pc.getMacAddress())));
                sendArpResponse(new NetworkConnection(pc, pc.getConnection()),
                        pc.getMacAddress(),
                        frame.getSourceMac(),
                        pc.getIpAddress(),
                        frame.getPacket().getSourceIp(),
                        new ArpResponseMessage(pc.getMacAddress())
                );
            } else if (frame.getPacket().getMessage() instanceof ArpResponseMessage arpResponseMessage) {
                logger.debug("Recipient {}, ip {} received ARP RESPONSE MESSAGE from {}, body -> requested mac for device {}",
                        pc, pc.getIpAddress(), storage.getNetworkDeviceByMac(frame.getSourceMac()), storage.getNetworkDeviceByMac(arpResponseMessage.getRequestedMacAddress()));
                pc.updateArp(frame.getPacket().getSourceIp(), arpResponseMessage.getRequestedMacAddress());
            }
        }
    }

    public void handleFrameOnSwitch(SwitchModel switchModel, NetworkConnection networkConnection, Frame frame) {
        NetworkDeviceModel connectedDevice = networkConnection.getStartDevice();
        if (!switchModel.knowsMacAddress(frame.getDestinationMac())) {
            logger.debug("{} DOESN'T KNOW the dst mac or it is broadcast", switchModel);
            for (SwitchConnection switchConnection : switchModel.getSwitchConnections()) {
                if ((switchConnection.getNetworkDeviceModel() == connectedDevice)) {
                    //Do not forward frame to the source device
                    if (!switchModel.knowsMacAddress(frame.getSourceMac())) {
                        switchModel.learnMacAddress(frame.getSourceMac(), switchConnection.getPort());
                        logger.debug("{} learned mac address of source device {}, mapped to port {}", switchModel, storage.getNetworkDeviceByMac(frame.getSourceMac()), switchConnection.getPort());
                    }
                    continue;
                }
                outboundQueue.add(new Pair<>(new NetworkConnection(switchModel, switchConnection.getNetworkDeviceModel()), frame));
            }
        } else {
            logger.debug("{} KNOWS the dst mac of device {}", switchModel, storage.getNetworkDeviceByMac(frame.getDestinationMac()));
            int outgoingPort = switchModel.getPort(frame.getDestinationMac());
            for (SwitchConnection switchConnection : switchModel.getSwitchConnections()) {
                if (switchConnection.getNetworkDeviceModel() == connectedDevice) {
                    if (!switchModel.knowsMacAddress(frame.getSourceMac())) {
                        switchModel.learnMacAddress(frame.getSourceMac(), switchConnection.getPort());
                        logger.debug("{} learned mac address of source device {}, mapped to port {}", switchModel, storage.getNetworkDeviceByMac(frame.getSourceMac()), switchConnection.getPort());
                    }
                }
            }
            for (SwitchConnection switchConnection : switchModel.getSwitchConnections()) {
                if (switchConnection.getPort() == outgoingPort) {
                    logger.debug("{} forwarding the frame to port {}, network connection: {} -> {}", switchModel, outgoingPort, switchModel, switchConnection.getNetworkDeviceModel());
                    outboundQueue.add(new Pair<>(new NetworkConnection(switchModel, switchConnection.getNetworkDeviceModel()), frame));
                }
            }
        }
    }

    public void handleFrameOnRouter(RouterInterface routerInterface, NetworkConnection networkConnection, Frame frame) {
        if (frame.getPacket().getMessage() instanceof RipMessage ripMessage) {
            logger.debug("Recipient {}, ip {} received RIP MESSAGE", routerInterface, routerInterface.getIpAddress());
            routerInterface.getInterfacesRouter().updateRoutingTable(ripMessage.getRoutingTable(), frame.getPacket().getSourceIp());
        } else if (frame.getPacket().getMessage() instanceof DhcpDiscoverMessage dhcpDiscoverMessage) {
            logger.debug("Recipient {}, ip {} received DHCP DISCOVERY MESSAGE from source device {}",
                    routerInterface, routerInterface.getIpAddress(), storage.getNetworkDeviceByMac(dhcpDiscoverMessage.getSourceMac()));
            IPAddress offeredIpAddress = networksController.reserveIpAddressInNetwork(routerInterface.getNetwork());
            IPAddress defaultGateway = routerInterface.getIpAddress();
            SubnetMask subnetMask = routerInterface.getNetwork().getSubnetMask();
            sendDhcpOffer(new NetworkConnection(routerInterface, networkConnection.getStartDevice()),
                    routerInterface.getMacAddress(),
                    dhcpDiscoverMessage.getSourceMac(),
                    routerInterface.getIpAddress(),
                    new DhcpOfferMessage(offeredIpAddress, defaultGateway, subnetMask));
        } else if (frame.getPacket().getMessage() instanceof DhcpResponseMessage) {
            logger.debug("Recipient {}, ip {} received DHCP RESPONSE MESSAGE from source device {}", routerInterface, routerInterface.getIpAddress(), storage.getNetworkDeviceByMac(frame.getSourceMac()));
            sendDhcpAck(new NetworkConnection(routerInterface, networkConnection.getStartDevice()),
                    routerInterface.getMacAddress(),
                    frame.getSourceMac(),
                    routerInterface.getIpAddress(),
                    frame.getPacket().getSourceIp(),
                    new DhcpAckMessage()
            );
        } else if (frame.getPacket().getMessage() instanceof ArpResponseMessage arpResponseMessage) {
            RouterModel router = routerInterface.getInterfacesRouter();
            IPAddress sourceIp = frame.getPacket().getSourceIp();
            router.updateArp(sourceIp, arpResponseMessage.getRequestedMacAddress());

        } else if (frame.getPacket().getMessage() instanceof StringMessage stringMessage) {
            logger.debug("Recipient {}, ip {}, received STRING MESSAGE", routerInterface, routerInterface.getIpAddress());
            IPAddress forwardToIp = frame.getPacket().getDestinationIp();
            RouterModel router = routerInterface.getInterfacesRouter();

            ConcurrentHashMap<Network, RouterInterface> networkRouterInterfaceMap = router.getRouterInterfaces();

            logger.info("{}, ip {} is looking for appropriate subnet", routerInterface, routerInterface.getIpAddress());
            for (ConcurrentHashMap.Entry<Network, RouterInterface> entry : networkRouterInterfaceMap.entrySet()) {
                Network network = entry.getKey();
                RouterInterface ri = entry.getValue();

                if ((network.getNetworkIpAddress().toLong() & network.getSubnetMask().toLong()) == (forwardToIp.toLong() & routerInterface.getNetwork().getSubnetMask().toLong())) {
                    logger.debug("Found the correct router interface {}, ip {}, on network ip {}, DST IP {}", ri, ri.getIpAddress(), network.getNetworkIpAddress(), forwardToIp);
                    MACAddress dstMacAddress = router.queryArp(forwardToIp);
                    if (dstMacAddress == null) {
                        logger.info("Router interface {}, ip {} DOES'T KNOW mac of dst device, sending ARP REQUEST to {}", ri, ri.getIpAddress(), forwardToIp);
                        threadPool.submit(() -> {
                            sendPacket(new NetworkConnection(ri, ri.getFirstConnectedDevice()),
                                    ri.getMacAddress(),
                                    MACAddress.ipv4Broadcast(),
                                    new Packet(ri.getIpAddress(), forwardToIp, new ArpRequestMessage(forwardToIp, ri.getIpAddress(), ri.getMacAddress())));
                            while (router.queryArp(forwardToIp) == null) {
                                logger.info("trying to wait for 1000");
                                try {
                                    Thread.sleep(20); // 1000 milliseconds = 1 second
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt(); // Preserve interrupt status
                                }

                            }
                            logger.info("finally sending packet");
                            sendPacket(new NetworkConnection(ri, ri.getFirstConnectedDevice()),
                                    ri.getMacAddress(),
                                    router.queryArp(forwardToIp),
                                    new Packet(ri.getIpAddress(), forwardToIp, stringMessage));
                        });
                        //knows MAC
                    } else {
                        logger.info("{}, ip {} KNOWS the mac of dst device, forwarding message", ri, ri.getIpAddress());
                        sendPacket(new NetworkConnection(ri, ri.getFirstConnectedDevice()), ri.getMacAddress(), dstMacAddress, new Packet(ri.getIpAddress(), forwardToIp, stringMessage));
                    }
                }
            }

        }
    }

    public void sendDhcpDiscovery(NetworkConnection networkConnection, MACAddress sourceMac) {
        sendPacket(networkConnection, sourceMac, MACAddress.ipv4Broadcast(), new Packet(null, null, new DhcpDiscoverMessage(sourceMac)));
    }

    public void sendArpRequest(NetworkConnection networkConnection, MACAddress senderMac, IPAddress senderIp, IPAddress targetIp) {
        sendPacket(networkConnection, senderMac, MACAddress.ipv4Broadcast(), new Packet(senderIp, targetIp, new ArpRequestMessage(targetIp, senderIp, senderMac)));
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

    public void sendArpResponse(NetworkConnection networkConnection, MACAddress sourceMac, MACAddress dstMac, IPAddress sourceIpAddress, IPAddress dstIpAddress, ArpResponseMessage arpResponseMessage) {
        sendPacket(networkConnection, sourceMac, dstMac, new Packet(sourceIpAddress, dstIpAddress, arpResponseMessage));
    }

    private void sendFrameWithAnimation(NetworkConnection networkConnection, Frame frame) {
        Platform.runLater(() -> {
            try {
                NetworkDeviceModel animationStartNetworkDevice = networkConnection.getStartDevice();
                NetworkDeviceModel animationEndNetworkDevice = networkConnection.getEndDevice();

                if (animationStartNetworkDevice instanceof RouterInterface routerInterfaceStart) {
                    animationStartNetworkDevice = routerInterfaceStart.getInterfacesRouter();
                }
                if (animationEndNetworkDevice instanceof RouterInterface routerInterfaceEnd) {
                    animationEndNetworkDevice = routerInterfaceEnd.getInterfacesRouter();
                }

                Rectangle visualFrame = createVisualFrame(frame);
                simulationWorkspaceView.addNode(visualFrame);
                PathTransition pathTransition = preparePathTransition(visualFrame, animationStartNetworkDevice, animationEndNetworkDevice);
                if (pathTransition == null) {
                    logger.warn("path transition is empty boi");
                    return;
                }
                pathTransition.setOnFinished(event -> {
                    simulationWorkspaceView.removeNode(visualFrame);
                    forwardToNextDevice(networkConnection, frame);
                });

                pathTransition.play();
            } catch (Exception e) {
                logger.error(e);
            }
        });
    }

    private PathTransition preparePathTransition(Rectangle visualFrame, NetworkDeviceModel startDevice, NetworkDeviceModel endDevice) {
        ConnectionLine connectionLine = simulationWorkspaceView.getConnectionLine(startDevice, endDevice);
        if (connectionLine == null) {
            logger.warn("connection line is empty boi");
            return null;
        }

        Path path = new Path();
        // Set up the path based on the start and end points of the connection line.
        if (connectionLine.getStartDevice().getUuid().equals(startDevice.getUuid())) {
            path.getElements().add(new MoveTo(connectionLine.getStartX(), connectionLine.getStartY()));
            path.getElements().add(new LineTo(connectionLine.getEndX(), connectionLine.getEndY()));
        } else {
            path.getElements().add(new MoveTo(connectionLine.getEndX(), connectionLine.getEndY()));
            path.getElements().add(new LineTo(connectionLine.getStartX(), connectionLine.getStartY()));
        }

        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(0.5));
        pathTransition.setPath(path);
        pathTransition.setNode(visualFrame);
        pathTransition.setCycleCount(1);
        pathTransition.setAutoReverse(false);

        return pathTransition;
    }


    public Rectangle createVisualFrame(Frame frame) {
        Rectangle rectangle = new Rectangle(10, 10);

        Message message = frame.getPacket().getMessage();

        if (message instanceof DhcpDiscoverMessage) {
            rectangle.setFill(Color.DARKRED);
        } else if (message instanceof DhcpOfferMessage) {
            rectangle.setFill(Color.RED);
        } else if (message instanceof DhcpResponseMessage) {
            rectangle.setFill(Color.ORANGE);
        } else if (message instanceof DhcpAckMessage) {
            rectangle.setFill(Color.YELLOW);
        } else if (message instanceof ArpRequestMessage) {
            rectangle.setFill(Color.BLUE);
        } else if (message instanceof ArpResponseMessage) {
            rectangle.setFill(Color.LIGHTBLUE);
        } else if (message instanceof StringMessage) {
            rectangle.setFill(Color.GREEN);
        }
        return rectangle;
    }

    public boolean simulationStarted() {
        return simulationStarted.get();
    }

    public boolean isPaused() {
        return isPaused.get();
    }
}


