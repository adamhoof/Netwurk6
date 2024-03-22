package view;

import common.NetworkDevice;
import common.NetworkDeviceType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class NetworkDeviceView extends ImageView implements NetworkDevice {
    private final NetworkDeviceType networkDeviceType;

    private final UUID uuid;

    private final List<ConnectionLine> connections = new ArrayList<>();

    public NetworkDeviceView(NetworkDeviceType networkDeviceType, Image image) {
        super(image);
        this.networkDeviceType = networkDeviceType;
        this.uuid = UUID.randomUUID();
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

    public List<ConnectionLine> getConnections() {
        return connections;
    }
}
