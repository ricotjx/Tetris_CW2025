// Implementation of the Board interface that manages the game state
// Incldes the current falling brick, hold functionality and game board matrix

package com.comp2042.core;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;
import com.comp2042.logic.MatrixOperations;
import com.comp2042.model.BrickRotator;
import com.comp2042.model.ClearRow;
import com.comp2042.model.NextShapeInfo;
import com.comp2042.model.ViewData;

import java.awt.*;

public class SimpleBoard implements Board {

    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private int[][] currentGameMatrix;
    private Point currentOffset;
    private Brick holdBrick = null;
    private boolean holdUsedThisTurn = false;
    private Brick currentBrick;

    // Constructs a SimpleBoard with specified dimensions
    // param width: the width of the game board in cells
    // param height: the height of the game board in cells
    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        currentGameMatrix = new int[height][width];
        brickGenerator = new RandomBrickGenerator();
        brickRotator = new BrickRotator();

        // Initialize with empty board
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                currentGameMatrix[i][j] = 0;
            }
        }
    }

    // Moves the current brick down by one unit
    // Returns true if move was successful, false if blocked
    @Override
    public boolean moveBrickDown() {
        if (currentBrick == null) return false;

        Point newOffset = new Point(currentOffset);
        newOffset.translate(0, 1);

        boolean conflict = MatrixOperations.intersect(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                (int) newOffset.getX(),
                (int) newOffset.getY()
        );

        if (!conflict) {
            currentOffset = newOffset;
            return true;
        }
        return false;
    }

    // Moves the current brick left by one unit
    // Returns true if move was successful, false if blocked
    @Override
    public boolean moveBrickLeft() {
        if (currentBrick == null) return false;

        Point newOffset = new Point(currentOffset);
        newOffset.translate(-1, 0);

        boolean conflict = MatrixOperations.intersect(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                (int) newOffset.getX(),
                (int) newOffset.getY()
        );

        if (!conflict) {
            currentOffset = newOffset;
            return true;
        }
        return false;
    }

    // Moves the current brick right by one unit
    // Returns true if move was successful, false if blocked
    @Override
    public boolean moveBrickRight() {
        if (currentBrick == null) return false;

        Point newOffset = new Point(currentOffset);
        newOffset.translate(1, 0);

        boolean conflict = MatrixOperations.intersect(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                (int) newOffset.getX(),
                (int) newOffset.getY()
        );

        if (!conflict) {
            currentOffset = newOffset;
            return true;
        }
        return false;
    }

    // Rotates the current brick counterclockwise
    // Returns true if rotation was successful, false if blocked
    @Override
    public boolean rotateLeftBrick() {
        if (currentBrick == null) return false;

        NextShapeInfo nextShape = brickRotator.getNextShape();
        boolean conflict = MatrixOperations.intersect(
                currentGameMatrix,
                nextShape.getShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );

        if (!conflict) {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
        return false;
    }

    // Creates a new brick at the top of the board
    // Returns true if game over (collision at spawn), false otherwise
    @Override
    public boolean createNewBrick() {
        // Reset hold flag for new piece
        holdUsedThisTurn = false;

        currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);

        // Start at top center - adjust based on brick width
        int brickWidth = brickRotator.getCurrentShape()[0].length;
        currentOffset = new Point(width / 2 - brickWidth / 2, 0);

        // Check if game over (collision at spawn)
        boolean collision = MatrixOperations.intersect(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );

        return collision; // true = game over
    }

    // Gets the current board matrix
    // Returns 2D array representing the game board
    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    // Gets view data for rendering
    // Returns ViewData containing current brick, next brick and hold brick
    @Override
    public ViewData getViewData() {
        int[][] holdMatrix = holdBrick != null ? holdBrick.getShapeMatrix().get(0) : null;
        int[][] nextBrickMatrix = brickGenerator.getNextBrick().getShapeMatrix().get(0);

        return new ViewData(
                brickRotator.getCurrentShape(),
                (int) currentOffset.x,
                (int) currentOffset.y,
                nextBrickMatrix,
                holdMatrix
        );
    }

    // Gets the matrix representation of the held brick
    // Returns 2D array of the held brick shape or null if no brick is held
    public int[][] getHoldBrickMatrix() {
        return holdBrick != null ? holdBrick.getShapeMatrix().get(0) : null;
    }

    // Holds the current brick for later use
    // Swaps current brick with held brick
    // Can only be used once per turn
    @Override
    public void holdCurrentBrick() {
        if (holdUsedThisTurn || currentBrick == null) {
            return;
        }

        if (holdBrick == null) {
            // First hold - store current and get new brick
            holdBrick = currentBrick;
            createNewBrick();
        } else {
            // Swap current with held brick
            Brick temp = currentBrick;
            currentBrick = holdBrick;
            holdBrick = temp;

            brickRotator.setBrick(currentBrick);

            // Reset position
            int brickWidth = brickRotator.getCurrentShape()[0].length;
            currentOffset = new Point(width / 2 - brickWidth / 2, 0);
        }

        holdUsedThisTurn = true;
    }

    // Performs a hard drop (instant placement) of the current brick
    @Override
    public void hardDrop() {
        if (currentBrick == null) return;

        while (moveBrickDown()) {
            // Keep moving down until can't
        }

        mergeBrickToBackground();
        clearRows();
        createNewBrick();
    }

    // Merges the current brick into the background board
    // Called when a brick reaches its final position
    @Override
    public void mergeBrickToBackground() {
        if (currentBrick == null) return;

        currentGameMatrix = MatrixOperations.merge(
                currentGameMatrix,
                brickRotator.getCurrentShape(),
                (int) currentOffset.getX(),
                (int) currentOffset.getY()
        );
    }

    // Clears completed rows from the board
    // Returns ClearRow object containing cleared rows data
    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        return clearRow;
    }

    // Gets the score object
    // Returns Score object for tracking points
    @Override
    public Score getScore() {
        return null;
    }

    // Starts a new game
    @Override
    public void newGame() {
        reset();
        createNewBrick();
    }

    // Resets the board to initial state
    @Override
    public void reset() {
        System.out.println("=== SIMPLEBOARD RESET ===");

        // Clear the board matrix
        currentGameMatrix = new int[height][width];

        // Reset game state
        holdBrick = null;
        holdUsedThisTurn = false;
        currentBrick = null;

        System.out.println("Board reset complete");
    }
}