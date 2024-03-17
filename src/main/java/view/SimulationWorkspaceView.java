package view;

import controller.MasterController;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

public class SimulationWorkspaceView {
    private final Stage stage;
    private Scene scene;
    private AnchorPane simulationWorkspace;

    private MasterController masterController;

    ToolBar toolBar;

    // This will hold the router representation that follows the mouse
    private Shape cursorFollowingShape = null;

    public SimulationWorkspaceView(Stage stage) {
        this.stage = stage;
        initializeView();
    }

    private void initializeView() {
        simulationWorkspace = new AnchorPane();
        toolBar = new ToolBar();

        Button routerToolBarButton = setupRouterToolBarButton();
        Button switchToolBarButton = setupSwitchToolBarButton();
        Button pcToolBarButton = setupPCToolBarButton();

        toolBar.getItems().addAll(routerToolBarButton, switchToolBarButton, pcToolBarButton);
        toolBar.toFront();
        simulationWorkspace.getChildren().add(toolBar);
        AnchorPane.setTopAnchor(toolBar, 0.0);

        setupFollowingShapeEvents();

        scene = new Scene(simulationWorkspace, 800, 600);
    }

    private Button setupRouterToolBarButton() {
        Button routerButton = new Button("Router");
        Circle routerRepresentation = new Circle(10);
        routerRepresentation.setFill(Color.BLUEVIOLET);
        routerButton.setGraphic(routerRepresentation);

        routerButton.setOnAction(event -> {
            if (cursorFollowingShape != null) {
                simulationWorkspace.getChildren().remove(cursorFollowingShape);
            }
            spawn(routerRepresentation);
        });
        return routerButton;
    }

    private Button setupSwitchToolBarButton() {
        Button switchButton = new Button("Switch");
        Rectangle switchRepresentation = new Rectangle(20, 10);
        switchRepresentation.setFill(Color.GREEN);
        switchButton.setGraphic(switchRepresentation);

        switchButton.setOnAction(clickEvent -> {
            if (cursorFollowingShape != null) {
                simulationWorkspace.getChildren().remove(cursorFollowingShape);
            }
            spawn(switchRepresentation);

        });
        return switchButton;
    }

    private Button setupPCToolBarButton() {
        Button pcButton = new Button("PC");
        Rectangle pcRepresentation = new Rectangle(20, 20);
        pcRepresentation.setFill(Color.RED);
        pcButton.setGraphic(pcRepresentation);

        pcButton.setOnAction(clickEvent -> {
            if (cursorFollowingShape != null) {
                simulationWorkspace.getChildren().remove(cursorFollowingShape);
            }
            spawn( pcRepresentation);
        });
        return pcButton;
    }

    private void spawn(Shape shape) {
        if (shape instanceof Circle) {
            Circle original = (Circle) shape;
            cursorFollowingShape = new Circle(original.getRadius() * 3, original.getFill());
        } else if (shape instanceof Rectangle) {
            Rectangle original = (Rectangle) shape;
            cursorFollowingShape = new Rectangle(original.getWidth() * 3, original.getHeight() * 3, original.getFill());
        } else {
            System.out.println("Wrong shape boi");
        }
        cursorFollowingShape.setOpacity(0.5);
    }

    private void setupFollowingShapeEvents() {
        simulationWorkspace.setOnMouseMoved(mouseEvent -> {
            if (cursorFollowingShape != null) {
                if (!simulationWorkspace.getChildren().contains(cursorFollowingShape)) {
                    simulationWorkspace.getChildren().add(cursorFollowingShape);
                    cursorFollowingShape.toBack();
                }

                cursorFollowingShape.setLayoutX(mouseEvent.getSceneX());
                cursorFollowingShape.setLayoutY(mouseEvent.getSceneY());
            }
        });

        simulationWorkspace.setOnMouseClicked(mouseEvent -> {

            if (cursorFollowingShape != null) {
                cursorFollowingShape.setOpacity(1.0);
                cursorFollowingShape = null;

            }
        });
    }

    public void display() {
        stage.setScene(scene);
        stage.show();
    }

    public void setController(MasterController masterController) {
        this.masterController = masterController;
    }
}
