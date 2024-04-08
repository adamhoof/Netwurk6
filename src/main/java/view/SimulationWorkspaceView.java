package view;

import common.AutoNameGenerator;
import common.NetworkDevice;
import controller.MasterController;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Map;

public class SimulationWorkspaceView {
    private final Stage stage;
    private Scene scene;
    private AnchorPane simulationWorkspace;
    private CursorFollowingNetworkDeviceHandler cursorFollowingDeviceHandler;
    private MasterController masterController;
    private boolean isConnectionMode = false;
    private NetworkDeviceView firstSelectedDevice = null;

    private final int iconSize = 32;
    private final int imageSize = 70;

    private final Tooltip labelsTooltip = new Tooltip();

    ToolBar toolBar;

    ArrayList<ConnectionLine> connectionLines = new ArrayList<>();

    public SimulationWorkspaceView(Stage stage) {
        this.stage = stage;
        initializeView();
    }

    public synchronized ConnectionLine getConnectionLine(NetworkDevice first, NetworkDevice second) {
        for (ConnectionLine connectionLine : connectionLines) {
            if ((connectionLine.getStartDevice().getUuid() == first.getUuid() && connectionLine.getEndDevice().getUuid() == second.getUuid())
                    || connectionLine.getStartDevice().getUuid() == second.getUuid() && connectionLine.getEndDevice().getUuid() == first.getUuid()) {
                return connectionLine;
            }
        }
        return null;
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

        cursorFollowingDeviceHandler = new CursorFollowingNetworkDeviceHandler();

        setupCursorFollowingDeviceEvents();

        initializeTooltip();

        scene = new Scene(simulationWorkspace, 800, 600);
    }

