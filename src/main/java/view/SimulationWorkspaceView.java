package view;

import controller.MasterController;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SimulationWorkspaceView {
    private final Stage stage;
    private Scene scene;
    private AnchorPane simulationWorkspace;

    private ContextMenu netwrokDeviceContextMenu;
    private NetworkDeviceView contextMenuNetworkDevice;
    private CursorFollowingNetworkDeviceHandler cursorFollowingDeviceHandler;
    private MasterController masterController;

    private boolean isConnectionMode = false;
    private NetworkDeviceView firstSelectedDevice = null;

    private final int iconSize = 32;
    private final int imageSize = 70;

    private final RouterPropertiesMenu routerPropertiesMenu = new RouterPropertiesMenu();

    ToolBar toolBar;

    public SimulationWorkspaceView(Stage stage) {
        this.stage = stage;
        initializeView();
    }

    private void initializeView() {
        simulationWorkspace = new AnchorPane();
        toolBar = new ToolBar();

        RouterView routerView = new RouterView(new Image("router_image.png"));
        SwitchView switchView = new SwitchView(new Image("switch_image.png"));
        PCView pcView = new PCView(new Image("server_image.png"));

        Button routerToolBarButton = createNetworkDeviceButton(routerView, new Image("router_icon.png"));
        Button switchToolBarButton = createNetworkDeviceButton(switchView, new Image("switch_icon.png"));
        Button pcToolBarButton = createNetworkDeviceButton(pcView, new Image("server_icon.png"));
        Button connectorToolBarButton = createConnectorButton(new ImageView(new Image("connector_icon.png")));
        Button startSimulationToolBarButton = createStartSimulationButton(new ImageView(new Image("start_icon.png")));

        toolBar.getItems().addAll(routerToolBarButton, switchToolBarButton, pcToolBarButton, connectorToolBarButton, startSimulationToolBarButton);
        toolBar.toFront();

        simulationWorkspace.getChildren().add(toolBar);
        AnchorPane.setTopAnchor(toolBar, 0.0);

        netwrokDeviceContextMenu = new ContextMenu();
        generateNetworkDeviceContextMenu(netwrokDeviceContextMenu);

        cursorFollowingDeviceHandler = new CursorFollowingNetworkDeviceHandler();

        setupCursorFollowingDeviceEvents();

        scene = new Scene(simulationWorkspace, 800, 600);
    }

    private Button createNetworkDeviceButton(NetworkDeviceView networkDeviceView, Image icon) {
        Button button = new Button(networkDeviceView.getNetworkDeviceType().toString());
        ImageView buttonIcon = new ImageView(icon);
        button.setGraphic(buttonIcon);

        button.setOnAction(event -> {
            if (cursorFollowingDeviceHandler.isFollowing()) {
                simulationWorkspace.getChildren().remove(cursorFollowingDeviceHandler.get());
            }
            spawn(networkDeviceView);
        });

        return button;
    }

    private Button createConnectorButton(ImageView icon) {
        Button connectorButton = new Button("Connector");
        icon.setFitHeight(iconSize);
        icon.setFitWidth(iconSize);
        icon.setPreserveRatio(true);
        connectorButton.setGraphic(icon);

        connectorButton.setOnAction(clickEvent -> isConnectionMode = true);

        return connectorButton;
    }

    private Button createStartSimulationButton(ImageView icon) {
        Button startSimulationButton = new Button("Start");
        icon.setFitHeight(iconSize);
        icon.setFitWidth(iconSize);
        icon.setPreserveRatio(true);
        startSimulationButton.setGraphic(icon);

        startSimulationButton.setOnAction(clickEvent -> masterController.startSimulation());

        return startSimulationButton;
    }

    private void setupCursorFollowingDeviceEvents() {
        setupCursorFollowingDeviceMoveEvent();
        setupCursorFollowingDeviceClickEvent();

    }

    private void setupCursorFollowingDeviceMoveEvent() {
        simulationWorkspace.setOnMouseMoved(moveEvent -> {
            if (cursorFollowingDeviceHandler.isFollowing()) {
                if (!simulationWorkspace.getChildren().contains(cursorFollowingDeviceHandler.get())) {
                    simulationWorkspace.getChildren().add(cursorFollowingDeviceHandler.get());
                    cursorFollowingDeviceHandler.get().toBack();
                }
                cursorFollowingDeviceHandler.get().setLayoutX(moveEvent.getSceneX());
                cursorFollowingDeviceHandler.get().setLayoutY(moveEvent.getSceneY());
            }
        });
    }

    private void setupCursorFollowingDeviceClickEvent() {
        simulationWorkspace.setOnMouseClicked(clickEvent -> {
            if (cursorFollowingDeviceHandler.isFollowing()) {
                setupPlacedDeviceEvents(cursorFollowingDeviceHandler.get());
                masterController.addDevice(cursorFollowingDeviceHandler.get());
                cursorFollowingDeviceHandler.place();
            }
        });
    }

    private void setupPlacedDeviceEvents(NetworkDeviceView networkDeviceView) {
        final double[] cursorDistanceFromShapeTopLeft = new double[2];
        setupPlacedDeviceClickEvent(networkDeviceView, cursorDistanceFromShapeTopLeft);
        setupPlacedDeviceDragEvent(networkDeviceView, cursorDistanceFromShapeTopLeft);
    }

    private void setupPlacedDeviceClickEvent(NetworkDeviceView networkDeviceView, double[] cursorDistanceFromShapeTopLeft) {
        networkDeviceView.setOnMousePressed(clickEvent -> {
            if (isConnectionMode) {
                if (firstSelectedDevice == null) {
                    firstSelectedDevice = networkDeviceView;
                } else {
                    addConnectionLine(firstSelectedDevice, networkDeviceView);
                    if (!masterController.addConnection(firstSelectedDevice, networkDeviceView)) {
                        System.out.println("unable to propagate network device connection to model");
                    }
                    isConnectionMode = false;
                    firstSelectedDevice = null;
                }
                clickEvent.consume();
            }

            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                cursorDistanceFromShapeTopLeft[0] = networkDeviceView.getLayoutX() - clickEvent.getSceneX();
                cursorDistanceFromShapeTopLeft[1] = networkDeviceView.getLayoutY() - clickEvent.getSceneY();
            } else if (clickEvent.getButton() == MouseButton.SECONDARY) {
                contextMenuNetworkDevice = networkDeviceView;
                netwrokDeviceContextMenu.show(simulationWorkspace.getScene().getWindow(), clickEvent.getScreenX(), clickEvent.getScreenY());
            }
        });
    }

    private void setupPlacedDeviceDragEvent(NetworkDeviceView networkDeviceView, double[] cursorDistanceFromShapeTopLeft) {
        networkDeviceView.setOnMouseDragged(dragEvent -> {
            if (dragEvent.getButton() == MouseButton.PRIMARY) {
                double newX = dragEvent.getSceneX() + cursorDistanceFromShapeTopLeft[0];
                double newY = dragEvent.getSceneY() + cursorDistanceFromShapeTopLeft[1];

                networkDeviceView.setLayoutX(newX);
                networkDeviceView.setLayoutY(newY);

                for (ConnectionLine line : networkDeviceView.getConnections()) {
                    updateLinePosition(networkDeviceView, line);
                }
            }
        });
    }

    private void generateNetworkDeviceContextMenu(ContextMenu contextMenu) {
        MenuItem propertiesOption = new MenuItem("Properties");

        MenuItem deleteOption = new MenuItem("Delete");
        deleteOption.setOnAction(event -> {
            if (contextMenuNetworkDevice != null) {
                simulationWorkspace.getChildren().remove(contextMenuNetworkDevice);
                contextMenuNetworkDevice = null;
            }
        });
        contextMenu.getItems().addAll(propertiesOption, deleteOption);
    }

    private void spawn(NetworkDeviceView networkDeviceView) {
        NetworkDeviceView deepCopy = networkDeviceView.deepCopy();
        deepCopy.setOpacity(0.5);
        deepCopy.setFitWidth(imageSize);
        deepCopy.setFitHeight(imageSize);
        cursorFollowingDeviceHandler.set(deepCopy);
    }

    private void addConnectionLine(NetworkDeviceView startDeviceView, NetworkDeviceView endDeviceView) {
        double startX = startDeviceView.getLayoutX() + startDeviceView.getFitWidth() / 2;
        double startY = startDeviceView.getLayoutY() + startDeviceView.getFitHeight() / 2;
        double endX = endDeviceView.getLayoutX() + endDeviceView.getFitWidth() / 2;
        double endY = endDeviceView.getLayoutY() + endDeviceView.getFitHeight() / 2;

        ConnectionLine connectionLine = new ConnectionLine(startX, startY, endX, endY, startDeviceView, endDeviceView);
        simulationWorkspace.getChildren().add(connectionLine);
        connectionLine.toBack();

        startDeviceView.addConnectionLine(connectionLine);
        endDeviceView.addConnectionLine(connectionLine);
    }

    private void updateLinePosition(NetworkDeviceView networkDeviceView, ConnectionLine line) {

        if (networkDeviceView.equals(line.getStartDevice())) {
            line.setStartX(networkDeviceView.getLayoutX() + networkDeviceView.getFitWidth() / 2);
            line.setStartY(networkDeviceView.getLayoutY() + networkDeviceView.getFitHeight() / 2);
        }

        if (networkDeviceView.equals(line.getEndDevice())) {
            line.setEndX(networkDeviceView.getLayoutX() + networkDeviceView.getFitWidth() / 2);
            line.setEndY(networkDeviceView.getLayoutY() + networkDeviceView.getFitHeight() / 2);
        }
    }

    public void display() {
        stage.setScene(scene);
        stage.show();
    }

    public void setController(MasterController masterController) {
        this.masterController = masterController;
    }
}
