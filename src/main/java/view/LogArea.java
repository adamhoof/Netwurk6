package view;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * The {@code LogArea} class extends {@code StackPane} and provides a dedicated area for
 * displaying log messages with an automatically scrolling view.
 *
 * It encapsulates a {@code TextFlow} within a {@code ScrollPane} to handle overflow by
 * scrolling, and uses a {@code Rectangle} as a visual frame around the log area.
 */
public class LogArea extends StackPane {
    private final TextFlow logWindow;
    private final Rectangle frame;
    private final ScrollPane logWindowScrollWrapper;

    /**
     * Constructs a {@code LogArea} with a specified width and height.
     *
     * @param width  the preferred width of the log area
     * @param height the preferred height of the log area
     */
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

    /**
     * Prints the given {@code Text} object to the log area and scrolls to the bottom.
     *
     * @param text the {@code Text} object to print
     */
    public void print(Text text) {
        logWindow.getChildren().add(text);
        // Scroll to the bottom to ensure the most recent log entry is visible
        logWindowScrollWrapper.setVvalue(logWindowScrollWrapper.getVmax());
    }
}
