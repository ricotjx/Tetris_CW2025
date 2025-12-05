package com.comp2042.UI;

import com.comp2042.core.Score;
import com.comp2042.model.ViewData;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class GameRenderer {
    private static final int BRICK_SIZE = 20;

    private GridPane gamePanel;
    private GridPane brickPanel;
    private GridPane holdPanel;
    private Group groupNotification;
    private Label scoreLabel;
    private Label levelLabel;
    private Label linesLabel;
    private Label comboLabel;
    private Label timerLabel;

    private Rectangle[][] displayMatrix;
    private Rectangle[][] nextBrickRectangles;
    private int[][] currentBoardMatrix;

    public GameRenderer(GridPane gamePanel, GridPane brickPanel, GridPane holdPanel,
                        Group groupNotification, Label scoreLabel, Label levelLabel,
                        Label linesLabel, Label comboLabel, Label timerLabel) {
        this.gamePanel = gamePanel;
        this.brickPanel = brickPanel;
        this.holdPanel = holdPanel;
        this.groupNotification = groupNotification;
        this.scoreLabel = scoreLabel;
        this.levelLabel = levelLabel;
        this.linesLabel = linesLabel;
        this.comboLabel = comboLabel;
        this.timerLabel = timerLabel;
    }

    // Initializes the game view with the current board state
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
    }

    // Refreshes the entire game view with updated data
    public void refreshGameView(ViewData viewData) {
        if (viewData == null) return;

        refreshGameBackground(currentBoardMatrix);
        drawFallingBrick(viewData);
        updateNextBrickPreview(viewData);
        updateHoldBrickPreview(viewData.getHoldBrickData());
    }

    // Updates only the game background (placed bricks)
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

    // Draws the currently falling brick
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

    // Updates the next brick preview panel
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

            // Center the next brick in the preview panel
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

    // Updates the hold brick preview panel
    public void updateHoldBrickPreview(int[][] holdBrick) {
        if (holdPanel == null) return;

        holdPanel.getChildren().clear();

        if (holdBrick == null) {
            // Show "HOLD" placeholder when no brick is held
            Label holdLabel = new Label("HOLD");
            holdLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-text-fill: gray;");
            holdLabel.setAlignment(javafx.geometry.Pos.CENTER);
            holdPanel.add(holdLabel, 1, 1, 2, 2);
            return;
        }

        int rows = holdBrick.length;
        int cols = holdBrick[0].length;

        int panelRows = 4;
        int panelCols = 4;

        // Center the brick in the hold panel
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

    // Updates all score and statistic displays
    public void updateStats(Score score) {
        // level
        if (levelLabel != null) {
            levelLabel.setText(String.valueOf(score.getLevel()));
        }
        // lines cleared
        if (linesLabel != null) {
            linesLabel.setText(String.valueOf(score.getTotalLinesCleared()));
        }
        // combo
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

    // Clears the hold brick preview panel
    public void clearHoldPreview() {
        updateHoldBrickPreview(null);
    }
}