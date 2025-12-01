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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;

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
    private Rectangle[][] displayMatrix;
    private InputEventListener eventListener;
    private Rectangle[][] nextBrickRectangles;
    private Timeline timeLine;
    private Timeline timerTimeLine;
    private long startTime;
    private long elapsedTime;
    private boolean isTimerRunning = false;

    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);
    private final BooleanProperty isHomeScreen = new SimpleBooleanProperty(true);

    private int[][] currentBoardMatrix;
    private Runnable onRestartGame;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        homePanel = new HomePanel();
        gameOverPanel = new GameOverPanel();

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

        try {
            URL fontUrl = getClass().getClassLoader().getResource("digital.ttf");
            if (fontUrl != null) {
                Font.loadFont(fontUrl.toExternalForm(), 38);
            }
        } catch (Exception e) {
            System.err.println("Could not load font: " + e.getMessage());
        }

        if (gamePanel != null) {
            gamePanel.setFocusTraversable(true);
            gamePanel.requestFocus();
            gamePanel.setOnKeyPressed(this::handleKeyPressed);
        }

        if (scoreLabel != null) {
            scoreLabel.setText("0");
        }

        if (holdPanel != null) {
            holdPanel.getChildren().clear();
        }

        initializeStatsDisplay();

        // Show home page when application starts
        showHomePage();
    }

    private void setupHomePageActions() {
        if (homePanel != null) {
            homePanel.getZenModeButton().setOnAction(actionEvent -> startGame("ZEN"));
            homePanel.getTimeLimitModeButton().setOnAction(actionEvent -> startGame("TIME_LIMIT"));
            homePanel.getLinesModeButton().setOnAction(actionEvent -> startGame("40_LINES"));
        }
    }

    private void startGame(String gameMode) {
        System.out.println("Starting game mode: " + gameMode);
        hideHomePage();

        // Create the GameController only when starting a game
        if (eventListener == null) {
            eventListener = new GameController(this);
        }
        newGame();
    }

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

        isHomeScreen.set(true);
        isGameOver.set(false);

        // Stop any existing game timers when showing home page
        if (timeLine != null) {
            timeLine.stop();
        }
        stopTimer();
    }

    public void hideHomePage() {
        if (homeContainer != null) {
            homeContainer.setVisible(false);
        }
        isHomeScreen.set(false);
    }

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

    public void setOnRestartGame(Runnable onRestartGame) {
        this.onRestartGame = onRestartGame;
    }

    private void handleKeyPressed(KeyEvent keyEvent) {
        if (eventListener == null) return;

        KeyCode code = keyEvent.getCode();

        // Don't process game keys when on home screen or game over/pause
        if (isHomeScreen.get() || isGameOver.get()) {
            return;
        }

        if (!isPause.get() && !isGameOver.get()) {
            switch (code) {
                case LEFT:
                case A:
                    refreshGameView(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                    keyEvent.consume();
                    break;
                case RIGHT:
                case D:
                    refreshGameView(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                    keyEvent.consume();
                    break;
                case UP:
                case W:
                    refreshGameView(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                    keyEvent.consume();
                    break;
                case DOWN:
                case S:
                    moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                    keyEvent.consume();
                    break;
                case SPACE:
                    if (eventListener instanceof GameController gc) {
                        gc.hardDrop();
                        refreshGameView(gc.getViewData());
                    }
                    keyEvent.consume();
                    break;
                case C:
                    ViewData holdView = eventListener.onHoldEvent(new MoveEvent(EventType.HOLD, EventSource.USER));
                    if (holdView != null) {
                        refreshGameView(holdView);
                    }
                    keyEvent.consume();
                    break;
            }
        }

        if (code == KeyCode.N) {
            newGame();
        }

        if (code == KeyCode.H) {
            showHomePage();
        }

        if (code == KeyCode.ESCAPE) {
            if (isHomeScreen.get()) {
                return;
            }
            pauseGame();
        }
    }

    private void updateHoldBrickPreview(int[][] holdBrick) {
        if (holdPanel == null) return;

        holdPanel.getChildren().clear();

        if (holdBrick == null) return;

        int rows = holdBrick.length;
        int cols = holdBrick[0].length;

        int panelRows = 4;
        int panelCols = 4;

        int rowOffset = (panelRows - rows) / 2;
        int colOffset = (panelCols - cols) / 2;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (holdBrick[i][j] != 0) {
                    Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    r.setFill(getFillColor(holdBrick[i][j]));
                    holdPanel.add(r, j + colOffset, i + rowOffset);
                }
            }
        }
    }

    public void initGameView(int[][] boardMatrix, ViewData viewData) {
        if (boardMatrix == null || boardMatrix.length == 0) {
            throw new IllegalArgumentException("Board matrix cannot be null or empty");
        }

        this.currentBoardMatrix = copyMatrix(boardMatrix);

        if (gamePanel != null) {
            gamePanel.getChildren().clear();
        }

        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];

        for (int i = 0; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(boardMatrix[i][j]));
                rectangle.setStroke(Color.GRAY);
                rectangle.setStrokeWidth(0.5);
                displayMatrix[i][j] = rectangle;
                if (gamePanel != null) {
                    gamePanel.add(rectangle, j, i);
                }
            }
        }

        updateNextBrickPreview(viewData);

        // Start timer when game initializes
        startTimer();

        timeLine = new Timeline(new KeyFrame(
                Duration.millis(dropSpeed()),  // Use dynamic speed based on level
                event -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD)))
        );
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    private void updateNextBrickPreview(ViewData viewData) {
        if (brickPanel != null) {
            brickPanel.getChildren().clear();
        }

        if (viewData != null && viewData.getNextBrickData() != null) {
            int[][] nextBrickData = viewData.getNextBrickData();

            int rows = nextBrickData.length;
            int cols = nextBrickData[0].length;

            int panelRows = 4;
            int panelCols = 4;

            int rowOffset = (panelRows - rows) / 2;
            int colOffset = (panelCols - cols) / 2;

            nextBrickRectangles = new Rectangle[nextBrickData.length][nextBrickData[0].length];
            for (int i = 0; i < nextBrickData.length; i++) {
                for (int j = 0; j < nextBrickData[i].length; j++) {
                    if (nextBrickData[i][j] != 0) {
                        Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                        rectangle.setFill(getFillColor(nextBrickData[i][j]));
                        nextBrickRectangles[i][j] = rectangle;
                        if (brickPanel != null) {
                            brickPanel.add(rectangle, j + colOffset, i + rowOffset);
                        }
                    }
                }
            }
        }
    }

    private Paint getFillColor(int colorCode) {
        return switch (colorCode) {
            case 0 -> Color.TRANSPARENT;
            case 1 -> Color.AQUA;
            case 2 -> Color.BLUE;
            case 3 -> Color.ORANGE;
            case 4 -> Color.YELLOW;
            case 5 -> Color.GREEN;
            case 6 -> Color.PURPLE;
            case 7 -> Color.RED;
            default -> Color.WHITE;
        };
    }

    private void refreshGameView(ViewData viewData) {
        if (viewData == null) return;

        refreshGameBackground(currentBoardMatrix);
        drawFallingBrick(viewData);
        updateNextBrickPreview(viewData);
        updateHoldBrickPreview(viewData.getHoldBrickData());
    }

    private void drawFallingBrick(ViewData viewData) {
        if (viewData == null || viewData.getBrickData() == null) return;

        int[][] brickData = viewData.getBrickData();
        int xPos = viewData.getxPosition();
        int yPos = viewData.getyPosition();

        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {
                if (brickData[i][j] != 0) {
                    int gridX = xPos + j;
                    int gridY = yPos + i;

                    if (gridY >= 0 && gridY < displayMatrix.length &&
                            gridX >= 0 && gridX < displayMatrix[0].length) {
                        setRectangleData(brickData[i][j], displayMatrix[gridY][gridX]);
                    }
                }
            }
        }
    }

    public void refreshGameBackground(int[][] board) {
        if (board == null || displayMatrix == null) return;

        this.currentBoardMatrix = copyMatrix(board);

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (i < displayMatrix.length && j < displayMatrix[i].length && displayMatrix[i][j] != null) {
                    setRectangleData(board[i][j], displayMatrix[i][j]);
                }
            }
        }
    }

    private int[][] copyMatrix(int[][] original) {
        if (original == null) return new int[0][0];
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        if (rectangle != null) {
            rectangle.setFill(getFillColor(color));
            rectangle.setArcHeight(5);
            rectangle.setArcWidth(5);
            rectangle.setStroke(Color.GRAY);
            rectangle.setStrokeWidth(0.5);
        }
    }

    private boolean moveDown(MoveEvent event) {
        if (eventListener == null || isPause.get()) return false;

        DownData downData = eventListener.onDownEvent(event);
        if (downData != null) {
            refreshGameView(downData.getViewData());
            updateStatsFromGameController();

            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                refreshGameBackground(downData.getClearRow().getNewMatrix());

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

    private void updateStatsFromGameController() {
        if (eventListener instanceof GameController) {
            GameController gameController = (GameController) eventListener;
            updateStats(gameController.getScore());
        }
    }

    private String getClearMessage(int linesCleared) {
        return switch (linesCleared) {
            case 1 -> "SINGLE";
            case 2 -> "DOUBLE";
            case 3 -> "TRIPLE";
            case 4 -> "TETRIS!";
            default -> linesCleared + " LINES";
        };
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
        if (eventListener instanceof GameController) {
            GameController gameController = (GameController) eventListener;
            bindScore(gameController.getScore().scoreProperty());
            bindScoreProperties(gameController.getScore());
        }
    }

    public void bindScore(IntegerProperty integerProperty) {
        if (scoreLabel != null && integerProperty != null) {
            scoreLabel.textProperty().bind(integerProperty.asString());
        }
    }

    public void bindScoreProperties(Score score) {
        if (score != null) {
            updateStats(score);
        }
    }

    private void updateStats(Score score) {
        if (levelLabel != null) {
            levelLabel.setText(String.valueOf(score.getLevel()));

            // Update game speed when level changes

            if (timeLine != null) {
                timeLine.stop();
                long newDropSpeed = dropSpeed(); // Get the new speed
                timeLine = new Timeline(new KeyFrame(
                        Duration.millis(newDropSpeed),
                        event -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
                ));
                timeLine.setCycleCount(Timeline.INDEFINITE);
                timeLine.play();
            }
        }
        if (linesLabel != null) {
            linesLabel.setText(String.valueOf(score.getTotalLinesCleared()));
        }
        if (comboLabel != null) {
            comboLabel.setText("x" + score.getComboCount());
            if (score.getComboCount() > 1) {
                comboLabel.getStyleClass().removeAll("combo-value", "combo-active");
                comboLabel.getStyleClass().add("combo-active");
            } else {
                comboLabel.getStyleClass().removeAll("combo-value", "combo-active");
                comboLabel.getStyleClass().add("combo-value");
            }
        }
    }

    // Timer methods
    private void startTimer() {
        if (timerTimeLine != null) {
            timerTimeLine.stop();
        }

        startTime = System.currentTimeMillis();
        isTimerRunning = true;

        timerTimeLine = new Timeline(new KeyFrame(
                Duration.millis(100),
                event -> updateTimerDisplay()
        ));
        timerTimeLine.setCycleCount(Timeline.INDEFINITE);
        timerTimeLine.play();
    }

    private void stopTimer() {
        if (timerTimeLine != null) {
            timerTimeLine.stop();
            isTimerRunning = false;
        }
    }

    private void resetTimer() {
        stopTimer();
        elapsedTime = 0;
        if (timerLabel != null) {
            timerLabel.setText("00:00");
        }
    }

    private void updateTimerDisplay() {
        if (isTimerRunning) {
            elapsedTime = System.currentTimeMillis() - startTime;
            long seconds = (elapsedTime / 1000) % 60;
            long minutes = (elapsedTime / (1000 * 60)) % 60;

            String timeString = String.format("%02d:%02d", minutes, seconds);
            if (timerLabel != null) {
                timerLabel.setText(timeString);
            }
        }
    }

    public String getFormattedTime() {
        long seconds = (elapsedTime / 1000) % 60;
        long minutes = (elapsedTime / (1000 * 60)) % 60;
        long hours = (elapsedTime / (1000 * 60 * 60)) % 24;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

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

    public void gameOver(int finalScore) {
        System.out.println("=== GUI CONTROLLER: GAME OVER CALLED ===");

        // Stop the timeline and timer
        if (timeLine != null) {
            timeLine.stop();
            timeLine = null;
        }
        stopTimer();

        // Stop the game controller
        if (eventListener instanceof GameController) {
            GameController gameController = (GameController) eventListener;
            gameController.stopGame();
        }

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
        isGameOver.set(true);
        isHomeScreen.set(false);
    }

    public void newGame() {
        System.out.println("=== STARTING NEW GAME ===");

        // Stop existing game
        if (timeLine != null) {
            timeLine.stop();
            timeLine = null;
        }

        stopTimer();
        resetTimer();

        // Reset game states
        isGameOver.set(false);
        isPause.set(false);
        isHomeScreen.set(false);

        // Hide both containers to show the game
        if (homeContainer != null) {
            homeContainer.setVisible(false);
        }
        if (gameOverContainer != null) {
            gameOverContainer.setVisible(false);
        }

        // Make sure game panel is visible and focused
        if (gamePanel != null) {
            gamePanel.setVisible(true);
            gamePanel.requestFocus();
        }

        if (eventListener == null) {
            eventListener = new GameController(this);
        } else if (eventListener instanceof GameController) {
            GameController gameController = (GameController) eventListener;

            // CALL RESET to get fresh instances
            gameController.reset();

            // THEN create the new game
            gameController.createNewGame();

            updateStats(gameController.getScore());
            initGameView(gameController.getCurrentBoard(), gameController.getViewData());
        }

        // Start new timer
        startTimer();

        // Use level 1 speed explicitly
        long initialSpeed = getSpeedForLevel(1);

        timeLine = new Timeline(new KeyFrame(
                Duration.millis(initialSpeed),
                event -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();

        if (gamePanel != null) {
            gamePanel.requestFocus();
        }
    }

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

    private long getSpeedForLevel(int level) {
        return switch (level) {
            case 1 -> 1500;  // 1.5 seconds
            case 2 -> 1400;  // 1.4 seconds
            case 3 -> 1300;  // 1.3 seconds
            case 4 -> 1200;  // 1.2 seconds
            case 5 -> 1100;  // 1.1 seconds
            case 6 -> 1000;  // 1.0 seconds
            case 7 -> 950;   // 0.95 seconds
            case 8 -> 900;   // 0.9 seconds
            case 9 -> 850;   // 0.85 seconds
            case 10 -> 800;  // 0.8 seconds
            case 11 -> 750;  // 0.75 seconds
            case 12 -> 700;  // 0.7 seconds
            case 13 -> 650;  // 0.65 seconds
            case 14 -> 600;  // 0.6 seconds
            case 15 -> 550;  // 0.55 seconds
            default -> Math.max(500, 550 - (level - 15) * 10);
        };
    }

    public void pauseGame() {
        isPause.set(!isPause.get());
        if (isPause.get()) {
            if (timeLine != null) {
                timeLine.stop();
            }
            stopTimer();
        } else if (!isPause.get() && timeLine != null && !isGameOver.get()) {
            timeLine.play();
            startTimer();
        }
        if (gamePanel != null) {
            gamePanel.requestFocus();
        }
    }

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

        isGameOver.set(false);
        isPause.set(false);
    }
}
