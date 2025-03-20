package com.example.your_note;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note);

        ImageButton backbutton = findViewById(R.id.back_button);

        backbutton.setOnClickListener(v -> {
            Intent intent = new Intent(NoteActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}