package view;

import common.Connection;
import javafx.scene.control.Label;
import javafx.scene.shape.Line;

public class ConnectionLine extends Line implements Connection {
    private final NetworkDeviceView startDevice;
    private final NetworkDeviceView endDevice;

    private final Label middleLabel;
    private final Label startLabel;
    private final Label endLabel;

    public ConnectionLine(double startX, double startY, double endX, double endY, NetworkDeviceView startDevice, NetworkDeviceView endDevice, String middleLabel, String startLabel, String endLabel) {
        super(startX, startY, endX, endY);
        this.startDevice = startDevice;
        this.endDevice = endDevice;
        this.middleLabel = new Label(middleLabel);
        this.middleLabel.setStyle("-fx-text-fill: #FF0000; -fx-font-weight: bold;");
        this.startLabel = new Label(startLabel);
        this.startLabel.setStyle("-fx-text-fill: #ff0942; -fx-font-style: italic;");
        this.endLabel = new Label(endLabel);
        this.endLabel.setStyle("-fx-text-fill: #ff0942; -fx-font-style: italic;");
    }

    public NetworkDeviceView getStartDevice() {
        return startDevice;
    }

    public NetworkDeviceView getEndDevice() {
        return endDevice;
    }


    public void updateLabelPosition(Label label, double x, double y) {
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.toFront();
    }

    public Label getMiddleLabel() {
        return middleLabel;
    }

    public Label getStartLabel() {
        return startLabel;
    }

    public Label getEndLabel() {
        return endLabel;
    }
}
