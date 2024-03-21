package view;

public class CursorFollowingNetworkDeviceHandler {
    NetworkDevice cursorFollowingNetworkDevice;

    public CursorFollowingNetworkDeviceHandler() {
    }

    public NetworkDevice get() {
        return cursorFollowingNetworkDevice;
    }

    public void set(NetworkDevice cursorFollowingNetworkDevice) {
        this.cursorFollowingNetworkDevice = cursorFollowingNetworkDevice;
    }

    public boolean isFollowing() {
        return cursorFollowingNetworkDevice != null;
    }

    public void place() {
        cursorFollowingNetworkDevice.setOpacity(1.0);
        cursorFollowingNetworkDevice = null;
    }
}
