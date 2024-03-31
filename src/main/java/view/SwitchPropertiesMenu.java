package view;

import common.RouterProperties;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class SwitchPropertiesMenu {
    Dialog<ButtonType> dialog = new Dialog<>();
    TextField macAddressField = new TextField();
    TextField ipAddressField = new TextField();

    public SwitchPropertiesMenu() {
        GridPane form = new GridPane();

        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10, 10, 10, 10));

        form.add(new Label("MAC address:"), 0, 0);
        form.add(macAddressField, 1, 0);
        form.add(new Label("IP address:"), 0, 1);
        form.add(ipAddressField, 1, 1);

        dialog.setTitle("Router Properties");
        dialog.setHeaderText("Edit Device Properties");

        dialog.getDialogPane().setContent(form);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                //
            }
            return null;
        });
    }

    public void show(SwitchPropertiesMenu switchPropertiesMenu) {
        dialog.showAndWait();
    }
}
