package controller;

import model.Network;
import model.NetworkDeviceStorage;
import model.RouterModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimulationController {
    private final ExecutorService threadPool;

    private final NetworkDeviceStorage storage;

    private final NetworksController networksController;

    public SimulationController(NetworkDeviceStorage storage, NetworksController networksController) {
        this.threadPool = Executors.newFixedThreadPool(50);
        this.storage = storage;
        this.networksController = networksController;
    }

    public void startSimulation() {

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
            for (RouterModel connectedRouter : storage.getRoutersConnections(router)) {
                Network sharedNetwork = networksController.getSharedNetwork(router, connectedRouter);
                if (sharedNetwork != null) {
                    connectedRouter.receiveRoutingTable(router.getRoutingTable(), router.ipAddressInNetwork(sharedNetwork));
                }
            }
        }
    }
}

