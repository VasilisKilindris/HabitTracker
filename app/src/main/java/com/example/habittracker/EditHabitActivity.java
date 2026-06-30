package com.example.habittracker;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class EditHabitActivity extends AppCompatActivity {

    private EditText etEditHabitName;
    private MaterialButton btnSaveHabit, btnDeleteHabit;
    private DatabaseHelper dbHelper;

    // Μεταβλητές για να αποθηκεύσουμε τα δεδομένα που μας ήρθαν από την 1η οθόνη
    private int habitId;
    private int habitStreak;
    private String habitDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_habit);

        dbHelper = new DatabaseHelper(this);

        etEditHabitName = findViewById(R.id.etEditHabitName);
        btnSaveHabit = findViewById(R.id.btnSaveHabit);
        btnDeleteHabit = findViewById(R.id.btnDeleteHabit);

        // ΠΑΙΡΝΟΥΜΕ ΤΑ ΔΕΔΟΜΕΝΑ ΑΠΟ ΤΟ INTENT
        habitId = getIntent().getIntExtra("HABIT_ID", -1);
        String currentName = getIntent().getStringExtra("HABIT_NAME");
        habitStreak = getIntent().getIntExtra("HABIT_STREAK", 0);
        habitDate = getIntent().getStringExtra("HABIT_DATE");

        // Βάζουμε το τωρινό όνομα στο πεδίο κειμένου για να το δει ο χρήστης
        etEditHabitName.setText(currentName);

        // Τι γίνεται αν πατήσει Αποθήκευση
        btnSaveHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = etEditHabitName.getText().toString().trim();
                if (!newName.isEmpty()) {
                    // Φτιάχνουμε ξανά το αντικείμενο με το νέο όνομα (τα άλλα μένουν ίδια)
                    Habit updatedHabit = new Habit(habitId, newName, habitStreak, habitDate);
                    dbHelper.updateHabit(updatedHabit); // Σώσιμο στη βάση

                    Toast.makeText(EditHabitActivity.this, "Ενημερώθηκε!", Toast.LENGTH_SHORT).show();
                    finish(); // Κλείνει αυτή η οθόνη και γυρνάμε πίσω!
                } else {
                    Toast.makeText(EditHabitActivity.this, "Το όνομα δεν μπορεί να είναι κενό", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Τι γίνεται αν πατήσει Διαγραφή
        btnDeleteHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.deleteHabit(habitId); // Διαγραφή από τη βάση
                Toast.makeText(EditHabitActivity.this, "Διαγράφηκε", Toast.LENGTH_SHORT).show();
                finish(); // Κλείνει αυτή η οθόνη και γυρνάμε πίσω!
            }
        });
    }
}