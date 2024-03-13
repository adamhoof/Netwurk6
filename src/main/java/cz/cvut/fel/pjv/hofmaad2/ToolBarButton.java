package cz.cvut.fel.pjv.hofmaad2;

import javafx.scene.control.Button;
import javafx.scene.shape.Shape;

public class ToolBarButton {
    Button button;
    Shape shape;

    public ToolBarButton(Button button, Shape style) {
        this.button = button;
        this.shape = style;
    }
}
