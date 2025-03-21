package com.example.your_note;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    private EditText titleInput, noteInput;
    private TextView time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note);

        titleInput = findViewById(R.id.title);
        noteInput = findViewById(R.id.note_input);
        time = findViewById(R.id.time);
        ImageButton backButton = findViewById(R.id.back_button);

        Intent intent = getIntent();
        String dateTime = intent.getStringExtra("time");

        if (dateTime != null && !dateTime.isEmpty()) {
            time.setText(dateTime);
        } else {
            String currentTime = getCurrentTime();
            time.setText(currentTime);
        }

        backButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String noteText = noteInput.getText().toString().trim();
            String noteTime = time.getText().toString();

            if (!title.isEmpty() || !noteText.isEmpty()) {
                Intent backIntent = new Intent(NoteActivity.this, MainActivity.class);
                backIntent.putExtra("title", title);
                backIntent.putExtra("note", noteText);
                backIntent.putExtra("time", noteTime);
                startActivity(backIntent);
            }
            finish();
        });
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
}
