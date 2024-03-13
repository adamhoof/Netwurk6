package cz.cvut.fel.pjv.hofmaad2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    private Shape draggableShape;
    private final List<ToolBarButton> toolBarButtons = new ArrayList<>();


    @Override
    public void start(Stage primaryStage) {
        AnchorPane root = new AnchorPane();
        ToolBar toolBar = new ToolBar();

        Circle circle = new Circle(10);
        circle.setFill(Color.BLUEVIOLET);
        ToolBarButton routerToolBarButton = createToolBarButton(circle, "Router");
        toolBarButtons.add(routerToolBarButton);

        Rectangle rectangle = new Rectangle(20, 10);
        rectangle.setFill(Color.CADETBLUE);
        ToolBarButton switchToolBarButton = createToolBarButton(rectangle, "Switch");
        toolBarButtons.add(switchToolBarButton);

        Rectangle square = new Rectangle(20, 20);
        square.setFill(Color.RED);
        ToolBarButton pcToolBarButton = createToolBarButton(square, "PC");
        toolBarButtons.add(pcToolBarButton);

        toolBar.getItems().addAll(routerToolBarButton.button, switchToolBarButton.button, pcToolBarButton.button);

        if (!toolBarButtons.isEmpty()) {
            for (ToolBarButton toolBarButton : toolBarButtons) {
                setToolBarButtonClickEvent(toolBarButton, root);
            }
        }


        root.getChildren().add(toolBar);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SpudrNet6");
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private ToolBarButton createToolBarButton(Shape shape, String name) {
        Button button = new Button();
        button.setGraphic(shape);
        button.setText(name);
        return new ToolBarButton(button, shape);
    }

    private void setToolBarButtonClickEvent(ToolBarButton toolBarButton, Pane root) {
        toolBarButton.button.setOnMouseClicked(event -> {
            if (toolBarButton.shape instanceof Circle) {
                Circle original = (Circle) toolBarButton.shape;
                draggableShape = new Circle(original.getRadius() * 3, original.getFill());
            } else if (toolBarButton.shape instanceof Rectangle) {
                Rectangle original = (Rectangle) toolBarButton.shape;
                draggableShape = new Rectangle(original.getWidth() * 3, original.getHeight() * 3, original.getFill());
            } else {
                System.out.println("Wrong shape boi");
            }

            draggableShape.setLayoutX(event.getSceneX());
            draggableShape.setLayoutY(event.getSceneY());

            root.getChildren().add(draggableShape);
            root.setOnMouseMoved(this::moveShapeWithMouse);
            root.setOnMouseReleased(e -> dropShape(root));
        });
    }

    private void moveShapeWithMouse(MouseEvent event) {
        if (draggableShape != null) {
            draggableShape.setLayoutX(event.getSceneX());
            draggableShape.setLayoutY(event.getSceneY());
        }
    }

    private void dropShape(Pane root) {
        root.setOnMouseMoved(null);
        root.setOnMouseReleased(null);
        draggableShape = null;  // Clear reference to the shape after dropping
    }

    public static void main(String[] args) {
        launch(args);
    }
}
