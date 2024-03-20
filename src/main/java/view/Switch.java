package view;

import javafx.scene.image.ImageView;

public class Switch extends NetworkDevice {
    public Switch(ImageView imageView) {
        super(NetworkDeviceType.SWITCH, imageView);
    }

    @Override
    public NetworkDevice deepCopy(){
        return new Switch(new ImageView(this.getImageView().getImage()));
    }
}
