package view;

import common.AutoNameGenerator;
import common.NetworkDevice;
import common.NetworkDeviceType;
import controller.MasterController;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the main workspace of a network simulation environment.
 * This class manages the graphical user interface components and interactions for simulating network devices and connections.
 */
public class SimulationWorkspaceView {
    private final Stage stage;
    private Scene scene;
    private AnchorPane simulationWorkspace;
    private CursorFollowingNetworkDeviceHandler cursorFollowingDeviceHandler;
    private MasterController masterController;
    private boolean isConnectionMode = false;
    private NetworkDeviceView firstSelectedDevice = null;
    private LogArea logArea;
    ArrayList<ConnectionLine> connectionLines = new ArrayList<>();
    ArrayList<NetworkDeviceView> networkDeviceViews = new ArrayList<>();

    private final int iconSize = 32;
    private final int imageSize = 70;

    private final Tooltip labelsTooltip = new Tooltip();

    ToolBar toolBar;


    /**
     * Constructs a SimulationWorkspaceView with a reference to the primary stage.
     *
     * @param stage the primary stage for this view
     */
    public SimulationWorkspaceView(Stage stage) {
        this.stage = stage;
        stage.setMinWidth(1200);
        stage.setMinHeight(1000);
        initializeView();
    }

    /**
     * Retrieves a connection line between two network devices if it exists.
     *
     * @param first  the first network device
     * @param second the second network device
     * @return the connection line if found, otherwise null
     */
    public synchronized ConnectionLine getConnectionLine(NetworkDevice first, NetworkDevice second) {
        for (ConnectionLine connectionLine : connectionLines) {
            if ((connectionLine.getStartDevice().getUuid() == first.getUuid() && connectionLine.getEndDevice().getUuid() == second.getUuid())
                    || connectionLine.getStartDevice().getUuid() == second.getUuid() && connectionLine.getEndDevice().getUuid() == first.getUuid()) {
                return connectionLine;
            }
        }
        return null;
    }

    public ArrayList<ConnectionLine> getConnectionLines() {
        return connectionLines;
    }

    public ArrayList<NetworkDeviceView> getNetworkDeviceViews() {
        return networkDeviceViews;
    }

    /**
     * Initializes the view and its components.
     */
    public void initializeView() {
        simulationWorkspace = new AnchorPane();
        toolBar = new ToolBar();

        RouterView routerView = new RouterView(UUID.randomUUID(), new Image("router_image.png"));
        SwitchView switchView = new SwitchView(UUID.randomUUID(), new Image("switch_image.png"));
        PCView pcView = new PCView(UUID.randomUUID(), new Image("server_image.png"));

        Button routerToolBarButton = createNetworkDeviceButton(routerView, new Image("router_icon.png"));
        Button switchToolBarButton = createNetworkDeviceButton(switchView, new Image("switch_icon.png"));
        Button pcToolBarButton = createNetworkDeviceButton(pcView, new Image("server_icon.png"));
        Button connectorToolBarButton = createConnectorButton(new ImageView(new Image("connector_icon.png")));
        Button startSimulationToolBarButton = createStartSimulationButton(new ImageView(new Image("start_icon.png")));
        Button pauseSimulationToolBarButton = createPauseSimulationButton(new ImageView(new Image("pause_icon.png")));

        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Options", networkDeviceViews, connectionLines);
        menuBar.getMenus().add(menu);
        AnchorPane.setTopAnchor(menuBar, 0.0);
        AnchorPane.setLeftAnchor(menuBar, 0.0);
        AnchorPane.setRightAnchor(menuBar, 0.0);
        addNode(menuBar);

        toolBar.getItems().addAll(routerToolBarButton, switchToolBarButton, pcToolBarButton, connectorToolBarButton, startSimulationToolBarButton, pauseSimulationToolBarButton);
        toolBar.toFront();
        addNode(toolBar);
        AnchorPane.setTopAnchor(toolBar, 30.0);

        logArea = new LogArea(450, 250);
        AnchorPane.setTopAnchor(logArea, 35.0);
        AnchorPane.setRightAnchor(logArea, 10.0);
        logArea.setAlignment(Pos.TOP_RIGHT);
        addNode(logArea);

        cursorFollowingDeviceHandler = new CursorFollowingNetworkDeviceHandler();

        setupCursorFollowingDeviceEvents();

        initializeTooltip();

        scene = new Scene(simulationWorkspace, 1200, 1000);
    }

    /**
     * Creates a button for adding network devices to the simulation.
     *
     * @param networkDeviceView the view representation of the network device
     * @param icon              the icon to display on the button
     * @return a configured button for network device creation
     */
    private Button createNetworkDeviceButton(NetworkDeviceView networkDeviceView, Image icon) {
        Button button = new Button(networkDeviceView.getNetworkDeviceType().toString());
        ImageView buttonIcon = new ImageView(icon);
        button.setGraphic(buttonIcon);

        button.setOnAction(buttonClickEvent -> {
            if (!masterController.simulationPaused()) {
                printToLogWindow("Pause simulation before making adjustments\n", Color.RED);
                return;
            }
            if (cursorFollowingDeviceHandler.isFollowing()) {
                return;
            }
            isConnectionMode = false;
            spawn(networkDeviceView);
        });

        return button;
    }

