package com.example.habittracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Δήλωση των γραφικών στοιχείων (UI)
    private RecyclerView recyclerViewHabits;
    private FloatingActionButton fabAddHabit;

    // Τα TextViews της επάνω κάρτας
    private TextView tvBestStreakCount, tvRunningStreak, tvCompletionRate, tvSubtitle;

    private ProgressBar progressBarCompletion;

    // Εργαλεία Βάσης και Λίστας
    private DatabaseHelper dbHelper;
    private HabitAdapter adapter;
    private List<Habit> habitList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Έλεγχος και εφαρμογή του Night Mode κατά την εκκίνηση της εφαρμογής
        SharedPreferences startupPrefs = getSharedPreferences("HabitStats", MODE_PRIVATE);
        boolean isNightModeOn = startupPrefs.getBoolean("night_mode_enabled", false);
        if (isNightModeOn) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);

        // Αρχικοποίηση της Βάσης Δεδομένων
        dbHelper = new DatabaseHelper(this);

        // Σύνδεση των views με τα IDs τους
        recyclerViewHabits = findViewById(R.id.recyclerViewHabits);
        fabAddHabit = findViewById(R.id.fabAddHabit);

        tvBestStreakCount = findViewById(R.id.tvBestStreakCount);
        tvRunningStreak = findViewById(R.id.tvRunningStreak);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);

        tvSubtitle = findViewById(R.id.tvSubtitle);

        progressBarCompletion = findViewById(R.id.progressBarCompletion);

        // Ρύθμιση του RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewHabits.setLayoutManager(layoutManager);

        String[] quotes = getResources().getStringArray(R.array.productivity_quotes);
        java.util.Random random = new java.util.Random();
        int randomIndex = random.nextInt(quotes.length);
        tvSubtitle.setText(quotes[randomIndex]);

        // Φόρτωση δεδομένων από τη βάση και εμφάνιση στην οθόνη
        loadHabits();

        // Προγραμματισμός του κλικ του κουμπιού για προσθήκη συνήθειας
        fabAddHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddHabitDialog();
            }
        });

        android.widget.ImageButton btnOpenHistory = findViewById(R.id.btnOpenHistory);
        btnOpenHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent για να ανοίξει η οθόνη του ιστορικού
                android.content.Intent intent = new android.content.Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        android.widget.ImageButton btnOpenSettings = findViewById(R.id.btnOpenSettings);
        btnOpenSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Intent intent = new android.content.Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabits();
    }

    // Μέθοδος που διαβάζει τα δεδομένα και τα βάζει στον Adapter
    private void loadHabits() {
        habitList = dbHelper.getAllHabits();
        adapter = new HabitAdapter(this, habitList, dbHelper);
        recyclerViewHabits.setAdapter(adapter);

        updateStats();
    }

    // Dialog Προσθήκης
    private void showAddHabitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        // Φορτώνουμε το custom layout που φτιάξαμε
        View dialogView = inflater.inflate(R.layout.dialog_add_habit, null);
        builder.setView(dialogView);

        EditText etHabitName = dialogView.findViewById(R.id.etHabitName);

        builder.setPositiveButton("Προσθήκη", (dialog, which) -> {
                    String name = etHabitName.getText().toString().trim();

                    // Αν ο χρήστης έγραψε κάτι (δεν είναι κενό)
                    if (!name.isEmpty()) {
                        // Δημιουργία και Αποθήκευση στη Βάση
                        Habit newHabit = new Habit(name);
                        dbHelper.addHabit(newHabit);

                        // Ξαναφορτώνουμε τη λίστα
                        loadHabits();
                        Toast.makeText(MainActivity.this, "Επιτυχής προσθήκη!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Το όνομα δεν μπορεί να είναι κενό", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Άκυρο", null);

        builder.show();
    }

    public void updateStats() {
        int completedTodayCount = 0;

        // Βρίσκουμε τη σημερινή ημερομηνία
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Πόσες συνήθειες ολοκληρώθηκαν σήμερα
        for (Habit h : habitList) {
            if (h.getLastCompletedDate() != null && h.getLastCompletedDate().equals(today)) {
                completedTodayCount++;
            }
        }

        // ----- ΥΠΟΛΟΓΙΣΜΟΣ ΠΟΣΟΣΤΟΥ  -----
        if (habitList.size() > 0) {
            int rate = (completedTodayCount * 100) / habitList.size();
            tvCompletionRate.setText(rate + "%");
            progressBarCompletion.setProgress(rate);
        } else {
            tvCompletionRate.setText("0%");
            progressBarCompletion.setProgress(0);
        }

        // -------------------   ΥΠΟΛΟΓΙΣΜΟΣ STREAKS      -------------------------
        SharedPreferences prefs = getSharedPreferences("HabitStats", MODE_PRIVATE);
        int bestPerfectStreak = prefs.getInt("best_perfect_streak", 0);
        int currentPerfectStreak = prefs.getInt("current_perfect_streak", 0);
        String lastPerfectDay = prefs.getString("last_perfect_day", "");

        // Βρίσκουμε το "Χθες"
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        // Αν η χθεσινή μέρα ΔΕΝ ήταν τέλεια και ούτε η σημερινή, το τρέχον σερί μηδενίζεται!
        if (!lastPerfectDay.equals(today) && !lastPerfectDay.equals(yesterday)) {
            currentPerfectStreak = 0;
        }

        // Αν Η ΣΗΜΕΡΙΝΗ μέρα έγινε στο 100%
        if (habitList.size() > 0 && completedTodayCount == habitList.size()) {

            // Αν δεν εχει ηδη αυξηθεί το σκορ
            if (!lastPerfectDay.equals(today)) {

                if (lastPerfectDay.equals(yesterday)) {
                    currentPerfectStreak++; // Συνεχόμενη τέλεια μέρα
                } else {
                    currentPerfectStreak = 1; // Ξεκίνημα νέου σερί
                }

                lastPerfectDay = today;

                // Ενημερώση το Ρεκόρ
                if (currentPerfectStreak > bestPerfectStreak) {
                    bestPerfectStreak = currentPerfectStreak;
                }

                // Αποθηκεύουμε τα νέα νούμερα
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("current_perfect_streak", currentPerfectStreak);
                editor.putInt("best_perfect_streak", bestPerfectStreak);
                editor.putString("last_perfect_day", lastPerfectDay);
                editor.apply();
            }
        }

        // ΕΜΦΑΝΙΣΗ ΣΤΗΝ ΟΘΟΝΗ
        tvRunningStreak.setText(currentPerfectStreak + " ημ.");
        tvBestStreakCount.setText(bestPerfectStreak + " ημ.");
    }
}