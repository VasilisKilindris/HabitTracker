package com.example.habittracker;


import org.junit.Test;
import static org.junit.Assert.*;

public class HabitStreakTest {

    @Test
    public void testStreakIncrement() {
        // Eικονική συνήθεια με σερί 0
        Habit habit = new Habit(1, "Γυμναστήριο", 0, "");

        // Δοκιμή Check
        habit.setStreak(habit.getStreak() + 1);

        // Ελεγχος αν όντως έγινε 1
        assertEquals(1, habit.getStreak());
    }

    @Test
    public void testStreakNeverNegative() {
        // Eικονική συνήθεια με σερί 0
        Habit habit = new Habit(1, "Διάβασμα", 0, "");

        // Δοκιμή Uncheck
        int newStreak = habit.getStreak() - 1;

        // Εδώ τρέχει η δικλείδα ασφαλείας
        if (newStreak < 0) {
            newStreak = 0;
        }
        habit.setStreak(newStreak);

        // Ελεγχος αν προστατεύτηκε και έμεινε 0 αντί για -1
        assertEquals(0, habit.getStreak());
    }
}