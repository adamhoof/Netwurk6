package view;

import common.NetworkDeviceType;
import javafx.scene.image.Image;

public class PCView extends NetworkDeviceView {
    public PCView(Image image) {
        super(NetworkDeviceType.PC, image);
    }

    @Override
    public NetworkDeviceView deepCopy(){
        return new PCView(this.getImage());
    }
}
