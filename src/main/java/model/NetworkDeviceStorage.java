package model;

import com.google.common.eventbus.Subscribe;
import common.GlobalEventBus;
import common.RouterInterfaceCreatedEvent;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Provides storage and management for different types of network device models,
 * including routers and PCs, in a network simulation environment.
 */
public class NetworkDeviceStorage {
    public NetworkDeviceStorage(){
        GlobalEventBus.register(this);
    }
    Map<UUID, NetworkDeviceModel> networkDeviceModels = new HashMap<>();
    ArrayList<RouterModel> routerModels = new ArrayList<>();
    List<PCModel> pcModels = new CopyOnWriteArrayList<>();
    ArrayList<RouterInterface> routerInterfaces = new ArrayList<>();

    /**
     * Adds a generic network device model to the storage.
     *
     * @param networkDeviceModel The network device model to be added.
     */
    public void add(NetworkDeviceModel networkDeviceModel) {
        networkDeviceModels.put(networkDeviceModel.getUuid(), networkDeviceModel);
    }

    /**
     * Adds a router model to the storage and maintains it in the router-specific list.
     *
     * @param routerModel The router model to be added.
     */
    public void addRouter(RouterModel routerModel) {
        networkDeviceModels.put(routerModel.getUuid(), routerModel);
        routerModels.add(routerModel);
    }

    /**
     * Adds a PC model to the storage and maintains it in the PC-specific list.
     *
     * @param pcModel The PC model to be added.
     */
    public void addPc(PCModel pcModel) {
        networkDeviceModels.put(pcModel.getUuid(), pcModel);
        pcModels.add(pcModel);
    }

    @Subscribe
    public void handleRouterInterfaceCreatedEvent(RouterInterfaceCreatedEvent event){
        addRouterInterface(event.routerInterface());
    }

    public void addRouterInterface(RouterInterface routerInterface) {
        routerInterfaces.add(routerInterface);
    }

    public List<RouterInterface> getRouterInterfaces(){
        return routerInterfaces;
    }

    /**
     * Retrieves a network device model by its UUID.
     *
     * @param uuid The UUID of the network device model.
     * @return The corresponding network device model, or null if not found.
     */
    public NetworkDeviceModel get(UUID uuid) {
        return networkDeviceModels.getOrDefault(uuid, null);
    }

    /**
     * Retrieves all router models stored in the system.
     *
     * @return An ArrayList of all stored router models.
     */
    public ArrayList<RouterModel> getRouterModels() {
        return routerModels;
    }

    /**
     * Retrieves a specific router model by its UUID.
     *
     * @param uuid The UUID of the router model.
     * @return The router model if found, or null otherwise.
     */
    public RouterModel getRouterModel(UUID uuid) {
        return (RouterModel) networkDeviceModels.getOrDefault(uuid, null);
    }

    /**
     * Retrieves a specific PC model by its UUID.
     *
     * @param uuid The UUID of the PC model.
     * @return The PC model if found, or null otherwise.
     */
    public PCModel getPcModel(UUID uuid) {
        return (PCModel) networkDeviceModels.getOrDefault(uuid, null);
    }

    /**
     * Retrieves a network device model by its MAC address.
     *
     * @param macAddress The MAC address of the network device model.
     * @return The network device model if found, or null otherwise.
     */
    public NetworkDeviceModel getNetworkDeviceByMac(MACAddress macAddress) {
        UUID uuidFromMac = UUID.fromString(macAddress.toString());
        return networkDeviceModels.getOrDefault(uuidFromMac, null);
    }

    /**
     * Retrieves all PC models stored in the system.
     *
     * @return An ArrayList of all stored PC models.
     */
    public ArrayList<PCModel> getPcModels() {
        return new ArrayList<>(pcModels);
    }
}
