package com.comp2042.UI;

import com.comp2042.core.GameController;
import com.comp2042.core.Score;
import com.comp2042.model.DownData;
import com.comp2042.model.ViewData;
import com.comp2042.model.events.MoveEvent;
import com.comp2042.model.events.EventSource;
import com.comp2042.model.events.EventType;
import com.comp2042.model.events.InputEventListener;
import com.comp2042.UI.GameOverPanel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.util.Callback;

public class GuiController implements Initializable {

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private StackPane homeContainer;

    @FXML
    private StackPane gameOverContainer;

    @FXML
    private Label scoreLabel;

    @FXML
    private GridPane holdPanel;

    @FXML
    private Label levelLabel;

    @FXML
    private Label linesLabel;

    @FXML
    private Label comboLabel;

    @FXML
    private VBox statsPanel;

    @FXML
    private Label timerLabel;

    private HomePanel homePanel;
    private GameOverPanel gameOverPanel;
    private GameRenderer gameRenderer;
    private InputHandler inputHandler;
    private GameStateManager gameStateManager;
    private InputEventListener eventListener;
    private Timeline timeLine;
    private GameTimer gameTimer;

    private Runnable onRestartGame;

    // Initializes the controller after FXML loading
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        homePanel = new HomePanel();
        gameOverPanel = new GameOverPanel();
        // Initialize component managers
        gameRenderer = new GameRenderer(gamePanel, brickPanel, holdPanel, groupNotification,
                scoreLabel, levelLabel, linesLabel, comboLabel, timerLabel);
        inputHandler = new InputHandler(this);
        gameStateManager = new GameStateManager();

        // Add home panel to home container
        if (homeContainer != null && homePanel != null) {
            homeContainer.getChildren().add(homePanel);
            StackPane.setAlignment(homePanel, Pos.CENTER);
        }

        // Add game over panel to game over container
        if (gameOverContainer != null && gameOverPanel != null) {
            gameOverContainer.getChildren().add(gameOverPanel);
            StackPane.setAlignment(gameOverPanel, Pos.CENTER);
        }

        setupHomePageActions();
        setupGameOverActions();

        // Load custom font
        try {
            URL fontUrl = getClass().getClassLoader().getResource("digital.ttf");
            if (fontUrl != null) {
                Font.loadFont(fontUrl.toExternalForm(), 38);
            }
        } catch (Exception e) {
            System.err.println("Could not load font: " + e.getMessage());
        }

        // Set up game panel for input
        if (gamePanel != null) {
            gamePanel.setFocusTraversable(true);
            gamePanel.requestFocus();
            gamePanel.setOnKeyPressed(this::handleKeyPressed);
        }

        // Initialize score display
        if (scoreLabel != null) {
            scoreLabel.setText("0");
        }

        if (holdPanel != null) {
            holdPanel.getChildren().clear();
        }

        initializeStatsDisplay();

        // Show home page when application starts
        showHomePage();

