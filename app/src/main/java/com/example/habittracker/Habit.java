package com.example.habittracker;

public class Habit {
    private int id;
    private String name;
    private int streak;
    private String lastCompletedDate;

    // 1ος Κατασκευαστής (Όταν διαβάζουμε μια συνήθεια από τη βάση δεδομένων)
    public Habit(int id, String name, int streak, String lastCompletedDate) {
        this.id = id;
        this.name = name;
        this.streak = streak;
        this.lastCompletedDate = lastCompletedDate;
    }

    // 2ος Κατασκευαστής (Για καίνουργια συνήθεια)
    public Habit(String name) {
        this.name = name;
        this.streak = 0;
        this.lastCompletedDate = "";
    }

    // Getters και Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public String getLastCompletedDate() { return lastCompletedDate; }
    public void setLastCompletedDate(String lastCompletedDate) { this.lastCompletedDate = lastCompletedDate; }
}
