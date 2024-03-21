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
    private NetworkDevice contextMenuNetworkDevice;
    private CursorFollowingNetworkDeviceHandler cursorFollowingDeviceHandler;
    private MasterController masterController;

    private boolean isConnectionMode = false;
    private NetworkDevice firstSelectedDevice = null;

    ToolBar toolBar;

    public SimulationWorkspaceView(Stage stage) {
        this.stage = stage;
        initializeView();
    }

    private void initializeView() {
        simulationWorkspace = new AnchorPane();
        toolBar = new ToolBar();

        Router routerView = new Router(new Image("router.png"));
        Switch switchView = new Switch(new Image("switch.png"));
        PC pcView = new PC(new Image("server.png"));

        Button routerToolBarButton = createNetworkDeviceButton(routerView);
        Button switchToolBarButton = createNetworkDeviceButton(switchView);
        Button pcToolBarButton = createNetworkDeviceButton(pcView);
        Button connectorToolBarButton = createConnectorButton(new ImageView(new Image("connector.png")));

        toolBar.getItems().addAll(routerToolBarButton, switchToolBarButton, pcToolBarButton, connectorToolBarButton);
        toolBar.toFront();

        simulationWorkspace.getChildren().add(toolBar);
        AnchorPane.setTopAnchor(toolBar, 0.0);

        netwrokDeviceContextMenu = new ContextMenu();
        generateNetworkDeviceContextMenu(netwrokDeviceContextMenu);

        cursorFollowingDeviceHandler = new CursorFollowingNetworkDeviceHandler();

        setupWorkspaceEvents();
        setupCursorFollowingDeviceEvents();

        scene = new Scene(simulationWorkspace, 800, 600);
    }

    private Button createNetworkDeviceButton(NetworkDevice networkDevice) {
        Button button = new Button(networkDevice.getNetworkDeviceType().toString());
        ImageView buttonIcon = new ImageView(networkDevice.getImage());
        buttonIcon.setFitHeight(40);
        buttonIcon.setFitWidth(40);
        buttonIcon.setPreserveRatio(true);
        button.setGraphic(buttonIcon);

        button.setOnAction(event -> {
            if (cursorFollowingDeviceHandler.isFollowing()) {
                simulationWorkspace.getChildren().remove(cursorFollowingDeviceHandler.get());
            }
            spawn(networkDevice);
        });

        return button;
    }

    private Button createConnectorButton(ImageView icon) {
        Button connectorButton = new Button("Connector");
        icon.setFitHeight(40);
        icon.setFitWidth(40);
        icon.setPreserveRatio(true);
        connectorButton.setGraphic(icon);

        connectorButton.setOnAction(clickEvent -> isConnectionMode = true);

        return connectorButton;
    }

    private void setupCursorFollowingDeviceEvents() {
        simulationWorkspace.setOnMouseMoved(mouseEvent -> {
            if (cursorFollowingDeviceHandler.isFollowing()) {
                if (!simulationWorkspace.getChildren().contains(cursorFollowingDeviceHandler.get())) {
                    simulationWorkspace.getChildren().add(cursorFollowingDeviceHandler.get());
                    cursorFollowingDeviceHandler.get().toBack();
                }
                cursorFollowingDeviceHandler.get().setLayoutX(mouseEvent.getSceneX());
                cursorFollowingDeviceHandler.get().setLayoutY(mouseEvent.getSceneY());
            }
        });
    }

    private void setupWorkspaceEvents() {
        simulationWorkspace.setOnMouseClicked(mouseEvent -> {
            if (cursorFollowingDeviceHandler.isFollowing()) {
                setupPlacedDeviceEvents(cursorFollowingDeviceHandler.get());
                cursorFollowingDeviceHandler.place();
            }
        });
    }

    private void setupPlacedDeviceEvents(NetworkDevice networkDevice) {
        final double[] cursorDistanceFromShapeTopLeft = new double[2];

        networkDevice.setOnMousePressed(clickEvent -> {
            if (isConnectionMode) {
                if (firstSelectedDevice == null) {
                    firstSelectedDevice = networkDevice;
                } else {
                    drawLine(firstSelectedDevice, networkDevice);
                    isConnectionMode = false;
                    firstSelectedDevice = null;
                }
                clickEvent.consume();
            }

            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                cursorDistanceFromShapeTopLeft[0] = networkDevice.getLayoutX() - clickEvent.getSceneX();
                cursorDistanceFromShapeTopLeft[1] = networkDevice.getLayoutY() - clickEvent.getSceneY();
            } else if (clickEvent.getButton() == MouseButton.SECONDARY) {
                contextMenuNetworkDevice = networkDevice;
                netwrokDeviceContextMenu.show(simulationWorkspace.getScene().getWindow(), clickEvent.getScreenX(), clickEvent.getScreenY());
            }
        });

        networkDevice.setOnMouseDragged(dragEvent -> {
            if (dragEvent.getButton() == MouseButton.PRIMARY) {
                double newX = dragEvent.getSceneX() + cursorDistanceFromShapeTopLeft[0];
                double newY = dragEvent.getSceneY() + cursorDistanceFromShapeTopLeft[1];

                networkDevice.setLayoutX(newX);
                networkDevice.setLayoutY(newY);

                for (ConnectionLine line : networkDevice.getConnections()) {
                    updateLinePosition(networkDevice, line);
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

    private void spawn(NetworkDevice networkDevice) {
        NetworkDevice deepCopy = networkDevice.deepCopy();
        deepCopy.setOpacity(0.5);
        deepCopy.setFitWidth(70);
        deepCopy.setFitHeight(70);
        cursorFollowingDeviceHandler.set(deepCopy);
    }

    private void drawLine(NetworkDevice startDevice, NetworkDevice endDevice) {
        double startX = startDevice.getLayoutX() + startDevice.getFitWidth() / 2;
        double startY = startDevice.getLayoutY() + startDevice.getFitHeight() / 2;
        double endX = endDevice.getLayoutX() + endDevice.getFitWidth() / 2;
        double endY = endDevice.getLayoutY() + endDevice.getFitHeight() / 2;

        ConnectionLine connectionLine = new ConnectionLine(startX, startY, endX, endY, startDevice, endDevice);
        simulationWorkspace.getChildren().add(connectionLine);
        connectionLine.toBack();

        startDevice.addConnection(connectionLine);
        endDevice.addConnection(connectionLine);
    }

    private void updateLinePosition(NetworkDevice networkDevice, ConnectionLine line) {

        if (networkDevice.equals(line.getStartDevice())) {
            line.setStartX(networkDevice.getLayoutX() + networkDevice.getFitWidth() / 2);
            line.setStartY(networkDevice.getLayoutY() + networkDevice.getFitHeight() / 2);
        }

        if (networkDevice.equals(line.getEndDevice())) {
            line.setEndX(networkDevice.getLayoutX() + networkDevice.getFitWidth() / 2);
            line.setEndY(networkDevice.getLayoutY() + networkDevice.getFitHeight() / 2);
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
