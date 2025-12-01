package com.comp2042.UI;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameOverPanel extends VBox {

public class GameOverPanel extends BorderPane {
    private final Button restartButton;
    private final Button homeButton;
    private final Label gameOverLabel;
    private final Label scoreLabel;
    private final Label levelLabel;
    private final Label linesLabel;
    private final Label timeLabel;

    public GameOverPanel() {
        setAlignment(Pos.CENTER);
        setSpacing(30);
        setVisible(false);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setPrefSize(500, 300);

        setStyle("-fx-background-color: rgba(0, 0, 0, 0.95); " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 40px; " +
                "-fx-border-color: #FF0000; " +
                "-fx-border-width: 5px; " +
                "-fx-border-radius: 15;"
        );

        // Game Over Title
        gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 45px; -fx-font-weight: bold; -fx-text-fill: red; -fx-effect: dropshadow(gaussian, white, 15, 0.7, 0, 0);");

        // Final Score Display
        scoreLabel = new Label("Final Score: 0");
        scoreLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.8), 8, 0, 2, 2);");

        // Stats Section
        Label statsTitle = new Label("GAME STATS");
        statsTitle.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-padding: 10 0 10 0;");

        // Level Stat
        HBox levelBox = new HBox(10);
        levelBox.setAlignment(Pos.CENTER);
        Label levelText = new Label("Level:");
        levelText.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 15px; -fx-text-fill: #44ff44; -fx-font-weight: bold;");
        levelLabel = new Label("1");
        levelLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 15px; -fx-text-fill: white; -fx-font-weight: bold;");
        levelBox.getChildren().addAll(levelText, levelLabel);

        // Lines Stat
        HBox linesBox = new HBox(10);
        linesBox.setAlignment(Pos.CENTER);
        Label linesText = new Label("Lines Cleared:");
        linesText.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 15px; -fx-text-fill: #4488ff; -fx-font-weight: bold;");
        linesLabel = new Label("0");
        linesLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 15px; -fx-text-fill: white; -fx-font-weight: bold;");
        linesBox.getChildren().addAll(linesText, linesLabel);

        // Time Stat
        HBox timeBox = new HBox(10);
        timeBox.setAlignment(Pos.CENTER);
        Label timeText = new Label("Time:");
        timeText.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 15px; -fx-text-fill: #ff66cc; -fx-font-weight: bold;");
        timeLabel = new Label("00:00");
        timeLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 15px; -fx-text-fill: white; -fx-font-weight: bold;");
        timeBox.getChildren().addAll(timeText, timeLabel);


        // Stats Container
        VBox statsContainer = new VBox(8);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.getChildren().addAll(statsTitle, levelBox, linesBox, timeBox);

        // Restart Button
        restartButton = new Button("PLAY AGAIN");
        restartButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 15px 30px; -fx-background-radius: 8;");

        // Home Button
        homeButton = new Button("MAIN MENU");
        homeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 15px 30px; -fx-background-radius: 8;");

        // Button Container
        HBox buttonContainer = new HBox(20);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(restartButton, homeButton);

        getChildren().addAll(gameOverLabel, scoreLabel, statsContainer, buttonContainer);

        applyCss();
        layout();
    }

    public void setFinalScore(int score) {
        scoreLabel.setText("Final Score: " + score);
    }

    public void setGameStats(int level, int lines, String time) {
        levelLabel.setText(String.valueOf(level));
        linesLabel.setText(String.valueOf(lines));
        timeLabel.setText(time);
    }

    public Button getRestartButton() {
        return restartButton;
    }

    public Button getHomeButton() {
        return homeButton;
    }
}