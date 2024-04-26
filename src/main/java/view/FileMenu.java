package view;

import io.JsonExporter;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileMenu extends Menu {
    private final NetworkDeviceViewProvider provider;

    public FileMenu(String name, NetworkDeviceViewProvider provider) {
        super(name);
        this.provider = provider;
        MenuItem save = new MenuItem("Save");
        save.setOnAction(clickEvent -> saveEventHandler(save));

        MenuItem load = new MenuItem("Load");

        this.getItems().addAll(save, load);
    }

    private void saveEventHandler(MenuItem save) {
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save simulation setup");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            Window window = save.getParentPopup().getOwnerWindow();
            File file = fileChooser.showSaveDialog(window);
            if (file != null) {
                saveDevicesToFile(file);
            }
        }
    }

    private void saveDevicesToFile(File file) {
        JsonExporter jsonExporter = new JsonExporter();
        try {
            String json = jsonExporter.exportDevices(provider.getDevices());
            Files.write(Paths.get(file.toURI()), json.getBytes());
        } catch (IOException e) {
            System.err.println("Failed to save devices: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
