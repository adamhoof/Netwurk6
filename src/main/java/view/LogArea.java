package view;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class LogArea extends StackPane {
    TextFlow logWindow;
    Rectangle frame;
    ScrollPane logWindowScrollWrapper;

    public LogArea(double width, double height) {
        this.setPrefSize(width, height);
        logWindow = new TextFlow();

        logWindowScrollWrapper = new ScrollPane(logWindow);
        logWindowScrollWrapper.setPrefViewportWidth(width);
        logWindowScrollWrapper.setPrefViewportHeight(height);
        logWindowScrollWrapper.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        frame = new Rectangle(width, height);
        frame.setStroke(Color.BLACK);
        frame.setFill(Color.TRANSPARENT);

        this.getChildren().addAll(frame, logWindowScrollWrapper);
    }

    public void print(Text text) {
        logWindow.getChildren().add(text);
        logWindowScrollWrapper.setVvalue(logWindowScrollWrapper.getVmax());
    }
}
