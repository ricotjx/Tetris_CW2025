// Main entry point for the TetrisJFX application
// Initializes the JavaFX application and sets up the main game window

package com.comp2042;

import com.comp2042.UI.GuiController;
import com.comp2042.core.GameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
        ResourceBundle resources = null;
        FXMLLoader fxmlLoader = new FXMLLoader(location, resources);
        Parent root = fxmlLoader.load();

        // Get the controller from the FXML loader
        GuiController c = fxmlLoader.getController();

        // Set up the main window
        primaryStage.setTitle("TetrisJFX");

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    // Main method that launches the JavaFX application
    // param args: command line arguments
    public static void main(String[] args) {
        launch(args);
    }
}
