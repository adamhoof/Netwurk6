package view;

import javafx.scene.image.ImageView;

public class PC extends NetworkDevice {
    public PC(ImageView imageView) {
        super(NetworkDeviceType.PC, imageView);
    }

    @Override
    public NetworkDevice deepCopy(){
        return new PC(new ImageView(this.getImageView().getImage()));
    }
}
