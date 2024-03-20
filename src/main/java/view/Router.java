package view;

import javafx.scene.image.ImageView;

public class Router extends NetworkDevice {
    public Router(ImageView imageView) {
        super(NetworkDeviceType.ROUTER, imageView);
    }

    @Override
    public NetworkDevice deepCopy() {
        return new Router(new ImageView(this.getImageView().getImage()));

    }
}
