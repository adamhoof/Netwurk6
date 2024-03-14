package view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Startup {

    private final Stage stage;
    private Scene scene;

    public Startup(Stage stage) {
        this.stage = stage;
        initializeView();
    }

    private void initializeView() {
        VBox root = new VBox();
        root.setSpacing(20);
        root.setAlignment(Pos.CENTER);

        Button startButton = new Button("New");
        startButton.setPrefSize(200, 40);
        startButton.setOnAction(event -> {
            // TODO show simulation setup screen
        });

        Button loadButton = new Button("Load");
        loadButton.setPrefSize(200, 40);
        loadButton.setOnAction(event -> {
            // TODO prompt for loading a saved simulation from file
        });

        Button optionsButton = new Button("Options");
        optionsButton.setPrefSize(200, 40);
        optionsButton.setOnAction(event -> {
            // TODO show options/settings screen
        });

        root.getChildren().addAll(startButton, loadButton, optionsButton);

        scene = new Scene(root, 800, 600);


    }

    public void display() {
        stage.setTitle("SpudrNet6");
        stage.setScene(scene);
        stage.show();
    }
}
