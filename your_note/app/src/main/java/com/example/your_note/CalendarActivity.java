package com.example.your_note;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {

    private String selectedDate = "";
    private int selectedHour = -1, selectedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        CalendarView calendarView = findViewById(R.id.calendarView);

        View topToolbar = findViewById(R.id.top_toolbar);
        ImageButton menuButton = topToolbar.findViewById(R.id.menu_button);

        View bottomToolbar = findViewById(R.id.bottom_toolbar);
        ImageButton createButton = bottomToolbar.findViewById(R.id.create_button);
        ImageButton notesButton = bottomToolbar.findViewById(R.id.notes_button);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        createButton.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, NoteActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        notesButton.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "." + month + "." + year;
            showDateTimeDialog();
        });
    }

    private void showDateTimeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_date_time_picker);
        dialog.setCancelable(false);

        TextView selectedDateView = dialog.findViewById(R.id.selected_date);
        Button pickTimeButton = dialog.findViewById(R.id.pick_time_button);
        Button createNoteButton = dialog.findViewById(R.id.create_note_button);
        ImageButton closeButton = dialog.findViewById(R.id.close_dialog_button);

        selectedDateView.setText("Выбранная дата: " + selectedDate);

        pickTimeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
                selectedHour = hourOfDay;
                selectedMinute = minuteOfHour;
                pickTimeButton.setText(String.format("Время: %02d:%02d", selectedHour, selectedMinute));
            }, hour, minute, true);

            timePickerDialog.show();
        });

        createNoteButton.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, NoteActivity.class);

            String dateTime = "";
            if (!selectedDate.isEmpty()) {
                dateTime += selectedDate;
            }
            if (selectedHour != -1 && selectedMinute != -1) {
                if (!dateTime.isEmpty()) {
                    dateTime += " ";
                }
                dateTime += String.format("%02d:%02d", selectedHour, selectedMinute);
            }

            intent.putExtra("time", dateTime);
            startActivity(intent);
            dialog.dismiss();
        });


        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
