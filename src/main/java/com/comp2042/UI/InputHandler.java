package com.comp2042.UI;

import com.comp2042.core.GameController;
import com.comp2042.model.ViewData;
import com.comp2042.model.events.MoveEvent;
import com.comp2042.model.events.EventSource;
import com.comp2042.model.events.EventType;
import com.comp2042.model.events.InputEventListener;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class InputHandler {
    private InputEventListener eventListener;
    private GuiController guiController;

    public InputHandler(GuiController guiController) {
        this.guiController = guiController;
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void handleKeyPressed(KeyEvent keyEvent, boolean isHomeScreen, boolean isGameOver, boolean isPause) {
        if (eventListener == null) return;

        KeyCode code = keyEvent.getCode();

        // Don't process game keys when on home screen or game over
        if (isHomeScreen || isGameOver) {
            return;
        }

        if (!isPause && !isGameOver) {
            handleGameInput(code, keyEvent);
        }

        handleSystemInput(code);
    }

    private void handleGameInput(KeyCode code, KeyEvent keyEvent) {
        switch (code) {
            case LEFT:
            case A:
                guiController.refreshGameView(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                keyEvent.consume();
                break;
            case RIGHT:
            case D:
                guiController.refreshGameView(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                keyEvent.consume();
                break;
            case UP:
            case W:
                guiController.refreshGameView(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                keyEvent.consume();
                break;
            case DOWN:
            case S:
                guiController.moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                keyEvent.consume();
                break;
            case SPACE:
                if (eventListener instanceof GameController gc) {
                    gc.hardDrop();
                    guiController.refreshGameView(gc.getViewData());
                }
                keyEvent.consume();
                break;
            case C:
                ViewData holdView = eventListener.onHoldEvent(new MoveEvent(EventType.HOLD, EventSource.USER));
                if (holdView != null) {
                    guiController.refreshGameView(holdView);
                }
                keyEvent.consume();
                break;
        }
    }

    private void handleSystemInput(KeyCode code) {
        switch (code) {
            case N:
                guiController.newGame();
                break;
            case H:
                guiController.showHomePage();
                break;
            case ESCAPE:
                guiController.pauseGame();
                break;
        }
    }
}