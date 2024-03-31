package view;

import common.NetworkDeviceType;
import javafx.scene.image.Image;

public class SwitchView extends NetworkDeviceView {
    public SwitchView(Image image) {
        super(NetworkDeviceType.SWITCH, image);
    }

    @Override
    public NetworkDeviceView deepCopy() {
        return new SwitchView(this.getImageView().getImage());
    }

}
