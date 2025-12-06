// Main controller class that manages the Tetris game flow
// Handles user input, game logic, scoring and different game modes
// Acts as the bridge between the game model and the user interface

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

    // Constructs a GameController with the specified GUI controller
    public GameController(GuiController c) {
        System.out.println("=== GAME CONTROLLER CONSTRUCTOR ===");
        viewGuiController = c;
        initializeGameState();
        viewGuiController.setEventListener(this);
        viewGuiController.bindScore(score.scoreProperty());
        viewGuiController.setOnRestartGame(this::createNewGame);
    }

    // Initializes all game state variables to their default values
    // Called when starting a new game
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

    // Handles down movement events
    // Returns DownData containing cleared rows and view data or null if game ended
    // param event: the move event containing event source information
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

    // Handles left movement events
    // Returns updated ViewData after the move
    // param event: the move event
    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        if (!gameStarted || gameEnded) return board.getViewData();
        board.moveBrickLeft();
        return board.getViewData();
    }

    // Handles right movement events
    // Returns updated ViewData after the move
    // param event: the move event
    @Override
    public ViewData onRightEvent(MoveEvent event) {
        if (!gameStarted || gameEnded) return board.getViewData();
        board.moveBrickRight();
        return board.getViewData();
    }

    // Handles rotation events
    // Returns updated ViewData after the rotation
    // param event: the move event
    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        if (!gameStarted || gameEnded) return board.getViewData();
        board.rotateLeftBrick();
        return board.getViewData();
    }

    // Handles hold brick events
    // Returns updated ViewData after holding brick
    // param event: the move event
    @Override
    public ViewData onHoldEvent(MoveEvent event) {
        if (!gameStarted || gameEnded) return board.getViewData();
        if (board instanceof SimpleBoard simpleBoard) {
            simpleBoard.holdCurrentBrick();
            return simpleBoard.getViewData();
        }
        return null;
    }

    // Performs a hard drop operation (instant brick placement)
    // Moves brick to lowest possible position and processes the result
    public void hardDrop() {
        if (!gameStarted || gameEnded) return;

        // Check time limit if in time limit mode
        if (isTimeLimitMode && isTimeLimitReached()) {
            System.out.println("TIME LIMIT REACHED (hard drop)!");
            onGameOver();
            return;
        }

        // Reset hardDropDistance
        hardDropDistance = 0;

        // Count how many rows we can drop
        while (board.moveBrickDown()) {
            hardDropDistance++;
        }

        // Merge the brick and process the result
        board.mergeBrickToBackground();
        ClearRow clearRow = board.clearRows();

        // Add hard drop points
        if (hardDropDistance > 0) {
            score.addHardDropScore(hardDropDistance);
            hardDropDistance = 0;   // Reset after adding points
        }

        // If hard drop clears line(s), use advanced scoring
        if (clearRow.getLinesRemoved() > 0) {
            handleAdvancedScoring(clearRow.getLinesRemoved());
        } else {
            score.piecePlacedWithoutClear();
        }

        // If perfect clear, add perfect clear score
        if (isPerfectClear()) {
            score.addPerfectClear();
        }

        boolean collisionAtSpawn = board.createNewBrick();
        if (collisionAtSpawn) {
            onGameOver();
            return;
        }

        viewGuiController.refreshGameBackground(board.getBoardMatrix());

        // Reset hardDropDistance after use
        hardDropDistance = 0;
    }

    // Handles advanced scoring calculations for line clears
    // param linesCleared: number of lines cleared (1-4)
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

    // Checks if the board is completely empty (perfect clear)
    // Returns true if board has no blocks, false otherwise
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

    // Gets the current view data for rendering
    // Returns ViewData containing current game state
    public ViewData getViewData() {
        return board.getViewData();
    }

    // Resets the game to initial state
    // Stops current game and clears all game data
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

    // Starts a new game
    // Resets board, score and begins game with first brick
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

    // Gets the score object
    // Returns the Score object for this game
    public Score getScore() {
        return score;
    }

    // Gets the current board matrix
    // Returns 2D array representing the current game board
    public int[][] getCurrentBoard() {
        return board.getBoardMatrix();
    }

    // Handles game over condition
    // Stops the game and displays final score
    public void onGameOver() {
        gameEnded = true;
        gameStarted = false;
        int finalScore = score.scoreProperty().get();
        viewGuiController.gameOver(finalScore);
    }

    // Checks if the game has started
    // Returns true if game is in progress, false otherwise
    public boolean isGameStarted() {
        return gameStarted;
    }

    // Checks if the game has ended
    // Returns true if game has ended, false otherwise
    public boolean isGameEnded() {
        return gameEnded;
    }

    // Stops the game
    // Used for pausing or force stopping the game
    public void stopGame() {
        gameStarted = false;
        gameEnded = true;
    }

    // Set 40 lines mode
    // param enabled: true to enable 40 lines mode, false to disable
    public void set40LinesMode(boolean enabled) {
        this.is40LinesMode = enabled;
        this.linesClearedInMode = 0;
        System.out.println("40 Lines mode set to: " + enabled);
    }

    // Checks if 40 lines mode is enabled
    // Returns true if 40 lines mode is active, false otherwise
    public boolean is40LinesMode() {
        return is40LinesMode;
    }

    // Set time limit mode
    // param enabled: true to enable time limit mode, false to disable
    public void setTimeLimitMode(boolean enabled) {
        this.isTimeLimitMode = enabled;
        this.gameStartTime = 0;
        System.out.println("Time Limit mode set to: " + enabled);
    }

    // Checks if time limit mode is enabled
    // Returns true if time limit mode is active, false otherwise
    public boolean isTimeLimitMode() {
        return isTimeLimitMode;
    }

    // Checks if the time limit has been reached in time limit mode
    // Returns true if time limit (2 mins) has been reached, false otherwise
    private boolean isTimeLimitReached() {
        if (!isTimeLimitMode || gameStartTime == 0) return false;

        long elapsedTime = System.currentTimeMillis() - gameStartTime;
        return elapsedTime >= TIME_LIMIT_MS;
    }
}