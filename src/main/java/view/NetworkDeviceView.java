package view;

import common.NetworkDevice;
import common.NetworkDeviceType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class NetworkDeviceView extends StackPane implements NetworkDevice {
    private final NetworkDeviceType networkDeviceType;

    private final ArrayList<String> labels;
    private final ImageView imageView;

    private final Label name;

    private final UUID uuid;


    private final List<ConnectionLine> connections = new ArrayList<>();

    public NetworkDeviceView(NetworkDeviceType networkDeviceType, Image image) {
        this.labels = new ArrayList<>();
        this.imageView = new ImageView(image);
        this.name = new Label();
        this.name.setStyle("-fx-text-fill: #FF0000; -fx-font-weight: bold;");
        this.name.setLayoutY(this.getLayoutY());
        this.networkDeviceType = networkDeviceType;
        this.uuid = UUID.randomUUID();
        this.getChildren().addAll(imageView, this.name);
    }

    public NetworkDeviceType getNetworkDeviceType() {
        return networkDeviceType;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public abstract NetworkDeviceView deepCopy();

    public void setName(String name) {
        this.name.setText(name);
    }

    public String getName() {
        return name.getText();
    }

    public void addConnectionLine(ConnectionLine line) {
        connections.add(line);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageViewFitWidth(int width) {
        imageView.setFitWidth(width);
    }

    public void setImageViewFitHeight(int height) {
        imageView.setFitWidth(height);
    }

    public List<ConnectionLine> getConnections() {
        return connections;
    }
}
