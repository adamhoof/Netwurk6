package view;

public class CursorFollowingNetworkDeviceHandler {
    NetworkDeviceView cursorFollowingNetworkDevice;

    public CursorFollowingNetworkDeviceHandler() {
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
}
