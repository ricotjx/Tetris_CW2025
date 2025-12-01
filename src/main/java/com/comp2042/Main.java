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
        GuiController c = fxmlLoader.getController();

        primaryStage.setTitle("TetrisJFX");

        int boardWidth = 10 * 20;
        int boardHeight = 20 * 20;
        int sidePanelWidth = 350;
        int windowHeight = boardHeight + sidePanelWidth;

        Scene scene = new Scene(root, boardWidth + sidePanelWidth, windowHeight);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(boardWidth + sidePanelWidth);
        primaryStage.setMinHeight(windowHeight);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
