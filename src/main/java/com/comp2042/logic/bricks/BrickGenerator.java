// Interface for generating Tetris bricks
package com.comp2042.logic.bricks;

public interface BrickGenerator {

    // Gets the next brick to drop
    // Returns a Brick object
    Brick getBrick();

    // Peeks at the upcoming brick without removing it
    // Returns the next Brick that will be generated
    Brick getNextBrick();
}
