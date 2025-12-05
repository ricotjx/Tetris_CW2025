package com.comp2042.UI;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;
import javafx.util.Callback;

public class GameTimer {
    private Timeline timeline;
    private long startTime;
    private long elapsedTime = 0;
    private boolean isRunning = false;
    private ObjectProperty<Callback<Void, String>> onTickCallback = new SimpleObjectProperty<>();

    public GameTimer() {
        // Initialize empty
    }

    public void start() {
        if (isRunning) return;

        startTime = System.currentTimeMillis() - elapsedTime;
        isRunning = true;

        // Create timeline that updates every 100ms
        timeline = new Timeline(new KeyFrame(
                Duration.millis(100),
                event -> update()
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // Pauses the timer while preserving elapsed time
    public void pause() {
        if (!isRunning) return;

        if (timeline != null) {
            timeline.stop();
        }
        isRunning = false;
        // elapsedTime is preserved
    }

    // Resumes the timer from where it was paused
    public void resume() {
        if (isRunning) return;
        start();
    }

    // Stops the timer
    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        isRunning = false;
    }

    // Resets the timer to zero and stops it
    public void reset() {
        stop();
        elapsedTime = 0;
    }

    // Updates elapsed time and trigger callback if set
    private void update() {
        elapsedTime = System.currentTimeMillis() - startTime;

        // Notify callback if set
        if (onTickCallback.get() != null) {
            String formattedTime = getFormattedTime();
            onTickCallback.get().call(null);
        }
    }

    // Gets the elapsed time formatted as MM:SS
    public String getFormattedTime() {
        long seconds = (elapsedTime / 1000) % 60;
        long minutes = (elapsedTime / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Gets the elapsed time in milliseconds
    public long getElapsedTime() {
        return elapsedTime;
    }

    // Checks if the timer is currently running
    public boolean isRunning() {
        return isRunning;
    }

    // Sets a callback to be executed on each timer tick (every 100ms)
    public void setOnTickCallback(Callback<Void, String> callback) {
        this.onTickCallback.set(callback);
    }
}