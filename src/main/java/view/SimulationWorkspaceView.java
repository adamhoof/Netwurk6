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
    private MasterController masterController;

    ToolBar toolBar;

    private NetworkDevice cursorFollowingNetworkDevice = null;

    public SimulationWorkspaceView(Stage stage) {
        this.stage = stage;
        initializeView();
    }

    private void initializeView() {
        simulationWorkspace = new AnchorPane();
        toolBar = new ToolBar();

        Router routerView = new Router(new ImageView(new Image("router.png")));
        Switch switchView = new Switch(new ImageView(new Image("switch.png")));
        PC pcView = new PC(new ImageView(new Image("server.png")));

        Button routerToolBarButton = createNetworkDeviceButton(routerView);
        Button switchToolBarButton = createNetworkDeviceButton(switchView);
        Button pcToolBarButton = createNetworkDeviceButton(pcView);

        toolBar.getItems().addAll(routerToolBarButton, switchToolBarButton, pcToolBarButton);
        toolBar.toFront();
        simulationWorkspace.getChildren().add(toolBar);
        AnchorPane.setTopAnchor(toolBar, 0.0);

        netwrokDeviceContextMenu = new ContextMenu();
        generateNetworkDeviceContextMenu(netwrokDeviceContextMenu);

        setupWorkspaceEvents();
        setupCurrentlyPlacedNetworkDeviceEvents();

        scene = new Scene(simulationWorkspace, 800, 600);
    }

    private Button createNetworkDeviceButton(NetworkDevice networkDevice) {
        Button button = new Button(networkDevice.getNetworkDeviceType().toString());
        ImageView buttonIcon = new ImageView(networkDevice.getImageView().getImage());
        buttonIcon.setFitHeight(50);
        buttonIcon.setFitWidth(50);
        buttonIcon.setPreserveRatio(true);
        button.setGraphic(buttonIcon);

        button.setOnAction(event -> {
            if (cursorFollowingNetworkDevice != null) {
                simulationWorkspace.getChildren().remove(cursorFollowingNetworkDevice.getImageView());
            }
            spawn(networkDevice);
        });

        return button;
    }

    private void setupCurrentlyPlacedNetworkDeviceEvents() {
        simulationWorkspace.setOnMouseMoved(mouseEvent -> {
            if (cursorFollowingNetworkDevice != null) {
                if (!simulationWorkspace.getChildren().contains(cursorFollowingNetworkDevice.getImageView())) {
                    simulationWorkspace.getChildren().add(cursorFollowingNetworkDevice.getImageView());
                    cursorFollowingNetworkDevice.getImageView().toBack();
                }
                cursorFollowingNetworkDevice.getImageView().setLayoutX(mouseEvent.getSceneX());
                cursorFollowingNetworkDevice.getImageView().setLayoutY(mouseEvent.getSceneY());
            }
        });
    }

    private void setupWorkspaceEvents() {
        simulationWorkspace.setOnMouseClicked(mouseEvent -> {
            if (networkDeviceFollowingCursor()) {
                makeInteractive(cursorFollowingNetworkDevice);
                placeFollowingNetworkDevice();
            }
        });
    }

    private void makeInteractive(NetworkDevice networkDevice) {
        final double[] cursorDistanceFromShapeTopLeft = new double[2];

        networkDevice.getImageView().setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {

                cursorDistanceFromShapeTopLeft[0] = networkDevice.getImageView().getLayoutX() - mouseEvent.getSceneX();
                cursorDistanceFromShapeTopLeft[1] = networkDevice.getImageView().getLayoutY() - mouseEvent.getSceneY();
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                contextMenuNetworkDevice = networkDevice;
                netwrokDeviceContextMenu.show(simulationWorkspace.getScene().getWindow(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
        });

        networkDevice.getImageView().setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                networkDevice.getImageView().setLayoutX(mouseEvent.getSceneX() + cursorDistanceFromShapeTopLeft[0]);
                networkDevice.getImageView().setLayoutY(mouseEvent.getSceneY() + cursorDistanceFromShapeTopLeft[1]);
            }
        });
    }

    private void generateNetworkDeviceContextMenu(ContextMenu contextMenu) {
        MenuItem propertiesOption = new MenuItem("Properties");

        MenuItem deleteOption = new MenuItem("Delete");
        deleteOption.setOnAction(event -> {
            if (contextMenuNetworkDevice != null) {
                simulationWorkspace.getChildren().remove(contextMenuNetworkDevice.getImageView());
                contextMenuNetworkDevice = null;
            }
        });
        contextMenu.getItems().addAll(propertiesOption, deleteOption);
    }

    private void spawn(NetworkDevice networkDevice) {
        NetworkDevice deepCopy = networkDevice.deepCopy();
        deepCopy.getImageView().setOpacity(0.5);
        cursorFollowingNetworkDevice = deepCopy;
    }

    private void placeFollowingNetworkDevice() {
        cursorFollowingNetworkDevice.getImageView().setOpacity(1.0);
        cursorFollowingNetworkDevice = null;
    }

    private boolean networkDeviceFollowingCursor() {
        return cursorFollowingNetworkDevice != null;
    }

    public void display() {
        stage.setScene(scene);
        stage.show();
    }

    public void setController(MasterController masterController) {
        this.masterController = masterController;
    }
}
