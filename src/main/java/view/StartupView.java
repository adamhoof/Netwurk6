package view;

import common.AutoNameGenerator;
import common.GlobalEventBus;
import controller.MasterController;
import controller.NetworksController;
import controller.SimulationController;
import io.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.NetworkDeviceStorage;

import java.io.File;
import java.util.List;
import java.util.Map;

public class StartupView {

    private final Stage stage;
    private Scene scene;
    VBox menu;

    public StartupView(Stage stage) {
        this.stage = stage;
        GlobalEventBus.register(this);
        initializeView();
    }

    private void initializeView() {
        menu = new VBox();
        menu.setSpacing(20);
        menu.setAlignment(Pos.CENTER);
        menu.setMinSize(200, 200);

        Button newButton = new Button("New");
        newButton.setPrefSize(200, 40);
        newButton.setOnAction(event -> {
            SimulationWorkspaceView simulationWorkspaceView = new SimulationWorkspaceView(stage);
            NetworksController networksController = new NetworksController();
            NetworkDeviceStorage networkDeviceStorage = new NetworkDeviceStorage();
            SimulationController simulationController = new SimulationController(simulationWorkspaceView, networkDeviceStorage, networksController);
            MasterController masterController = new MasterController(simulationWorkspaceView, networkDeviceStorage, networksController, simulationController);

            simulationWorkspaceView.display();
        });

        Button loadButton = new Button("Load");
        loadButton.setPrefSize(200, 40);
        loadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
            fileChooser.getExtensionFilters().add(jsonFilter);

            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                Platform.runLater(() -> {
                    JsonImporter jsonImporter = new JsonImporter();
                    System.out.println("Loading simulation from: " + selectedFile.getAbsolutePath());
                    SimulationWorkspaceView simulationWorkspaceView = new SimulationWorkspaceView(stage);
                    NetworksController networksController = new NetworksController();
                    NetworkDeviceStorage networkDeviceStorage = new NetworkDeviceStorage();
                    SimulationController simulationController = new SimulationController(simulationWorkspaceView, networkDeviceStorage, networksController);
                    MasterController masterController = new MasterController(simulationWorkspaceView, networkDeviceStorage, networksController, simulationController);

                    NetworkData networkData = jsonImporter.importNetworkData(selectedFile);
                    insertDevices(networkData.devices(), simulationWorkspaceView, masterController);
                    insertConnections(networkData, simulationWorkspaceView, masterController);
                    AutoNameGenerator.getInstance().setRouterNameCounter(networkData.autoNameGeneratorDTO().routerNameCounter());
                    AutoNameGenerator.getInstance().setSwitchNameCounter(networkData.autoNameGeneratorDTO().switchNameCounter());
                    AutoNameGenerator.getInstance().setRouterInterfaceNameCounter(networkData.autoNameGeneratorDTO().routerInterfaceNameCounter());
                    AutoNameGenerator.getInstance().setPcNameCounter(networkData.autoNameGeneratorDTO().pcNameCounter());
                    simulationWorkspaceView.display();
                });
            }
        });

        menu.getChildren().addAll(newButton, loadButton);

        scene = new Scene(menu, 800, 600);
    }

    public void display() {
        stage.setTitle("SpudrNet6");
        stage.setScene(scene);
        stage.show();
    }

    public void insertDevices(List<NetworkDeviceViewDTO> devices, SimulationWorkspaceView simulationWorkspaceView, MasterController masterController) {
        simulationWorkspaceView.initializeView();

        NetworkDeviceView networkDeviceView;
        for (NetworkDeviceViewDTO deviceData : devices) {
            switch (deviceData.type()) {
                case ROUTER -> {
                    networkDeviceView = new RouterView(deviceData.uuid(), new Image("router_image.png"));
                }
                case SWITCH -> {
                    networkDeviceView = new SwitchView(deviceData.uuid(), new Image("switch_image.png"));
                }
                case PC -> {
                    networkDeviceView = new PCView(deviceData.uuid(), new Image("server_image.png"));
                }
                case null, default -> {
                    return;
                }
            }
            networkDeviceView.setLayoutX(deviceData.x());
            networkDeviceView.setLayoutY(deviceData.y());
            networkDeviceView.setName(deviceData.name());

            simulationWorkspaceView.setupPlacedDeviceEvents(networkDeviceView);

            simulationWorkspaceView.addNode(networkDeviceView);
            simulationWorkspaceView.addDeviceView(networkDeviceView);
            masterController.addDevice(networkDeviceView);
        }
    }

    public void insertConnections(NetworkData networkData, SimulationWorkspaceView simulationWorkspaceView, MasterController masterController) {
        for (ConnectionLineDTO connectionLine : networkData.connections()) {
            NetworkDeviceView startDevice = simulationWorkspaceView.getNetworkDeviceViews().stream()
                    .filter(device -> device.getUuid().equals(connectionLine.startDeviceId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Device with UUID " + connectionLine.startDeviceId() + " not found"));

            NetworkDeviceView endDevice = simulationWorkspaceView.getNetworkDeviceViews().stream()
                    .filter(device -> device.getUuid().equals(connectionLine.endDeviceId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Device with UUID " + connectionLine.startDeviceId() + " not found"));

            masterController.addConnection(startDevice, endDevice);
            Map<String, String> labels = masterController.setupInitialLabelsForConnection(startDevice, endDevice);
            simulationWorkspaceView.addConnectionLine(startDevice, endDevice, labels.get("Middle"), labels.get("Start"), labels.get("End"));
        }
    }
}
