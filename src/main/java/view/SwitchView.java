package view;

import common.NetworkDeviceType;
import javafx.scene.image.Image;

/**
 * Represents a switch view in a network simulation environment.
 * This class extends NetworkDeviceView to provide specific functionalities related to network switches.
 */
public class SwitchView extends NetworkDeviceView {

    /**
     * Constructs a SwitchView with a specific image representing the switch.
     *
     * @param image the image to be displayed as the switch's icon
     */
    public SwitchView(Image image) {
        super(NetworkDeviceType.SWITCH, image);
    }

    /**
     * Creates a deep copy of this SwitchView instance.
     * This method is used to clone the SwitchView, allowing it to be placed multiple times within the simulation without reference conflicts.
     *
     * @return a new SwitchView instance with the same image
     */
    @Override
    public NetworkDeviceView deepCopy() {
        return new SwitchView(this.getImageView().getImage());
    }
}
