package com.example.your_note;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout notesContainer;
    private ImageView noteIcon;
    private TextView noteIconText;
    private NotesDatabaseHelper dbHelper;

    private static final int REQUEST_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        checkPermissions();

        notesContainer = findViewById(R.id.notes_container);

        dbHelper = new NotesDatabaseHelper(this);

        loadNotesFromDatabase();

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        View topToolbar = findViewById(R.id.top_toolbar);
        ImageButton menuButton = topToolbar.findViewById(R.id.menu_button);

        View bottomToolbar = findViewById(R.id.bottom_toolbar);
        ImageButton createButton = bottomToolbar.findViewById(R.id.create_button);
        ImageButton calendarButton = bottomToolbar.findViewById(R.id.calendar_button);

        View navMenu = navigationView.getHeaderView(0);

        notesContainer = findViewById(R.id.notes_container);
        noteIcon = findViewById(R.id.note_icon);
        noteIconText = findViewById(R.id.note_icon_text);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        createButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            startActivity(intent);
        });

        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String noteText = intent.getStringExtra("note");
        String time = intent.getStringExtra("time");

        if ((title != null && !title.trim().isEmpty()) || (noteText != null && !noteText.trim().isEmpty())) {
            addNoteToScreen(title, noteText, time);
        }
    }

    private void addNoteToScreen(String title, String noteText, String time) {
        noteIcon.setVisibility(View.GONE);
        noteIconText.setVisibility(View.GONE);

        TextView noteView = new TextView(this);
        noteView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        noteView.setPadding(20, 20, 20, 20);
        noteView.setBackgroundResource(R.drawable.note_background);
        noteView.setGravity(Gravity.START);
        noteView.setText(title + "\n" + noteText + "\n" + time);

        notesContainer.addView(noteView);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_PERMISSION
            );
        }
    }

    private void loadNotesFromDatabase() {
        List<Note> notes = dbHelper.getAllNotes();

        notesContainer.removeAllViews();

        if (notes.isEmpty()) {
            noteIcon.setVisibility(View.VISIBLE);
            noteIconText.setVisibility(View.VISIBLE);
        } else {
            noteIcon.setVisibility(View.GONE);
            noteIconText.setVisibility(View.GONE);

            for (Note note : notes) {
                View noteCard = createNoteCard(note);
                notesContainer.addView(noteCard);
            }
        }
    }

    private View createNoteCard(Note note) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 20, 20, 20);
        card.setBackgroundResource(R.drawable.note_background);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView titleView = new TextView(this);
        titleView.setText(note.getTitle());
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextSize(18f);

        TextView dateView = new TextView(this);
        dateView.setText(note.getDate());
        dateView.setTextSize(12f);

        TextView snippetView = new TextView(this);
        String snippet = note.getText();
        if (snippet.length() > 100) snippet = snippet.substring(0, 100) + "...";
        snippetView.setText(snippet);

        card.addView(titleView);
        card.addView(dateView);
        card.addView(snippetView);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            intent.putExtra("note_id", note.getId());
            startActivity(intent);
        });

        return card;
    }
}
