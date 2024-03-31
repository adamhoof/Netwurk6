package view;

import common.NetworkDevice;
import common.NetworkDeviceType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class NetworkDeviceView extends StackPane implements NetworkDevice {
    private final NetworkDeviceType networkDeviceType;

    private ArrayList<String> labels;
    private ImageView imageView;

    private final UUID uuid;


    private final List<ConnectionLine> connections = new ArrayList<>();

    public NetworkDeviceView(NetworkDeviceType networkDeviceType, Image image) {
        this.labels = new ArrayList<>();
        this.imageView = new ImageView(image);
        this.networkDeviceType = networkDeviceType;
        this.uuid = UUID.randomUUID();
        this.getChildren().addAll(imageView);
    }

    public NetworkDeviceType getNetworkDeviceType() {
        return networkDeviceType;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public abstract NetworkDeviceView deepCopy();

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
