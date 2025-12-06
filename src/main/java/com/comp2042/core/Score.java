// Manages the scoring system for the Tetris game
// Handles points, levels, combos and special bonuses

package com.comp2042.core;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public final class Score {

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private int level = 1;
    private int totalLinesCleared = 0;
    private int comboCount = 0;
    private boolean lastWasTetris = false;
    private int consecutiveTetrisCount = 0;

    // Gets the score property for JavaFX binding
    // Returns the score property
    public IntegerProperty scoreProperty() {
        return score;
    }

    // Adds points to the current score
    // param i: the number of points to add
    public void add(int i) {
        score.setValue(score.getValue() + i);
    }

    // Resets all scoring values to their initial state
    public void reset() {
        score.setValue(0);
        level = 1;
        totalLinesCleared = 0;
        comboCount = 0;
        lastWasTetris = false;
        consecutiveTetrisCount = 0;
    }

    // Gets the current game level
    // Returns the current level
    public int getLevel() {
        return level;
    }

    // Gets the total number of lines cleared
    // Returns total lines cleared
    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }

    // Gets the current combo count
    // Returns the combo count
    public int getComboCount() {
        return comboCount;
    }

    // Gets the consecutive Tetris count
    // Returns number of consecutive Tetris clears
    public int getConsecutiveTetrisCount() {
        return consecutiveTetrisCount;
    }

    // Checks if back to back Tetris bonus is active
    // Returns true if consecutive Tetris count is greater than 1
    public boolean isBackToBackActive() {
        return consecutiveTetrisCount > 1;
    }

    // Calculate score for line clears (1-4)
    // param linesCleared: number of lines cleared (1-4)
    public void addLineClearScore(int linesCleared) {
        if (linesCleared < 1 || linesCleared > 4) {
            resetCombo();
            return;
        }

        int points = 0;
        switch (linesCleared) {
            case 1:
                points = 100 * level;
                break;    // single
            case 2:
                points = 300 * level;
                break;    // double
            case 3:
                points = 500 * level;
                break;    // triple
            case 4:
                points = 800 * level;
                break;    // tetris
        }

        add(points);
        totalLinesCleared += linesCleared;

        // update level every 10 lines cleared
        level = (totalLinesCleared / 10) + 1;

        // Combo system
        if (linesCleared > 0) {
            addComboPoints();
        }

        System.out.println("Cleared" + linesCleared + " lines! +" + points + " points! Level: " + level);

        // Reset back-to-back tracking for non-Tetris line clears
        if (linesCleared != 4) {
            lastWasTetris = false;
            consecutiveTetrisCount = 0;
        } else {
            lastWasTetris = true;
        }
    }

    // Adds score for a Tetris
    public void addTetrisScore() {
        int basePoints = 800 * level;

        // Back-to-back bonus
        if (lastWasTetris) {
            consecutiveTetrisCount++;
            basePoints = 1200 * level; // Enhanced Tetris points for back-to-back
            System.out.println("Back-to-Back Tetris! Consecutive: " + consecutiveTetrisCount);
        } else {
            consecutiveTetrisCount = 1;
        }

        add(basePoints);
        totalLinesCleared += 4;
        level = (totalLinesCleared / 10) + 1;

        // Combo system
        addComboPoints();

        System.out.println("Tetris! +" + basePoints + " points! Level: " + level);

        lastWasTetris = true;
    }

    // Combo system
    // Adds combo points based on current combo count
    private void addComboPoints() {
        if (comboCount > 0) {
            int comboPoints = 50 * level * comboCount;
            add(comboPoints);
            System.out.println("Combo x" + comboCount + "! +" + comboPoints + " points!");
        }
        comboCount++;
    }

    // Resets the combo counter
    public void resetCombo() {
        if (comboCount > 1) {
            System.out.println("Combo broken! Reached x" + (comboCount - 1) + " combo");
        }
        comboCount = 0;
    }

    // Perfect clear bonus
    // Adds bonus points for a perfect clear (empty board)
    public void addPerfectClear() {
        int perfectClearBonus = 2000 * level;
        add(perfectClearBonus);
        System.out.println("Perfect Clear! +" + perfectClearBonus + " points!");

        // Combo system applies to perfect clear as well
        if (comboCount > 0) {
            addComboPoints();
        }
    }

    // Add points for hard drop (2 points per row dropped)
    // param dropDistance: number of rows dropped
    public void addHardDropScore(int dropDistance) {
        if (dropDistance > 0) {
            int points = dropDistance * 2;
            add(points);
            System.out.println("Hard drop: + " + points + " points (" + dropDistance + " rows)");
        }
    }

    // Add points for soft drop (1 point per row, called continuously during soft drop)
    // param dropDistance: number of rows soft dropped
    public  void addSoftDropScore(int dropDistance) {
        if (dropDistance > 0) {
            int points = dropDistance * 1;
            add(points);
        }
    }

    // Helper method to be called when a piece is placed without clearing lines
    public void piecePlacedWithoutClear() {
        resetCombo();
    }
}
