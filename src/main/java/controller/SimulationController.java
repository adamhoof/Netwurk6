package controller;

import com.google.common.eventbus.Subscribe;
import common.ExitRequestEvent;
import common.GlobalEventBus;
import common.ReadyToExitEvent;
import common.UpdateLabelsEvent;
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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controls the simulation of network communications, handling animations, and packet transmissions.
 */
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

    /**
     * Initializes the simulation controller with required dependencies.
     *
     * @param simulationWorkspaceView The user interface for the simulation.
     * @param storage                 Storage for all network devices.
     * @param networksController      Controller that manages network settings and behaviors.
     */
    public SimulationController(SimulationWorkspaceView simulationWorkspaceView, NetworkDeviceStorage storage, NetworksController networksController) {
        this.outboundQueue = new LinkedBlockingQueue<>();
        this.threadPool = Executors.newScheduledThreadPool(50);
        this.storage = storage;
        this.networksController = networksController;
        this.simulationWorkspaceView = simulationWorkspaceView;
        GlobalEventBus.register(this);
    }

    public void updateLabelsRequest(PCModel pcModel) {
        GlobalEventBus.post(new UpdateLabelsEvent(pcModel));
    }

    @Subscribe
    public void handleExitRequestEvent(ExitRequestEvent event) {
        try {
            threadPool.shutdown();
            if (!threadPool.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                List<Runnable> droppedTasks = threadPool.shutdownNow();
                System.out.println("Shutdown forced, dropping tasks: " + droppedTasks.size());
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        GlobalEventBus.post(new ReadyToExitEvent());
    }

    /**
     * Retrieves a frame from the outbound queue, blocking until one is available.
     *
     * @return The network connection and frame to be processed.
     * @throws RuntimeException if the thread is interrupted.
     */
    public Pair<NetworkConnection, Frame> receiveFrame() {
        try {
            return outboundQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts the network simulation, scheduling tasks for random communications and RIP protocol operations.
     */
    public void startSimulation() {
        if (simulationStarted.get()) {
            return;
        }
        isPaused.set(false);
        simulationStarted.set(true);

        ripTaskHandle = threadPool.scheduleAtFixedRate(this::startRip, 0, 30, TimeUnit.SECONDS);
        startPacketProcessing();
        randomCommunicationTaskHandle = threadPool.scheduleAtFixedRate(this::pickRandomLanCommunication, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Pauses the network simulation, halting all ongoing tasks and animations.
     */
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

    /**
     * Resumes the network simulation, restarting the paused tasks.
     */
    public void resumeSimulation() {
        isPaused.set(false);
        pauseSemaphore.release();
        ripTaskHandle = threadPool.scheduleAtFixedRate(this::startRip, 10, 30, TimeUnit.SECONDS);
        randomCommunicationTaskHandle = threadPool.scheduleAtFixedRate(this::pickRandomLanCommunication, 5, 10, TimeUnit.SECONDS);
    }

    /**
     * Periodically initiates RIP protocol communications between routers to update routing tables.
     */
    private void startRip() {
        for (RouterModel router : storage.getRouterModels()) {
            for (RouterModel connectedRouter : networksController.getRoutersRipConnections(router)) {
                Network sharedNetwork = networksController.getSharedNetwork(router, connectedRouter);
                if (sharedNetwork != null) {
                    sendFrameWithAnimation(
                            new NetworkConnection(router.getNetworksRouterInterface(sharedNetwork), connectedRouter.getNetworksRouterInterface(sharedNetwork)),
                            new Frame(router.getMacAddress(), connectedRouter.getMacAddress(),
                                    new Packet(router.getNetworksRouterInterface(sharedNetwork).getIpAddress(), connectedRouter.getNetworksRouterInterface(sharedNetwork).getIpAddress(), new RipMessage(router.getRoutingTable()))));
                }
            }
        }
    }

    /**
     * Randomly selects two PCs and attempts to start a communication session between them.
     */
    public void pickRandomLanCommunication() {
        logger.debug("Picking PC communication");
        ArrayList<PCModel> pcModels = storage.getPcModels();
        if (pcModels.size() < 2) {
            simulationWorkspaceView.printToLogWindow("Place more than 1 PC to start PC<->PC communication\n", Color.RED);
            logger.warn("Place more than 1 PC to start PC<->PC communication");
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
        while (initiatorPcModel == recipientPcModel || recipientPcModel.getConnection() == null || recipientPcModel.isConfigurationInProgress()) {
            randomIndex = random.nextInt(pcModels.size());
            recipientPcModel = pcModels.get(randomIndex);
        }
        logger.debug("Initiator {} wants to communicate with recipient {}", initiatorPcModel, recipientPcModel);
        PCModel finalInitiatorPcModel = initiatorPcModel;
        PCModel finalRecipientPcModel = recipientPcModel;
        initiateCommunication(finalInitiatorPcModel, finalRecipientPcModel);
    }

    public static RouterInterface findInterfaceByExactIpAddress(List<RouterInterface> interfaces, IPAddress targetIp) {
        Optional<RouterInterface> result = interfaces.stream()
                .filter(routerInterface -> routerInterface.getIpAddress() == targetIp)  // Check for reference equality
                .findFirst();

        return result.orElse(null);
    }

    /**
     * Initiates a communication session between two PCs, handling configuration and message transmission.
     *
     * @param initiator The initiating PC.
     * @param recipient The recipient PC.
     */
    public void initiateCommunication(PCModel initiator, PCModel recipient) {
        if (initiator == null || recipient == null) {
            logger.fatal("initiator is {}, recipient {} is", initiator, recipient);
            return;
        }

        logger.debug("Initiating communication, initiator: {}, recipient {}", initiator, recipient);
        simulationWorkspaceView.printToLogWindow(String.format("%s wants to communicate with %s\n", initiator, recipient), Color.GRAY);

        NetworkDeviceModel next = initiator.getConnection();
        if (!initiator.isConfigured()) {
            simulationWorkspaceView.printToLogWindow(String.format("%s is not configured => sending DHCP-Discovery\n", initiator), Color.DARKRED);
            logger.info("initiator {} is not configured => sending DHCP discovery", initiator);
            initiator.setConfigurationInProgress();
            sendDhcpDiscovery(new NetworkConnection(initiator, next), initiator.getMacAddress());
            return;
        }

        if (!recipient.isConfigured()) {
            next = recipient.getConnection();
            simulationWorkspaceView.printToLogWindow(String.format("%s is not configured => sending DHCP-Discovery\n", recipient), Color.DARKRED);
            logger.info("recipient {} is not configured => sending DHCP discovery", recipient);
            recipient.setConfigurationInProgress();
            sendDhcpDiscovery(new NetworkConnection(recipient, next), recipient.getMacAddress());
            return;
        }

        RouterInterface initiatorRouterInterface = findInterfaceByExactIpAddress(storage.getRouterInterfaces(), initiator.getDefaultGateway());
        RouterInterface recipientRouterInterface = findInterfaceByExactIpAddress(storage.getRouterInterfaces(), recipient.getDefaultGateway());

        if (initiatorRouterInterface == null){
            System.out.println("initiator interface null");
            return;
        }
        if (recipientRouterInterface == null){
            System.out.println("recipient interface null");
            return;
        }

        RouterModel initiatorRouterInterfacesRouter = initiatorRouterInterface.getInterfacesRouter();
        RouterModel recipientRouterInterfacesRouter = recipientRouterInterface.getInterfacesRouter();

        if (networksController.isSameNetwork(initiator, recipient)) {
            if (initiatorRouterInterfacesRouter != recipientRouterInterfacesRouter){
                //ip address of networks is the same, but they are somewhere completely different (2 distant LANs can have same network IPs)
                simulationWorkspaceView.printToLogWindow(String.format("PC WAN communication not implemented (%s -> %s)\nPicking different one\n", initiator, recipient), Color.RED);
                pickRandomLanCommunication();
                return;
            }
            logger.warn("Initiator {}, ip {} and recipient {}, ip {} ARE on the same network", initiator, initiator.getIpAddress(), recipient, recipient.getIpAddress());
            MACAddress recipientMac = initiator.queryArp(recipient.getIpAddress());

            if (recipientMac != null) {
                logger.info("Initiator {}, ip {} KNOWS recipient mac, sending direct string message, network communication: {} -> {}", initiator, initiator.getIpAddress(), initiator, next);
                simulationWorkspaceView.printToLogWindow(String.format("%s KNOWS recipient MAC => sending string message\n", initiator), Color.GREEN);
                sendPacket(new NetworkConnection(initiator, next), initiator.getMacAddress(), recipientMac, new Packet(initiator.getIpAddress(), recipient.getIpAddress(), new StringMessage("googa")));
            } else {
                logger.info("Initiator DOESN'T KNOW recipient mac, sending ARP request, network communication: {} -> {}", initiator, next);
                simulationWorkspaceView.printToLogWindow(String.format("%s DOESN'T know recipient MAC => sending ARP request\n", initiator), Color.BLUE);
                sendArpRequest(new NetworkConnection(initiator, next), initiator.getMacAddress(), initiator.getIpAddress(), recipient.getIpAddress());
            }
        } else {
            if (initiatorRouterInterfacesRouter != recipientRouterInterfacesRouter){
                //this indicates they are on a completely different network, not just a different subnet
                simulationWorkspaceView.printToLogWindow(String.format("PC WAN communication not implemented (%s -> %s)\nPicking different one\n", initiator, recipient), Color.RED);
                pickRandomLanCommunication();
                return;
            }

            logger.warn("Initiator {}, ip {} and recipient {}, ip {} AREN'T on the same network", initiator, initiator.getIpAddress(), recipient, recipient.getIpAddress());
            MACAddress defaultGatewayMac = initiator.queryArp(initiator.getDefaultGateway());
            if (defaultGatewayMac != null) {
                logger.info("Initiator {}, ip {} KNOWS default gateway mac (ip {}), sending string message, network communication: {} -> {}", initiator, initiator.getIpAddress(), initiator.getDefaultGateway(), initiator, next);
                simulationWorkspaceView.printToLogWindow(String.format("%s KNOWS default gateway MAC => sending string message\n", initiator), Color.GREEN);
                sendPacket(new NetworkConnection(initiator, next),
                        initiator.getMacAddress(),
                        defaultGatewayMac,
                        new Packet(initiator.getIpAddress(), recipient.getIpAddress(), new StringMessage("googa")));
            } else {
                logger.info("Initiator {}, ip {} DOESN'T KNOW default gateway mac, sending arp request, network communication: {} -> {}", initiator, initiator.getIpAddress(), initiator, next);
                simulationWorkspaceView.printToLogWindow(String.format("%s DOESN'T know default gateway MAC => sending ARP request\n", initiator), Color.BLUE);
                sendPacket(new NetworkConnection(initiator, next),
                        initiator.getMacAddress(),
                        MACAddress.ipv4Broadcast(),
                        new Packet(initiator.getIpAddress(), initiator.getDefaultGateway(), new ArpRequestMessage(initiator.getDefaultGateway(), initiator.getIpAddress(), initiator.getMacAddress())));
            }
        }
    }

    /**
     * Sends a network packet through the simulation infrastructure.
     *
     * @param networkConnection The network connection through which the packet will be sent.
     * @param sourceMac         The source MAC address.
     * @param destinationMac    The destination MAC address.
     * @param packet            The packet to be sent.
     */
    public void sendPacket(NetworkConnection networkConnection, MACAddress sourceMac, MACAddress destinationMac, Packet packet) {
        Frame ethernetFrame = new Frame(sourceMac, destinationMac, packet);
        outboundQueue.add(new Pair<>(networkConnection, ethernetFrame));
    }

    /**
     * Handles the packet processing logic, distributing frames to their respective destination devices.
     */
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

    /**
     * Forwards a frame to the next device in the network path.
     *
     * @param networkConnection The network connection representing the path of the frame.
     * @param frame             The frame being forwarded.
     */
    public void forwardToNextDevice(NetworkConnection networkConnection, Frame frame) {
        if (networkConnection.getEndDevice() instanceof PCModel pc) {
            handleFrameOnPc(pc, networkConnection, frame);

        } else if (networkConnection.getEndDevice() instanceof SwitchModel switchModel) {
            handleFrameOnSwitch(switchModel, networkConnection, frame);

        } else if (networkConnection.getEndDevice() instanceof RouterInterface routerInterface) {
            handleFrameOnRouter(routerInterface, networkConnection, frame);
        }
    }

    /**
     * Processes frames received by a PC, handling different types of network messages.
     *
     * @param pc                The PC receiving the frame.
     * @param networkConnection The network connection over which the frame was received.
     * @param frame             The frame to be processed.
     */
    public void handleFrameOnPc(PCModel pc, NetworkConnection networkConnection, Frame frame) {
        if (frame.getDestinationMac() == pc.getMacAddress() || frame.getPacket().getDestinationIp() == pc.getIpAddress()) {
            if (frame.getPacket().getMessage() instanceof StringMessage stringMessage) {
                logger.debug("Recipient {}, ip {} received STRING MESSAGE, body -> {}", pc, pc.getIpAddress(), stringMessage.getBody());
                simulationWorkspaceView.printToLogWindow(String.format("%s received string message: %s\n", pc, stringMessage.getBody()), Color.GREEN);
            } else if (frame.getPacket().getMessage() instanceof DhcpOfferMessage dhcpOfferMessage) {
                logger.debug("Recipient {}, ip {} received DHCP OFFER MESSAGE, body -> DG {}, Offered ip {}, Subnetmask {}", pc, pc.getIpAddress(), dhcpOfferMessage.getDefaultGateway(), dhcpOfferMessage.getOfferedIpAddress(), dhcpOfferMessage.getSubnetMask());
                pc.configure(dhcpOfferMessage.getOfferedIpAddress(), dhcpOfferMessage.getDefaultGateway(), dhcpOfferMessage.getSubnetMask());
                pc.updateArp(dhcpOfferMessage.getDefaultGateway(), frame.getSourceMac());
                updateLabelsRequest(pc);

                simulationWorkspaceView.printToLogWindow(String.format("%s sending DHCP-Response\n", pc), Color.ORANGE);
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
                simulationWorkspaceView.printToLogWindow(String.format("%s sending ARP-Response\n", pc), Color.LIGHTBLUE);
                sendPacket(new NetworkConnection(pc, pc.getConnection()),
                        pc.getMacAddress(),
                        arpRequestMessage.getRequesterMacAddress(),
                        new Packet(pc.getIpAddress(), arpRequestMessage.getRequesterIpAddress(), new ArpResponseMessage(pc.getMacAddress())));
            } else if (frame.getPacket().getMessage() instanceof ArpResponseMessage arpResponseMessage) {
                logger.debug("Recipient {}, ip {} received ARP RESPONSE MESSAGE from {}, body -> requested mac for device {}",
                        pc, pc.getIpAddress(), storage.getNetworkDeviceByMac(frame.getSourceMac()), storage.getNetworkDeviceByMac(arpResponseMessage.getRequestedMacAddress()));
                pc.updateArp(frame.getPacket().getSourceIp(), arpResponseMessage.getRequestedMacAddress());
            }
        }
    }

    /**
     * Handles frames received by a switch, forwarding them to the appropriate connected devices.
     *
     * @param switchModel       The switch processing the frame.
     * @param networkConnection The network connection over which the frame was received.
     * @param frame             The frame to be processed.
     */
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

    /**
     * Processes frames received by a router, handling different types of network messages.
     *
     * @param routerInterface   The router interface processing the frame.
     * @param networkConnection The network connection over which the frame was received.
     * @param frame             The frame to be processed.
     */
    public void handleFrameOnRouter(RouterInterface routerInterface, NetworkConnection networkConnection, Frame frame) {
        Message message = frame.getPacket().getMessage();
        if (message instanceof RipMessage ripMessage) {
            logger.debug("Recipient {}, ip {} received RIP MESSAGE", routerInterface, routerInterface.getIpAddress());
            routerInterface.getInterfacesRouter().updateRoutingTable(ripMessage.getRoutingTable(), frame.getPacket().getSourceIp());
        } else if (message instanceof DhcpDiscoverMessage dhcpDiscoverMessage) {
            logger.debug("Recipient {}, ip {} received DHCP DISCOVERY MESSAGE from source device {}",
                    routerInterface, routerInterface.getIpAddress(), storage.getNetworkDeviceByMac(dhcpDiscoverMessage.getSourceMac()));
            IPAddress offeredIpAddress = networksController.reserveIpAddressInNetwork(routerInterface.getNetwork());
            IPAddress defaultGateway = routerInterface.getIpAddress();
            SubnetMask subnetMask = routerInterface.getNetwork().getSubnetMask();
            simulationWorkspaceView.printToLogWindow(String.format("%s sending DHCP-Offer\n", routerInterface.getInterfacesRouter()), Color.RED);
            sendDhcpOffer(new NetworkConnection(routerInterface, networkConnection.getStartDevice()),
                    routerInterface.getMacAddress(),
                    dhcpDiscoverMessage.getSourceMac(),
                    routerInterface.getIpAddress(),
                    new DhcpOfferMessage(offeredIpAddress, defaultGateway, subnetMask));
        } else if (message instanceof DhcpResponseMessage) {
            logger.debug("Recipient {}, ip {} received DHCP RESPONSE MESSAGE from source device {}", routerInterface, routerInterface.getIpAddress(), storage.getNetworkDeviceByMac(frame.getSourceMac()));
            routerInterface.getInterfacesRouter().updateArp(frame.getPacket().getSourceIp(), frame.getSourceMac());
            simulationWorkspaceView.printToLogWindow(String.format("%s sending DHCP-Ack message\n", routerInterface.getInterfacesRouter()), Color.YELLOWGREEN);
            sendDhcpAck(new NetworkConnection(routerInterface, networkConnection.getStartDevice()),
                    routerInterface.getMacAddress(),
                    frame.getSourceMac(),
                    routerInterface.getIpAddress(),
                    frame.getPacket().getSourceIp(),
                    new DhcpAckMessage()
            );
        } else if (message instanceof ArpRequestMessage arpRequestMessage) {
            // this means they are on a same network
            if (arpRequestMessage.getRequestedIpAddress() != routerInterface.getIpAddress()) {
                for (NetworkDeviceModel networkDevice : routerInterface.getDirectConnections()) {
                    if (networkDevice instanceof PCModel pc && pc.getIpAddress() == arpRequestMessage.getRequestedIpAddress()) {
                        MACAddress recipientMac = routerInterface.getInterfacesRouter().queryArp(pc.getIpAddress());
                        if (recipientMac == null) {
                            logger.warn("router interface {}, ip {} DOES'T know mac of device {}, ip {}", routerInterface, routerInterface.getIpAddress(), pc, pc.getIpAddress());
                        } else {
                            sendPacket(new NetworkConnection(routerInterface, pc),
                                    routerInterface.getMacAddress(),
                                    recipientMac,
                                    frame.getPacket());
                        }

                    }
                }
            }
        } else if (message instanceof ArpResponseMessage arpResponseMessage) {
            logger.info("router interface {}, ip {} received arp response message", routerInterface, routerInterface.getIpAddress());
            if (arpResponseMessage.getRequestedMacAddress() != routerInterface.getMacAddress()) {

                MACAddress dstMac = frame.getDestinationMac();
                for (NetworkDeviceModel networkDevice : routerInterface.getDirectConnections()) {
                    if (networkDevice instanceof PCModel pc) {
                        if (pc.getMacAddress() == dstMac) {
                            sendPacket(new NetworkConnection(routerInterface, pc),
                                    routerInterface.getMacAddress(),
                                    dstMac, frame.getPacket()
                            );
                        }
                    }
                }
            } else {
                RouterModel router = routerInterface.getInterfacesRouter();
                IPAddress sourceIp = frame.getPacket().getSourceIp();
                router.updateArp(sourceIp, arpResponseMessage.getRequestedMacAddress());
            }
        } else if (message instanceof StringMessage stringMessage) {
            logger.debug("Recipient {}, ip {}, received STRING MESSAGE", routerInterface, routerInterface.getIpAddress());
            IPAddress forwardToIp = frame.getPacket().getDestinationIp();
            RouterModel router = routerInterface.getInterfacesRouter();

            LinkedHashMap<Network, RouterInterface> networkRouterInterfaceMap = router.getRouterInterfaces();

            logger.info("{}, ip {} is looking for appropriate subnet", routerInterface, routerInterface.getIpAddress());
            for (ConcurrentHashMap.Entry<Network, RouterInterface> entry : networkRouterInterfaceMap.entrySet()) {
                Network network = entry.getKey();
                RouterInterface ri = entry.getValue();

                if ((network.getNetworkIpAddress().toLong() & network.getSubnetMask().toLong()) == (forwardToIp.toLong() & routerInterface.getNetwork().getSubnetMask().toLong())) {
                    logger.debug("Found the correct router interface {}, ip {}, on network ip {}, DST IP {}", ri, ri.getIpAddress(), network.getNetworkIpAddress(), forwardToIp);
                    MACAddress dstMacAddress = router.queryArp(forwardToIp);
                    if (dstMacAddress == null) {
                        logger.warn("Router interface {}, ip {} DOES'T KNOW mac of dst device", ri, ri.getIpAddress());
                        return;
                        //knows MAC
                    } else {
                        logger.info("{}, ip {} KNOWS the mac of dst device, forwarding message", ri, ri.getIpAddress());
                        sendPacket(new NetworkConnection(ri, ri.getFirstConnectedDevice()),
                                ri.getMacAddress(),
                                dstMacAddress,
                                new Packet(ri.getIpAddress(), forwardToIp, stringMessage));
                    }
                }

            }
        }
    }

    /**
     * Sends a DHCP discovery packet over the specified network connection.
     *
     * @param networkConnection The network connection to send the packet on.
     * @param sourceMac         The MAC address of the source device.
     */
    public void sendDhcpDiscovery(NetworkConnection networkConnection, MACAddress sourceMac) {
        sendPacket(networkConnection, sourceMac, MACAddress.ipv4Broadcast(), new Packet(null, null, new DhcpDiscoverMessage(sourceMac)));
    }

    /**
     * Sends an ARP request over a network connection.
     *
     * @param networkConnection The network connection over which the ARP request is sent.
     * @param senderMac         The MAC address of the sender.
     * @param senderIp          The IP address of the sender.
     * @param targetIp          The target IP address for the ARP request.
     */
    public void sendArpRequest(NetworkConnection networkConnection, MACAddress senderMac, IPAddress senderIp, IPAddress targetIp) {
        sendPacket(networkConnection, senderMac, MACAddress.ipv4Broadcast(), new Packet(senderIp, targetIp, new ArpRequestMessage(targetIp, senderIp, senderMac)));
    }

    /**
     * Sends a DHCP offer response to a DHCP discovery.
     *
     * @param networkConnection The network connection to send the DHCP offer on.
     * @param sourceMac         The source MAC address.
     * @param dstMac            The destination MAC address.
     * @param sourceIpAddress   The IP address of the DHCP server.
     * @param dhcpOfferMessage  The DHCP offer message containing the offered IP address and subnet mask.
     */
    public void sendDhcpOffer(NetworkConnection networkConnection, MACAddress sourceMac, MACAddress dstMac, IPAddress sourceIpAddress, DhcpOfferMessage dhcpOfferMessage) {
        sendPacket(networkConnection, sourceMac, dstMac, new Packet(sourceIpAddress, IPAddress.nullIpAddress(), dhcpOfferMessage));
    }

    /**
     * Sends a DHCP response message, typically acknowledging configuration settings.
     *
     * @param networkConnection   The network connection to send the DHCP response on.
     * @param sourceMac           The source MAC address.
     * @param dstMac              The destination MAC address.
     * @param sourceIpAddress     The IP address of the DHCP server.
     * @param dstIpAddress        The IP address of the DHCP client.
     * @param dhcpResponseMessage The DHCP response message to be sent.
     */
    public void sendDhcpResponse(NetworkConnection networkConnection, MACAddress sourceMac, MACAddress dstMac, IPAddress sourceIpAddress, IPAddress dstIpAddress, DhcpResponseMessage dhcpResponseMessage) {
        sendPacket(networkConnection, sourceMac, dstMac, new Packet(sourceIpAddress, dstIpAddress, dhcpResponseMessage));
    }

    /**
     * Sends a DHCP acknowledgment message.
     *
     * @param networkConnection The network connection to send the DHCP ACK on.
     * @param sourceMac         The source MAC address.
     * @param dstMac            The destination MAC address.
     * @param sourceIpAddress   The IP address of the DHCP server.
     * @param dstIpAddress      The IP address of the DHCP client.
     * @param dhcpAckMessage    The DHCP acknowledgment message.
     */
    public void sendDhcpAck(NetworkConnection networkConnection, MACAddress sourceMac, MACAddress dstMac, IPAddress sourceIpAddress, IPAddress dstIpAddress, DhcpAckMessage dhcpAckMessage) {
        sendPacket(networkConnection, sourceMac, dstMac, new Packet(sourceIpAddress, dstIpAddress, dhcpAckMessage));
    }

    /**
     * Sends a frame over the network with visual animations representing the packet transfer.
     * This method is intended to be called on the JavaFX application thread.
     *
     * @param networkConnection The network connection through which the frame is sent.
     * @param frame             The frame containing the packet to be sent.
     */
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
                    logger.warn("Path transition could not be initialized.");
                    return;
                }
                pathTransition.setOnFinished(event -> {
                    simulationWorkspaceView.removeNode(visualFrame);
                    forwardToNextDevice(networkConnection, frame);
                });

                pathTransition.play();
            } catch (Exception e) {
                logger.error("an error occurred while trying to animate the frame transfer.", e);
            }
        });
    }

    /**
     * Prepares a path transition for the animation of a network packet.
     *
     * @param visualFrame The visual representation of the frame.
     * @param startDevice The starting device of the animation.
     * @param endDevice   The ending device of the animation.
     * @return The path transition for the animation.
     */
    private PathTransition preparePathTransition(Rectangle visualFrame, NetworkDeviceModel startDevice, NetworkDeviceModel endDevice) {
        ConnectionLine connectionLine = simulationWorkspaceView.getConnectionLine(startDevice, endDevice);
        if (connectionLine == null) {
            logger.warn("no connection line found for the specified devices.");
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

    /**
     * Creates a visual representation of a frame based on the type of message it carries.
     *
     * @param frame The frame for which the visual representation is created.
     * @return A rectangle colored according to the type of message in the frame.
     */
    public Rectangle createVisualFrame(Frame frame) {
        Rectangle rectangle = new Rectangle(10, 10);

        Message message = frame.getPacket().getMessage();
        switch (message.getClass().getSimpleName()) {
            case "DhcpDiscoverMessage":
                rectangle.setFill(Color.DARKRED);
                break;
            case "DhcpOfferMessage":
                rectangle.setFill(Color.RED);
                break;
            case "DhcpResponseMessage":
                rectangle.setFill(Color.ORANGE);
                break;
            case "DhcpAckMessage":
                rectangle.setFill(Color.GREENYELLOW);
                break;
            case "ArpRequestMessage":
                rectangle.setFill(Color.BLUE);
                break;
            case "ArpResponseMessage":
                rectangle.setFill(Color.LIGHTBLUE);
                break;
            case "StringMessage":
                rectangle.setFill(Color.GREEN);
                break;
            default:
                rectangle.setFill(Color.GRAY);
                break;
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

