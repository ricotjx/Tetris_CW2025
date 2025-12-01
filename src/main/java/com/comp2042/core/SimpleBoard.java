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
    private final Score score;
    private Brick holdBrick = null;
    private boolean holdUsedThisTurn = false;
    private Brick currentBrick;

    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        currentGameMatrix = new int[height][width];  // Note: [height][width] for row-major
        brickGenerator = new RandomBrickGenerator();
        brickRotator = new BrickRotator();
        score = new Score();

        // Initialize with empty board
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                currentGameMatrix[i][j] = 0;
            }
        }
    }

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

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

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

    public int[][] getHoldBrickMatrix() {
        return holdBrick != null ? holdBrick.getShapeMatrix().get(0) : null;
    }

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

    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        return clearRow;
    }

    @Override
    public Score getScore() {
        return score;
    }

    @Override
    public void newGame() {
        currentGameMatrix = new int[height][width];
        holdBrick = null;
        holdUsedThisTurn = false;
        currentBrick = null;
        score.reset();
        createNewBrick();
    }
}