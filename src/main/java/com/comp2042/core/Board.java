// Interface defining the contract for a Tetris game board
// Provides methods for game mechanics and state management

package com.comp2042.core;

import com.comp2042.model.ClearRow;
import com.comp2042.model.ViewData;

public interface Board {

    // Moves the current brick down by one unit
    // Returns true if move was successful, false if blocked
    boolean moveBrickDown();

    // Moves the current brick left by one unit
    // Returns true if move was successful, false if blocked
    boolean moveBrickLeft();

    // Moves the current brick right by one unit
    // Returns true if move was successful, false if blocked
    boolean moveBrickRight();

    // Rotates he current brick counterclockwise
    // Returns true if rotation was successful, false if blocked
    boolean rotateLeftBrick();

    // Creates a new brick at the top of the board
    // Returns true if game over (collision at spawn), false otherwise
    boolean createNewBrick();

    // Gets the current board matrix
    // Returns 2D array representing the game board
    int[][] getBoardMatrix();

    // Gets view data for rendering
    // Returns ViewData containing current brick, next brick and hold brick
    ViewData getViewData();

    // Merges the current brick into the background board
    void mergeBrickToBackground();

    // Clears completed rows from the board
    // Returns ClearRow object containing cleared rows data
    ClearRow clearRows();

    // Gets the score object
    // Returns Score object for tracking points
    Score getScore();

    // Starts a new game
    void newGame();

    // Holds the current brick for later use
    void holdCurrentBrick();

    // Performs a hard drop (instant placement) of the current brick
    void hardDrop();

    // Resets the board to initial state
    void reset();
}
