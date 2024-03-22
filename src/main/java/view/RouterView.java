package view;

import common.NetworkDeviceType;
import javafx.scene.image.Image;

public class RouterView extends NetworkDeviceView {
    public RouterView(Image image) {
        super(NetworkDeviceType.ROUTER, image);
    }

    @Override
    public NetworkDeviceView deepCopy() {
        return new RouterView(this.getImage());

    }
}
