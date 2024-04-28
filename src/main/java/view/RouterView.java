package view;

import common.NetworkDeviceType;
import javafx.scene.image.Image;

import java.util.UUID;

/**
 * Represents a router view in a network simulation environment.
 * This class extends the NetworkDeviceView to provide specific functionalities related to routers.
 */
public class RouterView extends NetworkDeviceView {

    /**
     * Constructs a RouterView with a specific image representing the router.
     *
     * @param image the image to be displayed as the router's icon
     */
    public RouterView(UUID uuid, Image image) {
        super(uuid, NetworkDeviceType.ROUTER, image);
    }

    /**
     * Creates a deep copy of this RouterView instance.
     * This method is used to clone the RouterView, allowing it to be placed multiple times within the simulation without reference conflicts.
     *
     * @return a new RouterView instance with the same image
     */
    @Override
    public NetworkDeviceView deepCopy() {
        return new RouterView(UUID.randomUUID(), this.getImageView().getImage());
    }
}
