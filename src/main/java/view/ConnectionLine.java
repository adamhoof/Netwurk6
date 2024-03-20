package view;

import javafx.scene.shape.Line;

public class ConnectionLine extends Line {
    private final NetworkDevice startDevice;
    private final NetworkDevice endDevice;

    public ConnectionLine(double startX, double startY, double endX, double endY, NetworkDevice startDevice, NetworkDevice endDevice) {
        super(startX, startY, endX, endY);
        this.startDevice = startDevice;
        this.endDevice = endDevice;
    }

    public NetworkDevice getStartDevice() {
        return startDevice;
    }

    public NetworkDevice getEndDevice() {
        return endDevice;
    }
}
