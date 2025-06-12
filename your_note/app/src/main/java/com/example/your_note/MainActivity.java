package com.example.your_note;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private GridLayout notesContainer;
    private ImageView noteIcon;
    private TextView noteIconText;
    private NotesDatabaseHelper dbHelper;

    private String selectedCategory = "Все";

    private static final int REQUEST_PERMISSION = 100;

    private static final int REQUEST_CODE_ADD_NOTE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        checkPermissions();

        dbHelper = new NotesDatabaseHelper(this);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        View topToolbar = findViewById(R.id.top_toolbar);
        ImageButton menuButton = topToolbar.findViewById(R.id.menu_button);
        EditText searchField = topToolbar.findViewById(R.id.search_field);
        ImageButton searchButton = topToolbar.findViewById(R.id.search_button);

        searchButton.setOnClickListener(v -> {
            if (searchField.getVisibility() == View.INVISIBLE) {
                searchField.setVisibility(View.VISIBLE);
                searchField.requestFocus();
                searchButton.setSelected(true);
            } else {
                searchField.setVisibility(View.INVISIBLE);
                searchButton.setSelected(false);
                searchField.setText("");
                loadNotesFromDatabase();
            }
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        View bottomToolbar = findViewById(R.id.bottom_toolbar);
        ImageButton createButton = bottomToolbar.findViewById(R.id.create_button);
        ImageButton calendarButton = bottomToolbar.findViewById(R.id.calendar_button);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        createButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            if (!selectedCategory.equals("Все")) {
                intent.putExtra("category", selectedCategory);
            }
            String creationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            intent.putExtra("creationDate", creationDate);

            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
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

        notesContainer = findViewById(R.id.notes_container);
        noteIcon = findViewById(R.id.note_icon);
        noteIconText = findViewById(R.id.note_icon_text);

        Button allButton = findViewById(R.id.all_notes);
        Button personalButton = findViewById(R.id.personal_notes);
        Button studyButton = findViewById(R.id.study_notes);

        allButton.setOnClickListener(v -> {
            selectedCategory = "Все";
            loadNotesFromDatabase();
        });

        personalButton.setOnClickListener(v -> {
            selectedCategory = "Личное";
            loadNotesFromDatabase();
        });

        studyButton.setOnClickListener(v -> {
            selectedCategory = "Учёба";
            loadNotesFromDatabase();
        });

        ImageButton themeButton = navigationView.findViewById(R.id.theme_button);
        themeButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Будет доступно в следующей версии! ;)", Toast.LENGTH_SHORT).show();
        });

        ImageButton backButton = navigationView.findViewById(R.id.back_menu_button);
        backButton.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        Button settingsButton = navigationView.findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        });

        Button categoryButton = navigationView.findViewById(R.id.category_button);
        categoryButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Будет доступно в следующей версии! ;)", Toast.LENGTH_SHORT).show();
        });

        Button cartButton = navigationView.findViewById(R.id.cart_button);
        cartButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Будет доступно в следующей версии! ;)", Toast.LENGTH_SHORT).show();
        });

        ImageButton filterButton = findViewById(R.id.filter_button);

        filterButton.setOnClickListener(v -> {
            String[] sortOptions = {"По дате: новые", "По дате: старые", "По алфавиту: А-Я", "По алфавиту: Я-А"};

            new AlertDialog.Builder(this)
                    .setTitle("Выберите сортировку")
                    .setItems(sortOptions, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                loadNotesSorted("date DESC");
                                break;
                            case 1:
                                loadNotesSorted("date ASC");
                                break;
                            case 2:
                                loadNotesSorted("title ASC");
                                break;
                            case 3:
                                loadNotesSorted("title DESC");
                                break;
                        }
                    })
                    .show();
        });

        loadNotesFromDatabase();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK && data != null) {
            int newNoteId = data.getIntExtra("new_note_id", -1);
            if (newNoteId != -1) {
                Note newNote = dbHelper.getNoteById(newNoteId);
                if (newNote != null) {
                    View noteCard = createNoteCard(newNote);
                    notesContainer.addView(noteCard, 0);
                    noteIcon.setVisibility(View.GONE);
                    noteIconText.setVisibility(View.GONE);
                }
            }
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

        if (selectedCategory.equals("Все")) {
            notes = dbHelper.getAllNotes();
        } else {
            notes = dbHelper.getNotesByCategory(selectedCategory);
        }

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

    private View createNoteCard(Note note, String highlightQuery) {
        int cardSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics());

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 16, 16, 16);
        card.setBackgroundResource(R.drawable.note_background);
        card.setGravity(Gravity.TOP);
        card.setClipToPadding(false);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = cardSize;
        params.height = cardSize;
        params.setMargins(16, 16, 16, 16);
        params.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setLayoutParams(params);

        TextView titleView = new TextView(this);
        String title = note.getTitle() != null ? note.getTitle() : "Без названия";

        if (!highlightQuery.isEmpty()) {
            SpannableString spannableTitle = new SpannableString(title);
            String lowerTitle = title.toLowerCase();
            String lowerQuery = highlightQuery.toLowerCase();

            int start = lowerTitle.indexOf(lowerQuery);
            while (start >= 0) {
                int end = start + highlightQuery.length();
                spannableTitle.setSpan(
                        new BackgroundColorSpan(Color.YELLOW),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                start = lowerTitle.indexOf(lowerQuery, end);
            }
            titleView.setText(spannableTitle);
        } else {
            titleView.setText(title);
        }

        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextSize(14f);
        titleView.setMaxLines(1);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(titleView);

        if (note.getDate() != null) {
            TextView dateView = new TextView(this);
            dateView.setText(note.getDate());
            dateView.setTextSize(10f);
            dateView.setTextColor(Color.GRAY);
            dateView.setMaxLines(1);
            card.addView(dateView);
        }

        if (note.getText() != null && !note.getText().trim().isEmpty()) {
            TextView textView = new TextView(this);
            textView.setText(Html.fromHtml(note.getText(), Html.FROM_HTML_MODE_LEGACY));
            textView.setTextSize(12f);
            textView.setMaxLines(3);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            card.addView(textView);
        }

        if (note.getDrawingPath() != null && !note.getDrawingPath().isEmpty()) {
            ImageView drawingView = new ImageView(this);
            drawingView.setImageURI(Uri.parse(note.getDrawingPath()));
            drawingView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            int heightPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
            drawingView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
            card.addView(drawingView);
        }

        if (note.getImagePath() != null && !note.getImagePath().isEmpty()) {
            ImageView imageView = new ImageView(this);
            imageView.setImageURI(Uri.parse(note.getImagePath()));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            int heightPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
            card.addView(imageView);
        }

        if (note.getAudioPath() != null && !note.getAudioPath().isEmpty()) {
            ImageView audioIcon = new ImageView(this);
            audioIcon.setImageResource(R.drawable.audio_recording);
            audioIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int heightPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
            audioIcon.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
            card.addView(audioIcon);
        }

        card.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            intent.putExtra("note_id", note.getId());
            startActivity(intent);
        });

        card.setOnLongClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Удалить заметку?")
                    .setMessage("Вы уверены, что хотите удалить эту заметку?")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        dbHelper.deleteNote(note.getId());
                        loadNotesFromDatabase();
                        Toast.makeText(MainActivity.this, "Заметка удалена", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            return true;
        });

        return card;
    }

    private View createNoteCard(Note note) {
        return createNoteCard(note, "");
    }

    private void performSearch(String query) {
        List<Note> filteredNotes;

        if (query.isEmpty()) {
            filteredNotes = selectedCategory.equals("Все")
                    ? dbHelper.getAllNotes()
                    : dbHelper.getNotesByCategory(selectedCategory);
        } else {
            filteredNotes = dbHelper.searchNotes(query);
        }

        notesContainer.removeAllViews();

        if (filteredNotes.isEmpty()) {
            noteIcon.setVisibility(View.VISIBLE);
            noteIconText.setVisibility(View.VISIBLE);
            noteIconText.setText("Ничего не найдено");
        } else {
            noteIcon.setVisibility(View.GONE);
            noteIconText.setVisibility(View.GONE);

            for (Note note : filteredNotes) {
                View noteCard = createNoteCard(note, query);
                notesContainer.addView(noteCard);
            }
        }
    }

    private void loadNotesSorted(String orderBy) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    NotesDatabaseHelper.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    orderBy
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COL_ID)));
                    note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COL_TITLE)));
                    note.setText(cursor.getString(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COL_TEXT)));
                    note.setDate(cursor.getString(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COL_DATE)));
                    note.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COL_IMAGE_PATH)));
                    note.setAudioPath(cursor.getString(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COL_AUDIO_PATH)));
                    note.setDrawingPath(cursor.getString(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COL_DRAWING_PATH)));
                    note.setReminderTimeMillis(cursor.getLong(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COL_REMINDER_TIME)));
                    note.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(NotesDatabaseHelper.COL_CATEGORY)));

                    notes.add(note);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

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
}