        // Initialize timer
        gameTimer = new GameTimer();
        gameTimer.setOnTickCallback(param -> {
            String formattedTime = gameTimer.getFormattedTime();
            Platform.runLater(() -> {
                if (timerLabel != null) {
                    timerLabel.setText(formattedTime);
                }
            });
            return formattedTime;
        });
    }

    // Sets up home page button actions
    private void setupHomePageActions() {
        if (homePanel != null) {
            homePanel.getZenModeButton().setOnAction(actionEvent -> startGame("ZEN"));
            homePanel.getTimeLimitModeButton().setOnAction(actionEvent -> startGame("TIME_LIMIT"));
            homePanel.getLinesModeButton().setOnAction(actionEvent -> startGame("40_LINES"));
        }
    }

    // Starts a new game with specified game mode
    private void startGame(String gameMode) {
        System.out.println("Starting game mode: " + gameMode);
        hideHomePage();

        newGame();
    }

    // Shows the home page (main menu) and resets game state
    public void showHomePage() {
        if (homeContainer != null) {
            homeContainer.setVisible(true);
            homeContainer.toFront();
        }
        if (gameOverContainer != null) {
            gameOverContainer.setVisible(false); // Hide game over container
        }

        if (homePanel != null) {
            homePanel.showPanel();
        }

        gameStateManager.goHome();

        // Stop any existing game timers when showing home page
        if (timeLine != null) {
            timeLine.stop();
        }
        stopTimer();

        // Clear hold preview when returning to home
        gameRenderer.clearHoldPreview();
    }

    // Hides the home page (main menu)
    public void hideHomePage() {
        if (homeContainer != null) {
            homeContainer.setVisible(false);
        }
        gameStateManager.setHomeScreen(false);
    }

    // Initializes the stats display with default values
    private void initializeStatsDisplay() {
        if (levelLabel != null) {
            levelLabel.setText("1");
        }
        if (linesLabel != null) {
            linesLabel.setText("0");
        }
        if (comboLabel != null) {
            comboLabel.setText("x0");
        }
        if (timerLabel != null) {
            timerLabel.setText("00:00");
        }
    }

    // Sets up game over panel button actions
    private void setupGameOverActions() {
        if (gameOverPanel != null) {
            gameOverPanel.getRestartButton().setOnAction(event -> {
                System.out.println("=== RESTART BUTTON CLICKED ===");
                newGame();
            });

            gameOverPanel.getHomeButton().setOnAction(event -> {
                System.out.println("=== HOME BUTTON CLICKED ===");
                showHomePage(); // Hide game over container and show home container
            });
        }
    }

    // Sets callback for game restart
    public void setOnRestartGame(Runnable onRestartGame) {
        this.onRestartGame = onRestartGame;
    }

    // Handles keyboard input by delegating to InputHandler
    private void handleKeyPressed(KeyEvent keyEvent) {
        inputHandler.handleKeyPressed(keyEvent,
                gameStateManager.isHomeScreen(),
                gameStateManager.isGameOver(),
                gameStateManager.isPause());
    }

    // Initializes the game view with board and brick data
    public void initGameView(int[][] boardMatrix, ViewData viewData) {
        gameRenderer.initGameView(boardMatrix, viewData);

        // Start timer when game initializes
        startTimer();

        timeLine = new Timeline(new KeyFrame(
                Duration.millis(dropSpeed()),  // Use dynamic speed based on level
                event -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD)))
        );
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    // Refreshes the entire game view
    public void refreshGameView(ViewData viewData) {
        gameRenderer.refreshGameView(viewData);
    }

    // Refreshes only the game background
    public void refreshGameBackground(int[][] board) {
        gameRenderer.refreshGameBackground(board);
    }

    // Moves current brick down one position
    public boolean moveDown(MoveEvent event) {
        if (eventListener == null || gameStateManager.isPause()) return false;

        DownData downData = eventListener.onDownEvent(event);
        if (downData != null) {
            refreshGameView(downData.getViewData());
            updateStatsFromGameController();

            // Handle cleared rows
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                gameRenderer.refreshGameBackground(downData.getClearRow().getNewMatrix());

                // Show score notification
                if (groupNotification != null) {
                    try {
                        NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                        groupNotification.getChildren().add(notificationPanel);
                        notificationPanel.showScore(groupNotification.getChildren());
                    } catch (Exception e) {
                        System.err.println("Failed to show notification: " + e.getMessage());
                    }
                }
            }
            return true;
        }

        if (gamePanel != null) {
            gamePanel.requestFocus();
        }
        return false;
    }

    // Updates stats from game controller
    private void updateStatsFromGameController() {
        if (eventListener instanceof GameController) {
            GameController gameController = (GameController) eventListener;
            updateStats(gameController.getScore());
        }
    }

    // Gets message for lines cleared
    private String getClearMessage(int linesCleared) {
        return switch (linesCleared) {
            case 1 -> "SINGLE";
            case 2 -> "DOUBLE";
            case 3 -> "TRIPLE";
            case 4 -> "TETRIS!";
            default -> linesCleared + " LINES";
        };
    }

    // Sets the game event listener
    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
        inputHandler.setEventListener(eventListener);
        if (eventListener instanceof GameController) {
            GameController gameController = (GameController) eventListener;
            bindScore(gameController.getScore().scoreProperty());
            bindScoreProperties(gameController.getScore());
            gameStateManager.setGameController(gameController);
        }
    }

    // Binds score label to score property
    public void bindScore(IntegerProperty integerProperty) {
        if (scoreLabel != null && integerProperty != null) {
            scoreLabel.textProperty().bind(integerProperty.asString());
        }
    }

    // Updates score properties from Score object
    public void bindScoreProperties(Score score) {
        if (score != null) {
            updateStats(score);
        }
    }

    // Updates all game stats
    private void updateStats(Score score) {
        gameRenderer.updateStats(score);
        updateGameSpeed(score.getLevel());
    }

    // Updates game speed based on current level
    private void updateGameSpeed(int level) {
        if (timeLine != null) {
            timeLine.stop();
            long newDropSpeed = getSpeedForLevel(level);
            timeLine = new Timeline(new KeyFrame(
                    Duration.millis(newDropSpeed),
                    event -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
            ));
            timeLine.setCycleCount(Timeline.INDEFINITE);
            timeLine.play();
        }
    }

    // Timer methods
    // Starts the game timer
    private void startTimer() {
        if (gameTimer != null) {
            gameTimer.start();
        }
    }

    // Stops the game timer
    private void stopTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    // Resets the game timer
    private void resetTimer() {
        if (gameTimer != null) {
            gameTimer.reset();
            if (timerLabel != null) {
                timerLabel.setText("00:00");
            }
        }
    }

    // Gets formatted game time
    public String getFormattedTime() {
        if (gameTimer != null) {
            return gameTimer.getFormattedTime();
        }
        return "00:00";
    }

    // Gets elapsed time in milliseconds
    public long getElapsedTime() {
        if (gameTimer != null) {
            return gameTimer.getElapsedTime();
        }
        return 0;
    }

    // Checks if timer is running
    public boolean isTimerRunning() {
        if (gameTimer != null) {
            return gameTimer.isRunning();
        }
        return false;
    }

    // Shows special clear messages
    public void showSpecialClearMessage(String message, int durationMs) {
        if (groupNotification != null) {
            try {
                NotificationPanel notificationPanel = new NotificationPanel(message);

                if (message.contains("TETRIS")) {
                    notificationPanel.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
                } else if (message.contains("TRIPLE")) {
                    notificationPanel.setStyle("-fx-background-color: #ffaa00; -fx-text-fill: black; -fx-font-weight: bold;");
                } else if (message.contains("DOUBLE")) {
                    notificationPanel.setStyle("-fx-background-color: #44ff44; -fx-text-fill: black; -fx-font-weight: bold;");
                } else {
                    notificationPanel.setStyle("-fx-background-color: #4488ff; -fx-text-fill: white; -fx-font-weight: bold;");
                }

                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());

                new Timeline(new KeyFrame(
                        Duration.millis(durationMs),
                        e -> groupNotification.getChildren().remove(notificationPanel)
                )).play();
            } catch (Exception e) {
                System.err.println("Failed to show special notification: " + e.getMessage());
            }
        }
    }

    // Handles game over sequence
    public void gameOver(int finalScore) {
        System.out.println("=== GUI CONTROLLER: GAME OVER CALLED ===");

        // Stop the timeline and timer
        if (timeLine != null) {
            timeLine.stop();
            timeLine = null;
        }
        stopTimer();

        // Clear hold preview on game over
        gameRenderer.clearHoldPreview();

        // Show game over panel
        if (gameOverContainer != null && gameOverPanel != null) {
            gameOverPanel.setFinalScore(finalScore);

            if (eventListener instanceof GameController) {
                GameController gameController = (GameController) eventListener;
                Score score = gameController.getScore();
                gameOverPanel.setGameStats(
                        score.getLevel(),
                        score.getTotalLinesCleared(),
                        getFormattedTime()
                );
            }

            // Hide home container and show game over container
            if (homeContainer != null) {
                homeContainer.setVisible(false);
            }

            gameOverContainer.setVisible(true);
            gameOverPanel.setVisible(true);
            gameOverContainer.toFront();

            // Force updates
            gameOverPanel.applyCss();
            gameOverPanel.layout();
            gameOverContainer.applyCss();
            gameOverContainer.layout();

            System.out.println("=== GAME OVER PANEL SHOULD BE VISIBLE ===");
        }

        gameStateManager.gameOver();
    }

    // Starts a new game
    public void newGame() {
        System.out.println("=== STARTING NEW GAME ===");

        // Stop existing game
        if (timeLine != null) {
            timeLine.stop();
            timeLine = null;
        }

        stopTimer();
        resetTimer();

        gameStateManager.startNewGame();

        // Hide both containers to show the game
        if (homeContainer != null) {
            homeContainer.setVisible(false);
        }
        if (gameOverContainer != null) {
            gameOverContainer.setVisible(false);
        }

        // Focus game panel
        if (gamePanel != null) {
            gamePanel.setVisible(true);
            gamePanel.requestFocus();
        }

        // create a new GameController instance to ensure fresh state
        System.out.println("Creating NEW GameController instance...");
        eventListener = new GameController(this);  // Always create new

        if (eventListener instanceof GameController gameController) {
            System.out.println("GameController created, calling createNewGame()...");
            gameStateManager.setGameController(gameController);
            gameController.createNewGame();  // This sets gameStarted = true

            System.out.println("Updating UI...");
            updateStats(gameController.getScore());
            initGameView(gameController.getCurrentBoard(), gameController.getViewData());
        }

        // Clear hold preview when starting new game
        gameRenderer.clearHoldPreview();

        // Start new timer
        startTimer();

        if (gamePanel != null) {
            gamePanel.requestFocus();
        }
    }

    // Calculates current drop speed based on level
    private long dropSpeed() {
        int level = 1; // Default to level 1

        if (eventListener instanceof GameController) {
            GameController gameController = (GameController) eventListener;
            level = gameController.getScore().getLevel();
            // Safety check
            level = Math.max(1, level);
        }

        return getSpeedForLevel(level);
    }

    // Gets drop speed for specific level
    // Based on classic tetris level speeds
    private long getSpeedForLevel(int level) {
        return switch (level) {
            case 1 -> 1500;  // 1.5s
            case 2 -> 1200;  // 1.2s
            case 3 -> 900;   // 0.9s
            case 4 -> 600;   // 0.6s
            case 5 -> 400;   // 0.4s
            case 6 -> 300;   // 0.3s
            case 7 -> 220;   // 0.22s
            case 8 -> 180;   // 0.18s
            case 9 -> 150;   // 0.15s
            case 10 -> 120;  // 0.12s
            case 11 -> 100;  // 0.1s
            case 12 -> 90;   // 0.09s
            case 13 -> 80;   // 0.08s
            case 14 -> 70;   // 0.07s
            case 15 -> 60;   // 0.06s
            default -> Math.max(30, 60 - (level - 15) * 3);
        };
    }

    // Toggles game pause state
    public void pauseGame() {
        gameStateManager.togglePause();

        if (gameStateManager.isPause()) {
            if (timeLine != null) {
                timeLine.stop();
            }
            if (gameTimer != null) {
                gameTimer.pause();
            }
        } else if (timeLine != null && gameStateManager.isGameActive()) {
            timeLine.play();
            if (gameTimer != null) {
                gameTimer.resume();
            }
        }

        if (gamePanel != null) {
            gamePanel.requestFocus();
        }
    }

    // Hides game over panel
    public void hideGameOverPanel() {
        if (gameOverContainer != null) {
            gameOverContainer.setVisible(false);
            gameOverContainer.setManaged(false);
            gameOverContainer.toBack();
            gameOverContainer.applyCss();
            gameOverContainer.layout();
        }
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(false);
            gameOverPanel.setManaged(false);
            gameOverPanel.applyCss();
            gameOverPanel.layout();
        }

        gameStateManager.reset();
    }
}