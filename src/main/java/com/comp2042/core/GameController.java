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
    private boolean is40LinesMode = false;
    private int linesClearedInMode = 0;
    private boolean isTimeLimitMode = false;
    private long gameStartTime = 0;
    private static final long TIME_LIMIT_MS = 120_000;  // 2 minutes = 120,000 ms

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
        this.is40LinesMode = false;
        this.linesClearedInMode = 0;
        this.isTimeLimitMode = false;
        this.gameStartTime = 0;
    }

    @Override
    public DownData onDownEvent(MoveEvent event) {
        // Don't process game events if game hasn't started
        if (!gameStarted || gameEnded) {
            return null;
        }

        // Check time limit if in time limit mode
        if (isTimeLimitMode && isTimeLimitReached()) {
            System.out.println("TIME LIMIT REACHED!");
            onGameOver();
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

        // Check time limit if in time limit mode
        if (isTimeLimitMode && isTimeLimitReached()) {
            System.out.println("TIME LIMIT REACHED (hard drop)!");
            onGameOver();
            return;
        }

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

        // Track lines for 40 lines mode
        if (is40LinesMode) {
            linesClearedInMode += linesCleared;
            System.out.println("40 Lines mode progress: " + linesClearedInMode + "/40 lines cleared");

            // Check if 40 lines reached
            if (linesClearedInMode >= 40) {
                System.out.println("40 LINES COMPLETED! Calling onGameOver()...");
                onGameOver();
            }
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

        // Record start time for time limit mode
        if (isTimeLimitMode) {
            gameStartTime = System.currentTimeMillis();
            System.out.println("Time limit mode started at: " + gameStartTime);
        }

        // Refresh the view
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
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

    // Set 40 lines mode
    public void set40LinesMode(boolean enabled) {
        this.is40LinesMode = enabled;
        this.linesClearedInMode = 0;
        System.out.println("40 Lines mode set to: " + enabled);
    }

    // Get current mode
    public boolean is40LinesMode() {
        return is40LinesMode;
    }

    public void setTimeLimitMode(boolean enabled) {
        this.isTimeLimitMode = enabled;
        this.gameStartTime = 0;
        System.out.println("Time Limit mode set to: " + enabled);
    }

    public boolean isTimeLimitMode() {
        return isTimeLimitMode;
    }

    // Check time limit
    private boolean isTimeLimitReached() {
        if (!isTimeLimitMode || gameStartTime == 0) return false;

        long elapsedTime = System.currentTimeMillis() - gameStartTime;
        return elapsedTime >= TIME_LIMIT_MS;
    }
}