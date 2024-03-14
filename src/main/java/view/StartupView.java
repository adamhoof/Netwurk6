package view;

import controller.ViewTransitionController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StartupView {

    private final Stage stage;
    private Scene scene;

    private final ViewTransitionController viewTransitionController;

    public StartupView(Stage stage, ViewTransitionController viewTransitionController) {
        this.stage = stage;
        this.viewTransitionController = viewTransitionController;
        initializeView();
    }

    private void initializeView() {
        VBox menu = new VBox();
        menu.setSpacing(20);
        menu.setAlignment(Pos.CENTER);

        Button newButton = new Button("New");
        newButton.setPrefSize(200, 40);
        newButton.setOnAction(event -> this.viewTransitionController.showSimulationWorkspaceView());

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

        menu.getChildren().addAll(newButton, loadButton, optionsButton);

        scene = new Scene(menu, 800, 600);
    }

    public void display() {
        stage.setTitle("SpudrNet6");
        stage.setScene(scene);
        stage.show();
    }
}
