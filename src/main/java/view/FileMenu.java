package view;

import io.ConnectionLineDTO;
import io.JsonExporter;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMenu extends Menu {
    private final NetworkDataProvider provider;

    public FileMenu(String name, NetworkDataProvider provider) {
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
                try {
                    exportNetworkData(file);
                } catch (IOException e) {
                    System.err.println("Failed to save network data: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void exportNetworkData(File file) throws IOException {
        JsonExporter jsonExporter = new JsonExporter();
        List<NetworkDeviceView> devices = provider.getDevices();

        List<ConnectionLineDTO> connectionDTOs = new ArrayList<>();
        for (ConnectionLine connectionLine : provider.getConnectionLines()) {

            UUID startDeviceUuid = connectionLine.getStartDevice().getUuid();
            UUID endDeviceUuid = connectionLine.getEndDevice().getUuid();

            ConnectionLineDTO dto = new ConnectionLineDTO(startDeviceUuid, endDeviceUuid);
            connectionDTOs.add(dto);
        }

        jsonExporter.exportNetworkData(devices, connectionDTOs, file);
    }
}
