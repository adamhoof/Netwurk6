package view;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.UUID;

public abstract class NetworkDevice extends Node {
    private ImageView imageView;
    private final NetworkDeviceType networkDeviceType;

    private final UUID id;

    public NetworkDevice(NetworkDeviceType networkDeviceType, ImageView image) {
        this.networkDeviceType = networkDeviceType;
        this.imageView = image;
        this.id = UUID.randomUUID();
    }

    public NetworkDeviceType getNetworkDeviceType() {
        return networkDeviceType;
    }

    public ImageView getImageView(){
        return imageView;
    }

    public abstract NetworkDevice deepCopy();
}
