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

        timeline = new Timeline(new KeyFrame(
                Duration.millis(100),
                event -> update()
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void pause() {
        if (!isRunning) return;

        if (timeline != null) {
            timeline.stop();
        }
        isRunning = false;
        // elapsedTime is preserved
    }

    public void resume() {
        if (isRunning) return;
        start();
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        isRunning = false;
    }

    public void reset() {
        stop();
        elapsedTime = 0;
    }

    private void update() {
        elapsedTime = System.currentTimeMillis() - startTime;

        // Notify callback if set
        if (onTickCallback.get() != null) {
            String formattedTime = getFormattedTime();
            onTickCallback.get().call(null);
        }
    }

    public String getFormattedTime() {
        long seconds = (elapsedTime / 1000) % 60;
        long minutes = (elapsedTime / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setOnTickCallback(Callback<Void, String> callback) {
        this.onTickCallback.set(callback);
    }
}