package com.example.habittracker;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvCompletedHabitsList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new DatabaseHelper(this);

        calendarView = findViewById(R.id.calendarView);
        tvCompletedHabitsList = findViewById(R.id.tvCompletedHabitsList);

        // Εμφάνιση των επιτευγμάτων για τη σημερινή μέρα κατά την εκκίνηση
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        showHabitsForDate(today);

        // Ακροατής αλλαγής ημερομηνίας στο ημερολόγιο
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                int correctedMonth = month + 1;
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, correctedMonth, dayOfMonth);
                showHabitsForDate(selectedDate);
            }
        });
    }

    // Μέθοδος που εμφανίζει ΜΟΝΟ όσα καταγράφηκαν στη βάση για τη συγκεκριμένη μέρα
    private void showHabitsForDate(String date) {
        List<String> completedList = dbHelper.getCompletedHabitsForDate(date);

        if (completedList != null && !completedList.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String habitName : completedList) {
                builder.append("• ").append(habitName).append("\n");
            }
            tvCompletedHabitsList.setText(builder.toString().trim());
        } else {
            tvCompletedHabitsList.setText("Καμία καταγραφή ολοκλήρωσης για αυτή τη μέρα. 😴");
        }
    }
}