    /**
     * Creates a button for enabling connection mode in the simulation.
     *
     * @param icon the icon to display on the button
     * @return a configured button for enabling connection mode
     */
    private Button createConnectorButton(ImageView icon) {
        Button connectorButton = new Button("Connector");
        icon.setFitHeight(iconSize);
        icon.setFitWidth(iconSize);
        icon.setPreserveRatio(true);
        connectorButton.setGraphic(icon);

        connectorButton.setOnAction(clickEvent -> {
            if (!masterController.simulationPaused()) {
                printToLogWindow("Pause simulation before making adjustments\n", Color.RED);
                return;
            }
            isConnectionMode = true;
        });

        return connectorButton;
    }

    /**
     * Creates a button to start or resume the simulation.
     *
     * @param icon the icon to display on the button
     * @return a configured button to start or resume the simulation
     */
    private Button createStartSimulationButton(ImageView icon) {
        Button startSimulationButton = new Button("Start");
        icon.setFitHeight(iconSize);
        icon.setFitWidth(iconSize);
        icon.setPreserveRatio(true);
        startSimulationButton.setGraphic(icon);

        startSimulationButton.setOnAction(clickEvent -> {
            if (!masterController.simulationStarted()) {
                masterController.startSimulation();
                return;
            }
            masterController.resumeSimulation();
        });

        return startSimulationButton;
    }

    /**
     * Creates a button to pause the simulation.
     *
     * @param icon the icon to display on the button
     * @return a configured button to pause the simulation
     */
    private Button createPauseSimulationButton(ImageView icon) {
        Button startSimulationButton = new Button("Pause");
        icon.setFitHeight(iconSize);
        icon.setFitWidth(iconSize);
        icon.setPreserveRatio(true);
        startSimulationButton.setGraphic(icon);

        startSimulationButton.setOnAction(clickEvent -> {
            if (masterController.simulationPaused()) {
                return;
            }
            masterController.pauseSimulation();
        });

        return startSimulationButton;
    }

    /**
     * Sets up the events related to the cursor following the device.
     */
    private void setupCursorFollowingDeviceEvents() {
        setupCursorFollowingDeviceMoveEvent();
        setupCursorFollowingDeviceClickEvent();
    }

    /**
     * Configures how a device follows the cursor during a drag on the simulation workspace.
     */
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

    /**
     * Configures the behavior when a cursor-following device placed on the simulation workspace (simulation workspace is clicked when device is following mouse).
     */
    private void setupCursorFollowingDeviceClickEvent() {
        simulationWorkspace.setOnMouseClicked(clickEvent -> {
            if (cursorFollowingDeviceHandler.isFollowing()) {
                setupPlacedDeviceEvents(cursorFollowingDeviceHandler.get());
                masterController.addDevice(cursorFollowingDeviceHandler.get());
                addDeviceView(cursorFollowingDeviceHandler.get());
                cursorFollowingDeviceHandler.place();
            }
        });
    }

    /**
     * Sets up event handling for network devices that have been placed on the simulation workspace.
     * Includes setting up events for clicking, dragging, and hovering over the device.
     *
     * @param networkDeviceView the view representation of the network device
     */
    public void setupPlacedDeviceEvents(NetworkDeviceView networkDeviceView) {
        final double[] cursorDistanceFromShapeTopLeft = new double[2];
        setupPlacedDeviceClickEvent(networkDeviceView, cursorDistanceFromShapeTopLeft);
        setupPlacedDeviceDragEvent(networkDeviceView, cursorDistanceFromShapeTopLeft);
        setupPlacedDeviceHoverEvent(networkDeviceView);
    }

    /**
     * Configures the hover event for a placed network device, showing a tooltip with device information.
     *
     * @param networkDeviceView the network device view to configure
     */
    private void setupPlacedDeviceHoverEvent(NetworkDeviceView networkDeviceView) {
        networkDeviceView.setOnMouseEntered(hoverEnterEvent -> {
            if (networkDeviceView.getNetworkDeviceType() == NetworkDeviceType.ROUTER) {

                String string = "Device type: " + networkDeviceView.getNetworkDeviceType().toString() + "\n" +
                        "Routing table: " + "\n" + masterController.getDeviceConfigurations(networkDeviceView);
                updateTooltipContent(string);

                Point2D p = networkDeviceView.localToScreen(networkDeviceView.getLayoutBounds().getMaxX(), networkDeviceView.getLayoutBounds().getMaxY());
                labelsTooltip.show(networkDeviceView, p.getX(), p.getY());
            }
        });

        networkDeviceView.setOnMouseExited(hoverExitedEvent -> {
            labelsTooltip.hide();
        });
    }

