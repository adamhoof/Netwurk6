package view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.UUID;

public abstract class NetworkDevice extends ImageView {
    private final NetworkDeviceType networkDeviceType;

    private final UUID uuid;

    public NetworkDevice(NetworkDeviceType networkDeviceType, Image image) {
        super(image);
        this.networkDeviceType = networkDeviceType;
        this.uuid = UUID.randomUUID();
    }

    public NetworkDeviceType getNetworkDeviceType() {
        return networkDeviceType;
    }

    public UUID getUUID() {
        return uuid;
    }

    public abstract NetworkDevice deepCopy();
}
