package view;

import javafx.scene.image.Image;

public class Router extends NetworkDevice {
    public Router(Image image) {
        super(NetworkDeviceType.ROUTER, image);
    }

    @Override
    public NetworkDevice deepCopy() {
        return new Router(this.getImage());

    }
}
