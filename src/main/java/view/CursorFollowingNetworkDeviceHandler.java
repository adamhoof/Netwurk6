package view;

import common.DecrementNameCounterRequestEvent;
import common.GlobalEventBus;

/**
 * Handles the functionality for a cursor-following network device within the simulation.
 * Allows for dynamic interaction with network devices as they follow the cursor before placement.
 */
public class CursorFollowingNetworkDeviceHandler {
    NetworkDeviceView cursorFollowingNetworkDevice;

    public CursorFollowingNetworkDeviceHandler() {
        GlobalEventBus.register(this);
    }

    public NetworkDeviceView get() {
        return cursorFollowingNetworkDevice;
    }

    public void set(NetworkDeviceView cursorFollowingNetworkDevice) {
        this.cursorFollowingNetworkDevice = cursorFollowingNetworkDevice;
    }

    public boolean isFollowing() {
        return cursorFollowingNetworkDevice != null;
    }

    public void place() {
        cursorFollowingNetworkDevice.setOpacity(1.0);
        cursorFollowingNetworkDevice = null;
    }

    public void drop() {
        GlobalEventBus.post(new DecrementNameCounterRequestEvent(cursorFollowingNetworkDevice.getNetworkDeviceType()));
        cursorFollowingNetworkDevice = null;
    }
}
