package com.comp2042.UI;

import com.comp2042.core.GameController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class GameStateManager {
    private final BooleanProperty isPause = new SimpleBooleanProperty(false);
    private final BooleanProperty isGameOver = new SimpleBooleanProperty(false);
    private final BooleanProperty isHomeScreen = new SimpleBooleanProperty(true);

    private GameController gameController;

    // Property getters for binding
    public BooleanProperty isPauseProperty() { return isPause; }
    public BooleanProperty isGameOverProperty() { return isGameOver; }
    public BooleanProperty isHomeScreenProperty() { return isHomeScreen; }

    // Value getters
    public boolean isPause() { return isPause.get(); }
    public boolean isGameOver() { return isGameOver.get(); }
    public boolean isHomeScreen() { return isHomeScreen.get(); }

    // Property setters
    public void setPause(boolean pause) { isPause.set(pause); }
    public void setGameOver(boolean gameOver) { isGameOver.set(gameOver); }
    public void setHomeScreen(boolean homeScreen) { isHomeScreen.set(homeScreen); }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    // Transitions to new game state
    public void startNewGame() {
        isGameOver.set(false);
        isPause.set(false);
        isHomeScreen.set(false);
    }

    // Transitions to game over state and stops the game controller
    public void gameOver() {
        isGameOver.set(true);
        isHomeScreen.set(false);
        isPause.set(false);

        if (gameController != null) {
            gameController.stopGame();
        }
    }

    // Toggle pause state
    public void togglePause() {
        isPause.set(!isPause.get());
    }

    // Pauses the game
    public void pause() {
        isPause.set(true);
    }

    // Resumes the game
    public void resume() {
        isPause.set(false);
    }

    // Returns to home screen (main menu)
    public void goHome() {
        isHomeScreen.set(true);
        isGameOver.set(false);
        isPause.set(false);
    }

    // Resets all states to default
    public void reset() {
        isPause.set(false);
        isGameOver.set(false);
        isHomeScreen.set(true);
    }

    // Checks if game input should be processed
    public boolean canProcessGameInput() {
        return !isHomeScreen() && !isGameOver() && !isPause();
    }

    // Checks if a game is currently active
    public boolean isGameActive() {
        return !isHomeScreen() && !isGameOver();
    }

    // Stops the game and transitions to game over state
    public void stopGame() {
        if (gameController != null) {
            gameController.stopGame();
        }
        gameOver();
    }
}