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
import androidx.drawerlayout.widget.DrawerLayout;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageButton menuButton = findViewById(R.id.menu_button);
        ImageButton createButton = findViewById(R.id.create_button);
        ImageButton notesbutton = findViewById(R.id.notes_button);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        createButton.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, NoteActivity.class);
            startActivity(intent);
        });

        notesbutton.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}