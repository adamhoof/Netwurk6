package view;

import controller.MasterController;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
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

    private Shape cursorFollowingShape = null;
    private Shape selectedShape = null;

    public SimulationWorkspaceView(Stage stage) {
        this.stage = stage;
        initializeView();
    }

    private void initializeView() {
        simulationWorkspace = new AnchorPane();
        toolBar = new ToolBar();

        Button routerToolBarButton = setupToolBarButton("Router", new Circle(10, Color.BLUEVIOLET));
        Button switchToolBarButton = setupToolBarButton("Switch", new Rectangle(20, 10, Color.GREEN));
        Button pcToolBarButton = setupToolBarButton("PC", new Rectangle(20, 20, Color.RED));

        toolBar.getItems().addAll(routerToolBarButton, switchToolBarButton, pcToolBarButton);
        toolBar.toFront();
        simulationWorkspace.getChildren().add(toolBar);
        AnchorPane.setTopAnchor(toolBar, 0.0);

        setupWorkspaceEvents();
        setupFollowingShapeEvents();

        scene = new Scene(simulationWorkspace, 800, 600);
    }

    private Button setupToolBarButton(String label, Shape shape) {
        Button button = new Button(label);
        button.setGraphic(shape);

        button.setOnAction(event -> {
            if (cursorFollowingShape != null) {
                simulationWorkspace.getChildren().remove(cursorFollowingShape);
            }
            if (selectedShape != null) {
                deselectSelectedShape();
            }
            spawn(shape);
        });

        return button;
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
    }

    private void setupWorkspaceEvents() {
        simulationWorkspace.setOnMouseClicked(mouseEvent -> {
            if (followingShapeExists()) {
                setupPlacedShapeEvents(cursorFollowingShape);
                placeFollowingShape();
            } else if (selectedShapeExists() && !cursorAtShape(selectedShape, mouseEvent)) {
                deselectSelectedShape();
                selectedShape = null;
            }
        });
    }

    private void setupPlacedShapeEvents(Shape shape) {
        final double[] cursorDistanceFromShapeTopLeft = new double[2];

        shape.setOnMousePressed(mouseEvent -> {
            if (selectedShape != null) {
                deselectSelectedShape();
            }
            cursorDistanceFromShapeTopLeft[0] = shape.getLayoutX() - mouseEvent.getSceneX();
            cursorDistanceFromShapeTopLeft[1] = shape.getLayoutY() - mouseEvent.getSceneY();

            selectedShape = shape;
            selectedShape.setStroke(Color.BLACK);
            selectedShape.setStrokeWidth(4);
            selectedShape.toFront();
        });

        shape.setOnMouseDragged(mouseEvent -> {
            shape.setLayoutX(mouseEvent.getSceneX() + cursorDistanceFromShapeTopLeft[0]);
            shape.setLayoutY(mouseEvent.getSceneY() + cursorDistanceFromShapeTopLeft[1]);
        });
    }

    private void spawn(Shape toClone) {
        Shape shape = cloneShape(toClone);
        shape.setOpacity(0.5);
        cursorFollowingShape = shape;
    }

    private Shape cloneShape(Shape shape) {
        if (shape instanceof Circle) {
            Circle original = (Circle) shape;
            return new Circle(original.getRadius() * 3, original.getFill());
        } else if (shape instanceof Rectangle) {
            Rectangle original = (Rectangle) shape;
            return new Rectangle(original.getWidth() * 3, original.getHeight() * 3, original.getFill());
        }
        throw new IllegalArgumentException("Unsupported shape type");
    }

    private void deselectSelectedShape() {
        selectedShape.setStrokeWidth(0);
    }

    private void placeFollowingShape() {
        cursorFollowingShape.setOpacity(1.0);
        cursorFollowingShape = null;
    }

    private boolean cursorAtShape(Shape shape, MouseEvent mouseEvent) {
        if (shape != null) {

            double localX = shape.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY()).getX();
            double localY = shape.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY()).getY();

            return shape.contains(localX, localY);
        }
        return false;
    }

    private boolean followingShapeExists() {
        return cursorFollowingShape != null;
    }

    private boolean selectedShapeExists() {
        return selectedShape != null && cursorFollowingShape == null;
    }

    public void display() {
        stage.setScene(scene);
        stage.show();
    }

    public void setController(MasterController masterController) {
        this.masterController = masterController;
    }
}
