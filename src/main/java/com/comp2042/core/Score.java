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

    public IntegerProperty scoreProperty() {
        return score;
    }

    public void add(int i) {
        score.setValue(score.getValue() + i);
    }

    public void reset() {
        score.setValue(0);
        level = 1;
        totalLinesCleared = 0;
        comboCount = 0;
        lastWasTetris = false;
        consecutiveTetrisCount = 0;
    }

    public int getLevel() {
        return level;
    }

    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }

    public int getComboCount() {
        return comboCount;
    }

    public int getConsecutiveTetrisCount() {
        return consecutiveTetrisCount;
    }

    public boolean isBackToBackActive() {
        return consecutiveTetrisCount > 1;
    }

    // Calculate score for line clears (1-4)
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
    private void addComboPoints() {
        if (comboCount > 0) {
            int comboPoints = 50 * level * comboCount;
            add(comboPoints);
            System.out.println("Combo x" + comboCount + "! +" + comboPoints + " points!");
        }
        comboCount++;
    }

    public void resetCombo() {
        if (comboCount > 1) {
            System.out.println("Combo broken! Reached x" + (comboCount - 1) + " combo");
        }
        comboCount = 0;
    }

    // Perfect clear bonus
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
    public void addHardDropScore(int dropDistance) {
        if (dropDistance > 0) {
            int points = dropDistance * 2;
            add(points);
            System.out.println("Hard drop: + " + points + " points (" + dropDistance + " rows)");
        }
    }

    // Add points for soft drop (1 point per row, called continuously during soft drop)
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
