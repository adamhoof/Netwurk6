package view;

import controller.SimulationActionsListener;
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

    private final SimulationActionsListener simulationActionsListener;

    public SimulationWorkspaceView(Stage stage, SimulationActionsListener simulationActionsListener) {
        this.stage = stage;
        this.simulationActionsListener = simulationActionsListener;
        initializeView();
    }

    private void initializeView() {
        AnchorPane simulationWorkspace = new AnchorPane();

        ToolBar toolBar = new ToolBar();

        Circle circle = new Circle(10);
        circle.setFill(Color.BLUEVIOLET);
        Button routerToolBarButton = createToolBarButton(circle, "Router");
        routerToolBarButton.setOnAction(event -> simulationActionsListener.addRouter());


        Rectangle rectangle = new Rectangle(20, 10);
        rectangle.setFill(Color.CADETBLUE);
        Button switchToolBarButton = createToolBarButton(rectangle, "Switch");

        Rectangle square = new Rectangle(20, 20);
        square.setFill(Color.RED);
        Button pcToolBarButton = createToolBarButton(square, "PC");

        toolBar.getItems().addAll(routerToolBarButton, switchToolBarButton, pcToolBarButton);
        simulationWorkspace.getChildren().add(toolBar);
        AnchorPane.setTopAnchor(toolBar, 0.0);

        scene = new Scene(simulationWorkspace, 800, 600);
    }

    private Button createToolBarButton(Shape shape, String name) {
        Button button = new Button();
        button.setText(name);

        if (shape != null) {
            button.setGraphic(shape);
        }
        return button;
    }

    public void display() {
        stage.setScene(scene);
        stage.show();
    }
}
