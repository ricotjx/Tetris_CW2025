package com.comp2042.UI;

import com.comp2042.model.*;
import com.comp2042.core.Score;
import com.comp2042.model.events.EventSource;
import com.comp2042.model.events.EventType;
import com.comp2042.model.events.InputEventListener;
import com.comp2042.model.events.MoveEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
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
    @FXML
    private Label scoreLabel;

    @FXML
    private GridPane holdPanel;

    @FXML
    private Label levelLabel;

    private GameOverPanel gameOverPanel;

    private Rectangle[][] displayMatrix;

    private InputEventListener eventListener;

    private Rectangle[][] rectangles;

    private Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty();

    private final BooleanProperty isGameOver = new SimpleBooleanProperty();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }
                }
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }
            }
        });
        gameOverPanel.setVisible(false);
        if (holdPanel != null) {
            holdPanel.getChildren().clear();
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
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }
        brickPanel.setLayoutX(gamePanel.getLayoutX() + brick.getxPosition() * brickPanel.getVgap() + brick.getxPosition() * BRICK_SIZE);
        brickPanel.setLayoutY(-42 + gamePanel.getLayoutY() + brick.getyPosition() * brickPanel.getHgap() + brick.getyPosition() * BRICK_SIZE);


        timeLine = new Timeline(new KeyFrame(
                Duration.millis(400),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
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
        return returnPaint;
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
        for (int i = 2; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(getFillColor(color));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    private void moveDown(MoveEvent event) {
        if (isPause.getValue() == Boolean.FALSE) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
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

    }

    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.setValue(Boolean.TRUE);
    }

    public void newGame(ActionEvent actionEvent) {
        timeLine.stop();
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
    }

    public void pauseGame(ActionEvent actionEvent) {
        gamePanel.requestFocus();
    }
}
