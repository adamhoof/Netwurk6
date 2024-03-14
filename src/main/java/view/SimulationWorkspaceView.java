package view;

import controller.SimulationActionsListener;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class SimulationWorkspaceView {
    private final Stage stage;
    private Scene scene;
    private AnchorPane simulationWorkspace;

    private ToolBar toolBar;

    private SimulationActionsListener simulationActionsListener;

    public SimulationWorkspaceView(Stage stage, SimulationActionsListener simulationActionsListener) {
        this.stage = stage;
        this.simulationActionsListener = simulationActionsListener;
        initializeView();
    }

    private void initializeView() {
        simulationWorkspace = new AnchorPane();
        toolBar = new ToolBar();

        Button routerToolBarButton = setupRouterToolBarButton();

        toolBar.getItems().addAll(routerToolBarButton);

        simulationWorkspace.getChildren().add(toolBar);
        AnchorPane.setTopAnchor(toolBar, 0.0);
        scene = new Scene(simulationWorkspace, 800, 600);
    }

    private Circle createRouterRepresentation() {
        Circle router = new Circle(10); // Radius of 10
        router.setFill(Color.BLUEVIOLET); // Color it for distinction
        return router;
    }

    private Button setupRouterToolBarButton() {
        Button routerButton = new Button("Router");
        Circle routerButtonStyle = createRouterRepresentation();
        routerButton.setGraphic(routerButtonStyle);

        routerButton.setOnAction(event -> {
            Circle clone = new Circle(routerButtonStyle.getRadius());
            clone.setFill(routerButtonStyle.getFill());
            simulationWorkspace.getChildren().add(clone);
            setupDragAndDrop(clone);
        });

        return routerButton;
    }

    private void setupDragAndDrop(Circle router) {
        // Capture initial mouse click position
       /* router.setOnMousePressed(mouseEvent -> {
            router.setCenterX(mouseEvent.getX());
            router.setCenterY(mouseEvent.getY());
        });*/

        // Update router position as mouse moves
        simulationWorkspace.setOnMouseDragged(mouseEvent -> {
            router.setCenterX(mouseEvent.getX());
            router.setCenterY(mouseEvent.getY());
        });

        // Finalize router position on mouse release
        simulationWorkspace.setOnMouseReleased(mouseEvent -> {
            router.setCenterX(mouseEvent.getX());
            router.setCenterY(mouseEvent.getY());

            // Remove drag-and-drop handlers to stop dragging
            simulationWorkspace.setOnMouseDragged(null);
            simulationWorkspace.setOnMouseReleased(null);
            simulationActionsListener.addedRouter();
        });
    }

    public void display() {
        stage.setScene(scene);
        stage.show();
    }
}