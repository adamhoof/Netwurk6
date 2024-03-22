package view;

import javafx.scene.shape.Line;

public class ConnectionLine extends Line {
    private final NetworkDeviceView startDevice;
    private final NetworkDeviceView endDevice;

    public ConnectionLine(double startX, double startY, double endX, double endY, NetworkDeviceView startDevice, NetworkDeviceView endDevice) {
        super(startX, startY, endX, endY);
        this.startDevice = startDevice;
        this.endDevice = endDevice;
    }

    public NetworkDeviceView getStartDevice() {
        return startDevice;
    }

    public NetworkDeviceView getEndDevice() {
        return endDevice;
    }
}