    private Button createNetworkDeviceButton(NetworkDeviceView networkDeviceView, Image icon) {
        Button button = new Button(networkDeviceView.getNetworkDeviceType().toString());
        ImageView buttonIcon = new ImageView(icon);
        button.setGraphic(buttonIcon);

        button.setOnAction(buttonClickEvent -> {
            if (cursorFollowingDeviceHandler.isFollowing()) {
                removeNode(cursorFollowingDeviceHandler.get());
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
        setupPlacedDeviceHoverEvent(networkDeviceView);
    }

    private void setupPlacedDeviceHoverEvent(NetworkDeviceView networkDeviceView) {
        networkDeviceView.setOnMouseEntered(hoverEnterEvent -> {
            String string = "Device type: " + networkDeviceView.getNetworkDeviceType().toString() + "\n" +
                    "Routing table: " + "\n" + masterController.getDeviceConfigurations(networkDeviceView);
            updateTooltipContent(string);

            Point2D p = networkDeviceView.localToScreen(networkDeviceView.getLayoutBounds().getMaxX(), networkDeviceView.getLayoutBounds().getMaxY());
            labelsTooltip.show(networkDeviceView, p.getX(), p.getY());
        });

        networkDeviceView.setOnMouseExited(hoverExitedEvent -> {
            labelsTooltip.hide();
        });
    }

    private void setupPlacedDeviceClickEvent(NetworkDeviceView networkDeviceView, double[] cursorDistanceFromShapeTopLeft) {
        networkDeviceView.setOnMousePressed(clickEvent -> {
            if (isConnectionMode) {
                if (firstSelectedDevice == null) {
                    firstSelectedDevice = networkDeviceView;
                } else {
                    if (!masterController.addConnection(firstSelectedDevice, networkDeviceView)) {
                        System.out.println("Unable to connect devices");
                    } else {
                        Map<String, String> labels = masterController.getLabelsForConnection(firstSelectedDevice, networkDeviceView);
                        addConnectionLine(firstSelectedDevice, networkDeviceView, labels.get("Middle"), labels.get("Start"), labels.get("End"));
                    }
                    isConnectionMode = false;
                    firstSelectedDevice = null;
                }
                clickEvent.consume();
            }

            if (clickEvent.getButton() == MouseButton.PRIMARY) {
                cursorDistanceFromShapeTopLeft[0] = networkDeviceView.getLayoutX() - clickEvent.getSceneX();
                cursorDistanceFromShapeTopLeft[1] = networkDeviceView.getLayoutY() - clickEvent.getSceneY();
            }
        });
    }

    private void setupPlacedDeviceDragEvent(NetworkDeviceView networkDeviceView, double[] cursorDistanceFromShapeTopLeft) {
        networkDeviceView.setOnMouseDragged(dragEvent -> {
            labelsTooltip.hide();
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

    private void spawn(NetworkDeviceView networkDeviceView) {
        NetworkDeviceView deepCopy = networkDeviceView.deepCopy();
        deepCopy.setOpacity(0.5);
        deepCopy.setImageViewFitWidth(imageSize);
        deepCopy.setImageViewFitHeight(imageSize);
        switch (networkDeviceView.getNetworkDeviceType()) {
            case ROUTER -> deepCopy.setName(AutoNameGenerator.generateRouterName());
            case SWITCH -> deepCopy.setName(AutoNameGenerator.generateSwitchName());
            case PC -> deepCopy.setName(AutoNameGenerator.generatePcName());
        }
        cursorFollowingDeviceHandler.set(deepCopy);
    }

    private void addConnectionLine(NetworkDeviceView startDeviceView, NetworkDeviceView endDeviceView, String middleLabel, String startLabel, String endLabel) {
        double startX = startDeviceView.getLayoutX() + startDeviceView.getWidth() / 2;
        double startY = startDeviceView.getLayoutY() + startDeviceView.getHeight() / 2;
        double endX = endDeviceView.getLayoutX() + endDeviceView.getWidth() / 2;
        double endY = endDeviceView.getLayoutY() + endDeviceView.getHeight() / 2;

        ConnectionLine connectionLine = new ConnectionLine(startX, startY, endX, endY, startDeviceView, endDeviceView, middleLabel, startLabel, endLabel);

        double centerX = (connectionLine.getStartX() + connectionLine.getEndX()) / 2;
        double centerY = (connectionLine.getStartY() + connectionLine.getEndY()) / 2;

        double thirdXFromStart = startX + (endX - startX) / 5;
        double thirdYFromStart = startY + (endY - startY) / 5;
        double thirdXFromEnd = endX - (endX - startX) / 5;
        double thirdYFromEnd = endY - (endY - startY) / 5;

        connectionLine.updateLabelPosition(connectionLine.getMiddleLabel(), centerX, centerY);
        connectionLine.updateLabelPosition(connectionLine.getStartLabel(), thirdXFromStart, thirdYFromStart);
        connectionLine.updateLabelPosition(connectionLine.getEndLabel(), thirdXFromEnd, thirdYFromEnd);
        simulationWorkspace.getChildren().addAll(connectionLine, connectionLine.getMiddleLabel(), connectionLine.getStartLabel(), connectionLine.getEndLabel());
        connectionLine.toBack();

        startDeviceView.addConnectionLine(connectionLine);
        endDeviceView.addConnectionLine(connectionLine);
        connectionLines.add(connectionLine);
    }

    private void updateLinePosition(NetworkDeviceView networkDeviceView, ConnectionLine connectionLine) {

        if (networkDeviceView.equals(connectionLine.getStartDevice())) {
            connectionLine.setStartX(networkDeviceView.getLayoutX() + networkDeviceView.getWidth() / 2);
            connectionLine.setStartY(networkDeviceView.getLayoutY() + networkDeviceView.getHeight() / 2);
        }

        if (networkDeviceView.equals(connectionLine.getEndDevice())) {
            connectionLine.setEndX(networkDeviceView.getLayoutX() + networkDeviceView.getWidth() / 2);
            connectionLine.setEndY(networkDeviceView.getLayoutY() + networkDeviceView.getHeight() / 2);
        }

        double centerX = (connectionLine.getStartX() + connectionLine.getEndX()) / 2;
        double centerY = (connectionLine.getStartY() + connectionLine.getEndY()) / 2;

        double startX = connectionLine.getStartDevice().getLayoutX() + connectionLine.getStartDevice().getWidth() / 2;
        double startY = connectionLine.getStartDevice().getLayoutY() + connectionLine.getStartDevice().getHeight() / 2;
        double endX = connectionLine.getEndDevice().getLayoutX() + connectionLine.getEndDevice().getWidth() / 2;
        double endY = connectionLine.getEndDevice().getLayoutY() + connectionLine.getEndDevice().getHeight() / 2;

        double thirdXFromStart = startX + (endX - startX) / 5;
        double thirdYFromStart = startY + (endY - startY) / 5;
        double thirdXFromEnd = endX - (endX - startX) / 5;
        double thirdYFromEnd = endY - (endY - startY) / 5;

        connectionLine.updateLabelPosition(connectionLine.getMiddleLabel(), centerX, centerY);
        connectionLine.updateLabelPosition(connectionLine.getStartLabel(), thirdXFromStart, thirdYFromStart);
        connectionLine.updateLabelPosition(connectionLine.getEndLabel(), thirdXFromEnd, thirdYFromEnd);
    }

    private void initializeTooltip() {
        labelsTooltip.setAutoHide(true);
        labelsTooltip.setStyle("-fx-font-family: 'monospace';");
    }

    private void updateTooltipContent(String content) {
        labelsTooltip.setText(content);
    }

    public void display() {
        stage.setScene(scene);
        stage.show();
    }

    public void addNode(Node node) {
        simulationWorkspace.getChildren().add(node);
    }

    public synchronized void removeNode(Node node) {
        simulationWorkspace.getChildren().remove(node);
    }

    public void setController(MasterController masterController) {
        this.masterController = masterController;
    }
}
