package com.example.habittracker;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;

public class
SettingsActivity extends AppCompatActivity {

    private MaterialButton btnResetAll;
    private SwitchCompat switchNotifications, switchNightMode;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DatabaseHelper(this);
        sharedPrefs = getSharedPreferences("HabitStats", MODE_PRIVATE);

        btnResetAll = findViewById(R.id.btnResetAll);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchNightMode = findViewById(R.id.switchNightMode);

        // Φόρτωση προηγούμενης κατάστασης για τις Υπενθυμίσεις
        boolean isNotifyOn = sharedPrefs.getBoolean("notifications_enabled", false);
        switchNotifications.setChecked(isNotifyOn);

        // Φόρτωση προηγούμενης κατάστασης για το Night Mode
        boolean isNightModeOn = sharedPrefs.getBoolean("night_mode_enabled", false);
        switchNightMode.setChecked(isNightModeOn);

        // Κουμπί Reset
        btnResetAll.setOnClickListener(v -> showResetConfirmationDialog());

        // 1ος διακόπτης για υπενθυμίσεις
        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("notifications_enabled", isChecked);
                editor.apply();
            }
        });

        // 2ος διακόπτης για NightMode
        switchNightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("night_mode_enabled", isChecked);
                editor.apply();

                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Toast.makeText(SettingsActivity.this, "Σκοτεινή Λειτουργία", Toast.LENGTH_SHORT).show();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Toast.makeText(SettingsActivity.this, "Φωτεινή Λειτουργία", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Μέθοδος για κουμπιού προειδοποίησης reset
    private void showResetConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Προσοχή!");
        builder.setMessage("Είστε σίγουροι ότι θέλετε να διαγράψετε όλες τις συνήθειες, το ιστορικό και τα streaks σας;");

        builder.setPositiveButton("Ναι, Διαγραφή", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.clearAllData();

                // Καθαρισμός SharedPreferences (κρατώντας όμως τις ρυθμίσεις των διακοπτών)
                boolean currentNightMode = sharedPrefs.getBoolean("night_mode_enabled", false);
                boolean currentNotifications = sharedPrefs.getBoolean("notifications_enabled", false);

                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.clear();
                editor.putBoolean("night_mode_enabled", currentNightMode);
                editor.putBoolean("notifications_enabled", currentNotifications);
                editor.apply();

                Toast.makeText(SettingsActivity.this, "Όλα τα δεδομένα διαγράφηκαν!", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        builder.setNegativeButton("Άκυρο", null);
        builder.show();
    }
}