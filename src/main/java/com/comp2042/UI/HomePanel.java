package com.comp2042.UI;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class HomePanel extends VBox {

    private final Button zenModeButton;
    private final Button timeLimitModeButton;
    private final Button linesModeButton;
    private final Label titleLabel;
    private final Label subtitleLabel;

    public HomePanel() {
        setAlignment(Pos.CENTER);
        setSpacing(40);
        setVisible(true);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setPrefSize(500, 300);

        setStyle("-fx-background-color: rgba(0, 0, 0, 0.95); " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 50px; " +
                "-fx-border-color: #0000FF; " +
                "-fx-border-width: 6px; " +
                "-fx-border-radius: 20;"
        );

        // Main Title
        titleLabel = new Label("TETRIS");
        titleLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 45px; -fx-font-weight: bold; " +
                "-fx-text-fill: #000000; " +
                "-fx-effect: dropshadow(gaussian, white, 20, 0.7, 0, 0);");

        // Subtitle
        subtitleLabel = new Label("CHOOSE YOUR GAME MODE");
        subtitleLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 20px; -fx-font-weight: bold; " +
                "-fx-text-fill: white; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.8), 8, 0, 2, 2);");

        // Zen Mode Button
        zenModeButton = createModeButton("ZEN MODE",
                "Relaxed endless gameplay",
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32);");

        // Time Limit Mode Button
        timeLimitModeButton = createModeButton("TIME LIMIT",
                "Clear as many lines in 2 minutes",
                "-fx-background-color: linear-gradient(to bottom, #2196F3, #0D47A1);");

        // 40 Lines Mode Button
        linesModeButton = createModeButton("40 LINES",
                "Clear 40 lines as fast as you can",
                "-fx-background-color: linear-gradient(to bottom, #FF9800, #E65100);");

        // Button Container
        VBox buttonContainer = new VBox(25);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setMaxWidth(300);
        buttonContainer.getChildren().addAll(
                zenModeButton, timeLimitModeButton, linesModeButton
        );

        getChildren().addAll(titleLabel, subtitleLabel, buttonContainer);

        applyCss();
        layout();
    }

    private Button createModeButton(String title, String description, String buttonStyle) {
        VBox buttonContent = new VBox(5);
        buttonContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 15px; -fx-text-fill: #E0E0E0; -fx-font-style: italic;");

        buttonContent.getChildren().addAll(titleLabel, descLabel);

        Button button = new Button();
        button.setGraphic(buttonContent);
        button.setStyle(buttonStyle +
                " -fx-text-fill: white; " +
                " -fx-font-family: 'Arial'; " +
                " -fx-padding: 20px 30px; " +
                " -fx-background-radius: 10; " +
                " -fx-border-color: white; " +
                " -fx-border-width: 2px; " +
                " -fx-border-radius: 10; " +
                " -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        // Hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(buttonStyle.replace("to bottom", "to top") +
                    " -fx-text-fill: white; " +
                    " -fx-font-family: 'Arial'; " +
                    " -fx-padding: 20px 30px; " +
                    " -fx-background-radius: 10; " +
                    " -fx-border-color: #FFD700; " +
                    " -fx-border-width: 3px; " +
                    " -fx-border-radius: 10; " +
                    " -fx-effect: dropshadow(three-pass-box, rgba(255,215,0,0.6), 15, 0, 0, 0);");
        });

        button.setOnMouseExited(e -> {
            button.setStyle(buttonStyle +
                    " -fx-text-fill: white; " +
                    " -fx-font-family: 'Arial'; " +
                    " -fx-padding: 20px 30px; " +
                    " -fx-background-radius: 10; " +
                    " -fx-border-color: white; " +
                    " -fx-border-width: 2px; " +
                    " -fx-border-radius: 10; " +
                    " -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        });

        button.setMaxWidth(Double.MAX_VALUE);

        return button;
    }

    public Button getZenModeButton() {
        return zenModeButton;
    }

    public Button getTimeLimitModeButton() {
        return timeLimitModeButton;
    }

    public Button getLinesModeButton() {
        return linesModeButton;
    }

    public void showPanel() {
        setVisible(true);
        setManaged(true);
        toFront();
    }

    public void hidePanel() {
        setVisible(false);
        setManaged(false);
        toBack();
    }
}