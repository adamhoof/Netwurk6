package view;

import common.NetworkDeviceType;
import javafx.scene.image.Image;

import java.util.UUID;

/**
 * Represents a PC view in a network simulation environment.
 * This class extends NetworkDeviceView to provide specific functionalities related to personal computers within the simulation.
 */
public class PCView extends NetworkDeviceView {

    /**
     * Constructs a PCView with a specific image representing the PC.
     *
     * @param image the image to be displayed as the PC's icon
     */
    public PCView(UUID uuid, Image image) {
        super(uuid, NetworkDeviceType.PC, image);
    }

    /**
     * Creates a deep copy of this PCView instance.
     * This method is used to clone the PCView, allowing it to be placed multiple times within the simulation without reference conflicts.
     *
     * @return a new PCView instance with the same image
     */
    @Override
    public NetworkDeviceView deepCopy() {
        return new PCView(UUID.randomUUID(), this.getImageView().getImage());
    }
}
