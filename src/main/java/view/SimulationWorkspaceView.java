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
    private FollowingNetworkDeviceController followingNetworkDeviceController;
    private MasterController masterController;

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

        followingNetworkDeviceController = new FollowingNetworkDeviceController();

        setupWorkspaceEvents();
        setupCurrentlyPlacedNetworkDeviceEvents();

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
            if (followingNetworkDeviceController.isFollowing()) {
                simulationWorkspace.getChildren().remove(followingNetworkDeviceController.get());
            }
            spawn(networkDevice);
        });

        return button;
    }

    private Button createConnectorButton(ImageView icon) {
        Button button = new Button("Connector");
        icon.setFitHeight(40);
        icon.setFitWidth(40);
        icon.setPreserveRatio(true);
        button.setGraphic(icon);

        return button;
    }

    private void setupCurrentlyPlacedNetworkDeviceEvents() {
        simulationWorkspace.setOnMouseMoved(mouseEvent -> {
            if (followingNetworkDeviceController.isFollowing()) {
                if (!simulationWorkspace.getChildren().contains(followingNetworkDeviceController.get())) {
                    simulationWorkspace.getChildren().add(followingNetworkDeviceController.get());
                    followingNetworkDeviceController.get().toBack();
                }
                followingNetworkDeviceController.get().setLayoutX(mouseEvent.getSceneX());
                followingNetworkDeviceController.get().setLayoutY(mouseEvent.getSceneY());
            }
        });
    }

    private void setupWorkspaceEvents() {
        simulationWorkspace.setOnMouseClicked(mouseEvent -> {
            if (followingNetworkDeviceController.isFollowing()) {
                makeInteractive(followingNetworkDeviceController.get());
                followingNetworkDeviceController.place();
            }
        });
    }

    private void makeInteractive(NetworkDevice networkDevice) {
        final double[] cursorDistanceFromShapeTopLeft = new double[2];

        networkDevice.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {

                cursorDistanceFromShapeTopLeft[0] = networkDevice.getLayoutX() - mouseEvent.getSceneX();
                cursorDistanceFromShapeTopLeft[1] = networkDevice.getLayoutY() - mouseEvent.getSceneY();
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                contextMenuNetworkDevice = networkDevice;
                netwrokDeviceContextMenu.show(simulationWorkspace.getScene().getWindow(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
        });

        networkDevice.setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                networkDevice.setLayoutX(mouseEvent.getSceneX() + cursorDistanceFromShapeTopLeft[0]);
                networkDevice.setLayoutY(mouseEvent.getSceneY() + cursorDistanceFromShapeTopLeft[1]);
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
        followingNetworkDeviceController.set(deepCopy);
    }

    public void display() {
        stage.setScene(scene);
        stage.show();
    }

    public void setController(MasterController masterController) {
        this.masterController = masterController;
    }
}
