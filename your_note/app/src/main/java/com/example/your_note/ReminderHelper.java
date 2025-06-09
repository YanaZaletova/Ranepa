package com.example.your_note;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;
import java.util.function.Consumer;

public class ReminderHelper {

    public static void showDateTimePicker(Context context, Consumer<Long> onTimeSelected) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                long selectedTimeMillis = calendar.getTimeInMillis();
                onTimeSelected.accept(selectedTimeMillis); // ← сообщаем NoteActivity
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }


    public static void scheduleAlarms(Context context, long targetTimeMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long[] times = {
                targetTimeMillis - 24 * 60 * 60 * 1000,
                targetTimeMillis - 60 * 60 * 1000,
                targetTimeMillis
        };

        for (int i = 0; i < times.length; i++) {
            long triggerAtMillis = times[i];
            if (triggerAtMillis < System.currentTimeMillis()) continue;

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("note_text", "У вас есть запланированная заметка");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }

        Toast.makeText(context, "Напоминание установлено!", Toast.LENGTH_SHORT).show();
    }
}
