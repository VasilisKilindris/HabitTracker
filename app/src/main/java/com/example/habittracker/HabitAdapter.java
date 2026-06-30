package com.example.habittracker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habitList;
    private Context context;
    private DatabaseHelper dbHelper;

    // Κατασκευαστής του Adapter
    public HabitAdapter(Context context, List<Habit> habitList, DatabaseHelper dbHelper) {
        this.context = context;
        this.habitList = habitList;
        this.dbHelper = dbHelper;
    }

    // 1. Δημιουργία της κάρτας (item_habit.xml)
    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    // 2. Σύνδεση των δεδομένων με τα γραφικά στοιχεία της κάρτας
    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit currentHabit = habitList.get(position);

        holder.tvHabitTitle.setText(currentHabit.getName());
        holder.tvHabitStreak.setText("⚡ " + currentHabit.getStreak() + " ημέρες");

        // Βρες τη σημερινή ημερομηνία για να ελέγξουμε αν το task έγινε σήμερα
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        CardView cardView = (CardView) holder.itemView;

        // Έλεγχος αν η συσκευή βρίσκεται σε Night Mode αυτή τη στιγμή
        int nightModeFlags = context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean isNightMode = (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES);

        // -------------------------- ΕΛΕΓΧΟΣ ΟΠΤΙΚΗΣ ΕΜΦΑΝΙΣΗΣ (ΧΡΩΜΑΤΑ & ΕΙΚΟΝΙΔΙΑ) ---------------------------
        if (currentHabit.getLastCompletedDate() != null && currentHabit.getLastCompletedDate().equals(today)) {
            // ΑΝ ΟΛΟΚΛΗΡΩΘΗΚΕ ΣΗΜΕΡΑ:
            holder.btnCheckHabit.setImageResource(R.drawable.ic_check_box_filled);
            holder.btnCheckHabit.setColorFilter(Color.parseColor("#4ADE80"));

            if (isNightMode) {
                cardView.setCardBackgroundColor(Color.parseColor("#052E16"));
                holder.tvHabitTitle.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#F0FDF4"));
                holder.tvHabitTitle.setTextColor(Color.parseColor("#1A1D20"));
            }
        } else {
            // ΑΝ ΔΕΝ ΕΧΕΙ ΟΛΟΚΛΗΡΩΘΕΙ:
            holder.btnCheckHabit.setImageResource(R.drawable.ic_check_box_empty);
            holder.btnCheckHabit.setColorFilter(Color.parseColor("#717D8A"));

            if (isNightMode) {
                cardView.setCardBackgroundColor(Color.parseColor("#1E1E1E"));
                holder.tvHabitTitle.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                holder.tvHabitTitle.setTextColor(Color.parseColor("#1A1D20"));
            }
        }

        // ---ΚΛΙΚ ΣΤΗΝ ΚΑΡΤΑ ΓΙΑ ΕΠΕΞΕΡΓΑΣΙΑ ---
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    Habit habitToEdit = habitList.get(currentPos);

                    // Μετάβαση στο 2ο Activity (EditHabitActivity)
                    android.content.Intent intent = new android.content.Intent(context, EditHabitActivity.class);
                    intent.putExtra("HABIT_ID", habitToEdit.getId());
                    intent.putExtra("HABIT_NAME", habitToEdit.getName());
                    intent.putExtra("HABIT_STREAK", habitToEdit.getStreak());
                    intent.putExtra("HABIT_DATE", habitToEdit.getLastCompletedDate());
                    context.startActivity(intent);
                }
            }
        });

        // ----------------- ΚΛΙΚ ΣΤΟ ΚΟΥΜΠΙ CHECK  ------------------------
        holder.btnCheckHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();

                if (currentPosition != RecyclerView.NO_POSITION) {
                    Habit clickedHabit = habitList.get(currentPosition);
                    String todayClick = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    // Αν η συνήθεια έχει ήδη ολοκληρωθεί σήμερα ->  UNCHECK
                    if (clickedHabit.getLastCompletedDate() != null && clickedHabit.getLastCompletedDate().equals(todayClick)) {

                        // 1 Μειώνουμε το streak κατά 1
                        int newStreak = clickedHabit.getStreak() - 1;
                        if (newStreak < 0) newStreak = 0; //Κάτω όριο το 0

                        clickedHabit.setStreak(newStreak);

                        // 2 Αδειάζουμε την ημερομηνία τελευταίας ολοκλήρωσης
                        clickedHabit.setLastCompletedDate("");

                        // 3 Ενημερώνουμε τη SQLite και σβήνουμε την εγγραφή από το ιστορικό
                        dbHelper.updateHabit(clickedHabit);
                        dbHelper.deleteCompletion(clickedHabit.getId(), todayClick);

                        Toast.makeText(context, "Η ολοκλήρωση ακυρώθηκε", Toast.LENGTH_SHORT).show();
                    }
                    // Αν ΔΕΝ έχει γίνει σήμερα -> Κανονικό CHECK
                    else {
                        // 1 Αυξάνουμε το streak κατά 1
                        int newStreak = clickedHabit.getStreak() + 1;
                        clickedHabit.setStreak(newStreak);

                        // 2 Καταγράφουμε τη σημερινή ημερομηνία
                        clickedHabit.setLastCompletedDate(todayClick);

                        // 3 Ενημερώνουμε SQLite και προσθέτουμε εγγραφή στο ιστορικό
                        dbHelper.updateHabit(clickedHabit);
                        dbHelper.logCompletion(clickedHabit.getId(), todayClick);

                        Toast.makeText(context, "Μπράβο! Το streak αυξήθηκε!", Toast.LENGTH_SHORT).show();
                    }

                    // Ανανέωση των γραφικών της λίστας
                    notifyDataSetChanged();

                    // Ανανέωση των στατιστικών και της μπάρας προόδου στην κεντρική οθόνη
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).updateStats();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvHabitTitle, tvHabitStreak;
        ImageButton btnCheckHabit;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHabitTitle = itemView.findViewById(R.id.tvHabitTitle);
            tvHabitStreak = itemView.findViewById(R.id.tvHabitStreak);
            btnCheckHabit = itemView.findViewById(R.id.btnCheckHabit);
        }
    }
}
