package view;

import javafx.scene.image.Image;

public class Switch extends NetworkDevice {
    public Switch(Image image) {
        super(NetworkDeviceType.SWITCH, image);
    }

    @Override
    public NetworkDevice deepCopy() {
        return new Switch(this.getImage());
    }
}
