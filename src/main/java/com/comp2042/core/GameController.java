package com.comp2042.core;

import com.comp2042.model.ClearRow;
import com.comp2042.model.DownData;
import com.comp2042.model.ViewData;
import com.comp2042.model.events.MoveEvent;
import com.comp2042.model.events.EventSource;
import com.comp2042.model.events.InputEventListener;
import com.comp2042.UI.GuiController;

public class GameController implements InputEventListener {

    private Board board = new SimpleBoard(10, 20);
    private final GuiController viewGuiController;
    private Score score = new Score();
    private int hardDropDistance = 0;
    private boolean isSoftDropping = false;
    private boolean gameStarted = false;
    private boolean gameEnded = false;

    public GameController(GuiController c) {
        System.out.println("=== GAME CONTROLLER CONSTRUCTOR ===");
        viewGuiController = c;
        initializeGameState();
        viewGuiController.setEventListener(this);
        viewGuiController.bindScore(score.scoreProperty());
        viewGuiController.setOnRestartGame(this::createNewGame);
    }

    private void initializeGameState() {
        // Reset without creating brick
        score.reset();
        this.hardDropDistance = 0;
        this.isSoftDropping = false;
        this.gameStarted = false;
        this.gameEnded = false;
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        // Don't process game events if game hasn't started
        if (!gameStarted || gameEnded) {
            return null;
        }

        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;

        if (event.getEventSource() == EventSource.USER && canMove) {
            score.addSoftDropScore(1);
            isSoftDropping = true;
        }

        if (!canMove) {
            board.mergeBrickToBackground();

            if (hardDropDistance > 0) {
                score.addHardDropScore(hardDropDistance);
                hardDropDistance = 0;
            }

            clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                handleAdvancedScoring(clearRow.getLinesRemoved());
            } else {
                score.piecePlacedWithoutClear();
            }

            if (isPerfectClear()) {
                score.addPerfectClear();
            }

            if (board.createNewBrick()) {
                onGameOver();
                return null;
            }

            viewGuiController.refreshGameBackground(board.getBoardMatrix());
            isSoftDropping = false;
        }
        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        if (!gameStarted || gameEnded) return board.getViewData();
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        if (!gameStarted || gameEnded) return board.getViewData();
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        if (!gameStarted || gameEnded) return board.getViewData();
        board.rotateLeftBrick();
        return board.getViewData();
    }

    @Override
    public ViewData onHoldEvent(MoveEvent event) {
        if (!gameStarted || gameEnded) return board.getViewData();
        if (board instanceof SimpleBoard simpleBoard) {
            simpleBoard.holdCurrentBrick();
            return simpleBoard.getViewData();
        }
        return null;
    }

    public void hardDrop() {
        if (!gameStarted || gameEnded) return;

        hardDropDistance = 0;
        while (board.moveBrickDown()) {
            hardDropDistance++;
        }

        board.mergeBrickToBackground();
        ClearRow clearRow = board.clearRows();
        if (clearRow.getLinesRemoved() > 0) {
            handleAdvancedScoring(clearRow.getLinesRemoved());
        } else {
            score.piecePlacedWithoutClear();
        }

        if (isPerfectClear()) {
            score.addPerfectClear();
        }

        boolean collisionAtSpawn = board.createNewBrick();
        if (collisionAtSpawn) {
            onGameOver();
            return;
        }

        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }

    private void handleAdvancedScoring(int linesCleared) {
        if (linesCleared == 4) {
            score.addTetrisScore();
        } else {
            score.addLineClearScore(linesCleared);
        }
    }

    private boolean isPerfectClear() {
        int[][] boardMatrix = board.getBoardMatrix();
        for (int y = 0; y < boardMatrix.length; y++) {
            for (int x = 0; x < boardMatrix[y].length; x++) {
                if (boardMatrix[y][x] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public ViewData getViewData() {
        return board.getViewData();
    }

    public void reset() {
        // Stop any ongoing game logic
        stopGame();

        // Reset the board
        board.newGame();

        // Reset the score
        score.reset();

        // Reset all game state variables
        this.hardDropDistance = 0;
        this.isSoftDropping = false;
        this.gameStarted = false;
        this.gameEnded = false;
    }

    @Override
    public void createNewGame() {
        board.newGame();

        gameStarted = true;
        gameEnded = false;

        // Refresh the view
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }

    private boolean boardHasBrick() {
        // Check if there's a current brick in the board
        // You might need to add a method to Board interface
        return getViewData().getBrickData() != null;
    }

    public Score getScore() {
        return score;
    }

    public int[][] getCurrentBoard() {
        return board.getBoardMatrix();
    }

    public void onGameOver() {
        gameEnded = true;
        gameStarted = false;
        int finalScore = score.scoreProperty().get();
        viewGuiController.gameOver(finalScore);
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public void stopGame() {
        gameStarted = false;
        gameEnded = true;
    }
}