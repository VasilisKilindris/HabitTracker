package com.example.habittracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HabitsDB";
    private static final int DATABASE_VERSION = 1;

    // Πίνακας 1: Συνήθειες
    private static final String TABLE_HABITS = "habits";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_STREAK = "streak";
    private static final String COLUMN_LAST_COMPLETED = "last_completed";

    // Πίνακας 2: Ιστορικό Ολοκληρώσεων (ΝΕΟΣ ΠΙΝΑΚΑΣ)
    private static final String TABLE_HISTORY = "habit_history";
    private static final String COLUMN_HIST_ID = "hist_id";
    private static final String COLUMN_HIST_HABIT_ID = "habit_id";
    private static final String COLUMN_HIST_DATE = "completion_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Δημιουργία Πίνακα 1
        String createHabitsTable = "CREATE TABLE " + TABLE_HABITS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_STREAK + " INTEGER, " +
                COLUMN_LAST_COMPLETED + " TEXT)";
        db.execSQL(createHabitsTable);

        // Δημιουργία Πίνακα 2 (Ιστορικό)
        String createHistoryTable = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COLUMN_HIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HIST_HABIT_ID + " INTEGER, " +
                COLUMN_HIST_DATE + " TEXT)";
        db.execSQL(createHistoryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }

    public void addHabit(Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, habit.getName());
        values.put(COLUMN_STREAK, habit.getStreak());
        values.put(COLUMN_LAST_COMPLETED, habit.getLastCompletedDate());
        db.insert(TABLE_HABITS, null, values);
        db.close();
    }

    public List<Habit> getAllHabits() {
        List<Habit> habitList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_HABITS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Habit habit = new Habit(cursor.getInt(0),
                                        cursor.getString(1),
                                        cursor.getInt(2),
                                        cursor.getString(3));
                habitList.add(habit);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return habitList;
    }

    public void updateHabit(Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, habit.getName());
        values.put(COLUMN_STREAK, habit.getStreak());
        values.put(COLUMN_LAST_COMPLETED, habit.getLastCompletedDate());
        db.update(TABLE_HABITS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(habit.getId())});
        db.close();
    }

    public void deleteHabit(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HABITS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        // Διαγράφουμε και το ιστορικό αυτής της συνήθειας για να είναι καθαρή η βάση
        db.delete(TABLE_HISTORY, COLUMN_HIST_HABIT_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // --- ΝΕΕΣ ΜΕΘΟΔΟΙ ΓΙΑ ΤΟ ΙΣΤΟΡΙΚΟ ---

    // Καταγραφή μιας ολοκλήρωσης στο ιστορικό
    public void logCompletion(int habitId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HIST_HABIT_ID, habitId);
        values.put(COLUMN_HIST_DATE, date);
        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    // Διαγραφή μιας συγκεκριμένης ολοκλήρωσης από το ιστορικό -  Uncheck
    public void deleteCompletion(int habitId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, COLUMN_HIST_HABIT_ID + " = ? AND " + COLUMN_HIST_DATE + " = ?",
                new String[]{String.valueOf(habitId), date});
        db.close();
    }

    // Διάβασμα των συνηθειών που έγιναν σε μια ΣΥΓΚΕΚΡΙΜΕΝΗ ημερομηνία
    public List<String> getCompletedHabitsForDate(String date) {
        List<String> completedHabits = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT h." + COLUMN_NAME + " FROM " + TABLE_HABITS + " h " +
                "INNER JOIN " + TABLE_HISTORY + " hist ON h." + COLUMN_ID + " = hist." + COLUMN_HIST_HABIT_ID + " " +
                "WHERE hist." + COLUMN_HIST_DATE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{date});
        if (cursor.moveToFirst()) {
            do {
                completedHabits.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return completedHabits;
    }

    // Μέθοδος για ολική διαγραφή όλων των πινάκων (Total Reset)
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HABITS, null, null); // Διαγράφει όλες τις συνήθειες
        db.delete(TABLE_HISTORY, null, null); // Διαγράφει όλο το ιστορικό
        db.close();
    }
}