package view;

import javafx.scene.image.Image;

public class PC extends NetworkDevice {
    public PC(Image image) {
        super(NetworkDeviceType.PC, image);
    }

    @Override
    public NetworkDevice deepCopy(){
        return new PC(this.getImage());
    }
}