    /**
     * Configures the click event for a network device, potentially starting or ending a connection or moving the device.
     *
     * @param networkDeviceView              the network device view to configure
     * @param cursorDistanceFromShapeTopLeft an array to store cursor distance from the top-left of the device
     */
    private void setupPlacedDeviceClickEvent(NetworkDeviceView networkDeviceView, double[] cursorDistanceFromShapeTopLeft) {
        networkDeviceView.setOnMousePressed(clickEvent -> {
            if (isConnectionMode) {
                if (firstSelectedDevice == null) {
                    firstSelectedDevice = networkDeviceView;
                } else {
                    if (!masterController.addConnection(firstSelectedDevice, networkDeviceView)) {
                        System.out.println("Unable to connect devices");
                    } else {
                        Map<String, String> labels = masterController.setupInitialLabelsForConnection(firstSelectedDevice, networkDeviceView);
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

    /**
     * Configures the drag event for a network device to move it around the simulation workspace.
     *
     * @param networkDeviceView              the network device view to configure
     * @param cursorDistanceFromShapeTopLeft an array holding the cursor distance from the device's top-left corner
     */
    private void setupPlacedDeviceDragEvent(NetworkDeviceView networkDeviceView, double[] cursorDistanceFromShapeTopLeft) {
        networkDeviceView.setOnMouseDragged(dragEvent -> {
            if (!masterController.simulationPaused()) {
                printToLogWindow("Pause simulation before making adjustments\n", Color.RED);
                return;
            }
            labelsTooltip.hide();
            if (dragEvent.getButton() == MouseButton.PRIMARY) {
                double newX = dragEvent.getSceneX() + cursorDistanceFromShapeTopLeft[0];
                double newY = dragEvent.getSceneY() + cursorDistanceFromShapeTopLeft[1];

                networkDeviceView.setLayoutX(newX);
                networkDeviceView.setLayoutY(newY);

                for (ConnectionLine line : networkDeviceView.getConnections()) {
                    updateLabelPositions(line);
                }
            }
        });
    }

    /**
     * Handles spawning a device for cursor following.
     * Creates a deep copy of a network device view, sets it for cursor following, and updates the interface.
     *
     * @param networkDeviceView the network device view to spawn
     */
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

    /**
     * Adds a connection line between two network devices and updates the graphical representation.
     *
     * @param startDeviceView the starting device view
     * @param endDeviceView   the ending device view
     * @param middleLabel     the label in the middle of the connection line
     * @param startLabel      the label near the start of the connection line
     * @param endLabel        the label near the end of the connection line
     */
    public void addConnectionLine(NetworkDeviceView startDeviceView, NetworkDeviceView endDeviceView, String middleLabel, String startLabel, String endLabel) {
        double startX = startDeviceView.getLayoutX() + startDeviceView.getWidth() / 2;
        double startY = startDeviceView.getLayoutY() + startDeviceView.getHeight() / 2;
        double endX = endDeviceView.getLayoutX() + endDeviceView.getWidth() / 2;
        double endY = endDeviceView.getLayoutY() + endDeviceView.getHeight() / 2;

        ConnectionLine connectionLine = new ConnectionLine(startX, startY, endX, endY, startDeviceView, endDeviceView, middleLabel, startLabel, endLabel);

        connectionLine.startXProperty().bind(startDeviceView.layoutXProperty().add(startDeviceView.widthProperty().divide(2)));
        connectionLine.startYProperty().bind(startDeviceView.layoutYProperty().add(startDeviceView.heightProperty().divide(2)));
        connectionLine.endXProperty().bind(endDeviceView.layoutXProperty().add(endDeviceView.widthProperty().divide(2)));
        connectionLine.endYProperty().bind(endDeviceView.layoutYProperty().add(endDeviceView.heightProperty().divide(2)));


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

    public void addDeviceView(NetworkDeviceView networkDeviceView) {
        networkDeviceViews.add(networkDeviceView);
    }

    /**
     * Updates the position of a connection line based on the movement of network devices.
     * @param connectionLine    the connection line to be updated
     */
    private void updateLabelPositions(ConnectionLine connectionLine) {
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

    /**
     * Initializes tooltips for displaying information.
     */
    private void initializeTooltip() {
        labelsTooltip.setAutoHide(true);
        labelsTooltip.setStyle("-fx-font-family: 'monospace';");
    }

    /**
     * Updates the content of tooltips used in the simulation.
     *
     * @param content the new content to be displayed in the tooltip
     */
    private void updateTooltipContent(String content) {
        labelsTooltip.setText(content);
    }

    /**
     * Sets the current scene to the stage and shows it.
     */
    public void display() {
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Adds a graphical node to the simulation workspace.
     *
     * @param node the node to be added
     */
    public void addNode(Node node) {
        simulationWorkspace.getChildren().add(node);
    }

    /**
     * Removes a graphical node from the simulation workspace.
     *
     * @param node the node to be removed
     */
    public synchronized void removeNode(Node node) {
        simulationWorkspace.getChildren().remove(node);
    }

    /**
     * Sets the controller that manages the logic behind the simulation.
     *
     * @param masterController the controller to set
     */
    public void setController(MasterController masterController) {
        this.masterController = masterController;
    }

    public void printToLogWindow(String string, Color color) {
        Text text = new Text(string);
        text.setFill(color);
        Platform.runLater(() -> {
            logArea.print(text);
        });
    }
}

