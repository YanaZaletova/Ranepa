package com.example.your_note;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    private LinearLayout inputField, imagePreviewRow;
    private LinearLayout textPanelContainer, drawingPanel, imagePanel, audioPanelContainer;
    private int noteId = -1;
    private boolean hasChanges() {
        boolean titleChanged = !titleInput.getText().toString().equals(originalTitle);
        boolean textChanged = !noteInput.getText().toString().equals(originalText);
        boolean drawingChanged = drawingView != null && drawingView.hasDrawing();

        return titleChanged || textChanged || drawingChanged;
    }

    private String originalText = "";
    private String originalTitle = "";
    private EditText noteInput, titleInput;
    DrawingView drawingView;
    final boolean[] isDrawingMode = {false};
    private ImageView imageOverlay;
    private Uri imageUri;
    private long reminderTimeMillis = -1;
    private AudioHelper audioHelper;
    private ImageButton insertButton, audioButton, recordingButton, removeButton;
    private Layout.Alignment currentAlignment = Layout.Alignment.ALIGN_NORMAL;

    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        drawingView = findViewById(R.id.drawing_view);

        titleInput = findViewById(R.id.title);
        noteInput = findViewById(R.id.note_input);
        noteInput.bringToFront();

        textPanelContainer = findViewById(R.id.text_formatting_panel);
        audioPanelContainer = findViewById(R.id.merge_audio_panel);
        drawingPanel = findViewById(R.id.drawing_tools_panel);
        imagePanel = findViewById(R.id.merge_image_panel);

        noteInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                checkSelectionStyles();
            }
        });

        noteInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.postDelayed(this::checkSelectionStyles, 50);
            }
            return false;
        });

        noteInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                checkSelectionStyles();
            }
        });

        inputField = findViewById(R.id.audio_overlay);

        ImageButton textButton = findViewById(R.id.text_button);
        audioButton = findViewById(R.id.audio_button);

        insertButton = findViewById(R.id.insert_button);
        removeButton = findViewById(R.id.remove_audio_button);
        recordingButton = findViewById(R.id.recording_button);

        EditText noteInput = findViewById(R.id.note_input);
        EditText searchField = findViewById(R.id.search_field);
        ImageButton searchButton = findViewById(R.id.search_button);

        searchButton.setOnClickListener(v -> {
            if (searchField.getVisibility() == View.GONE) {
                searchField.setVisibility(View.VISIBLE);
                searchField.requestFocus();
                searchButton.setSelected(true);
            } else {
                searchField.setVisibility(View.GONE);
                searchButton.setSelected(false);
                highlightSearchResults(noteInput, "");
            }
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                highlightSearchResults(noteInput, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //Текстовое форматирование

        textButton.setOnClickListener(v -> {
            hideOtherPanelsExcept(textPanelContainer);
            TextFormatting.toggleFormattingPanel(textPanelContainer);
        });

        ImageButton boldBtn = findViewById(R.id.bold_button);
        ImageButton italicBtn = findViewById(R.id.italic_button);
        ImageButton underlineBtn = findViewById(R.id.underline_button);
        ImageButton strikeBtn = findViewById(R.id.strike_button);

//        ImageButton alignLeft = findViewById(R.id.align_left);
//        ImageButton alignCenter = findViewById(R.id.align_center);
//        ImageButton alignRight = findViewById(R.id.align_right);

//        alignLeft.setOnClickListener(v -> {
//            TextFormatting.applyAlignment(noteInput, Layout.Alignment.ALIGN_NORMAL);
//            TextFormatting.setAlignmentSelected(alignLeft, alignCenter, alignRight);
//            currentAlignment = Layout.Alignment.ALIGN_NORMAL;
//        });
//
//        alignCenter.setOnClickListener(v -> {
//            TextFormatting.applyAlignment(noteInput, Layout.Alignment.ALIGN_CENTER);
//            TextFormatting.setAlignmentSelected(alignCenter, alignLeft, alignRight);
//        });
//
//        alignRight.setOnClickListener(v -> {
//            TextFormatting.applyAlignment(noteInput, Layout.Alignment.ALIGN_OPPOSITE);
//            TextFormatting.setAlignmentSelected(alignRight, alignLeft, alignCenter);
//        });

        boldBtn.setOnClickListener(v -> {
            boolean isApplied = TextFormatting.toggleBold(noteInput);
            boldBtn.setSelected(isApplied);
            if (isApplied) {
                italicBtn.setSelected(false);
            }
        });

        italicBtn.setOnClickListener(v -> {
            boolean isApplied = TextFormatting.toggleItalic(noteInput);
            italicBtn.setSelected(isApplied);
            if (isApplied) {
                boldBtn.setSelected(false);
            }
        });

        underlineBtn.setOnClickListener(v -> {
            TextFormatting.toggleButton(underlineBtn);
            TextFormatting.applyUnderline(noteInput);
        });

        strikeBtn.setOnClickListener(v -> {
            TextFormatting.toggleButton(strikeBtn);
            TextFormatting.applyStrikethrough(noteInput);
        });

        int[] colorIds = {
                R.id.color_red, R.id.color_orange, R.id.color_yellow, R.id.color_green,
                R.id.color_light_blue, R.id.color_blue, R.id.color_purple, R.id.color_black
        };

        int[] colors = {
                Color.RED, 0xFFFF9800, 0xFFFFFF00, Color.GREEN,
                0xFF03A9F4, Color.BLUE, 0xFF9C27B0, Color.BLACK
        };

        ImageButton[] colorButtons = new ImageButton[colorIds.length];

        for (int i = 0; i < colorIds.length; i++) {
            final int col = colors[i];
            final ImageButton btn = findViewById(colorIds[i]);
            colorButtons[i] = btn;
            btn.setOnClickListener(v -> {
                TextFormatting.clearColorSelection(colorButtons);
                btn.setSelected(true);
                TextFormatting.applyTextColor(noteInput, col);
            });
        }

        //Маркеры

        ImageButton checklistButton = findViewById(R.id.checklist_button);
        checklistButton.setOnClickListener(v -> {
            ChecklistHelper.insertChecklistItem(noteInput);
        });

        //Рисование

        int bgColor;
        Drawable background = noteInput.getBackground();
        if (background instanceof ColorDrawable) {
            bgColor = ((ColorDrawable) background).getColor();
        } else {
            bgColor = ((ColorDrawable) noteInput.getBackground().mutate()).getColor();
        }

        if (bgColor == 0) bgColor = Color.WHITE;
        drawingView.setEraserBackgroundColor(bgColor);

        LinearLayout drawingPanel = findViewById(R.id.drawing_tools_panel);
        ImageButton drawButton = findViewById(R.id.draw_button);

        ScrollView scrollView = findViewById(R.id.scroll_content);

        drawButton.setOnClickListener(v -> {
            drawingView.setEditable(true);
            hideOtherPanelsExcept(drawingPanel);
            togglePanelWithAnimation(drawingPanel);

            if (!isDrawingMode[0]) {
                drawingView.setVisibility(View.VISIBLE);
                drawingView.bringToFront();

                noteInput.clearFocus();
                noteInput.setCursorVisible(false);
                isDrawingMode[0] = true;

                drawButton.setSelected(true);

                Toast.makeText(this, "Режим рисования активирован. Выйти по долгому нажатию", Toast.LENGTH_LONG).show();
            } else {
                drawingView.setVisibility(View.VISIBLE);
                drawingView.bringToFront();
            }
        });

        drawButton.setOnLongClickListener(v -> {
            drawingView.setEditable(false);
            drawingView.setVisibility(View.VISIBLE);
            drawingPanel.setVisibility(View.GONE);

            noteInput.bringToFront();
            noteInput.requestFocus();
            noteInput.setCursorVisible(true);

            scrollView.bringToFront();

            isDrawingMode[0] = false;
            drawButton.setSelected(false);

            Toast.makeText(this, "Режим рисования отключён", Toast.LENGTH_SHORT).show();
            return true;
        });

        ImageButton paintThin = findViewById(R.id.paint_thin);
        ImageButton paintMedium = findViewById(R.id.paint_medium);
        ImageButton paintThick = findViewById(R.id.paint_thick);

        paintThin.setOnClickListener(v -> {
            drawingView.setStrokeWidth(5f);
            selectPaintWidth(paintThin, paintMedium, paintThick);
        });

        paintMedium.setOnClickListener(v -> {
            drawingView.setStrokeWidth(10f);
            selectPaintWidth(paintMedium, paintThin, paintThick);
        });

        paintThick.setOnClickListener(v -> {
            drawingView.setStrokeWidth(20f);
            selectPaintWidth(paintThick, paintThin, paintMedium);
        });

        ImageButton eraserBtn = findViewById(R.id.paint_eraser);
        eraserBtn.setOnClickListener(v -> {
            boolean selected = !eraserBtn.isSelected();
            eraserBtn.setSelected(selected);
            drawingView.setEraserMode(selected);
        });

        findViewById(R.id.clean).setOnClickListener(v -> drawingView.clearCanvas());

        int[] colorBtnIds = {
                R.id.draw_color_red, R.id.draw_color_orange, R.id.draw_color_yellow,
                R.id.draw_color_green, R.id.draw_color_light_blue,
                R.id.draw_color_blue, R.id.draw_color_purple, R.id.draw_color_black
        };

        int[] paintColors = {
                Color.RED, 0xFFFF9800, 0xFFFFFF00, Color.GREEN,
                0xFF03A9F4, Color.BLUE, 0xFF9C27B0, Color.BLACK
        };

        for (int i = 0; i < colorBtnIds.length; i++) {
            final int col = paintColors[i];
            findViewById(colorBtnIds[i]).setOnClickListener(v -> {
                drawingView.setColor(col);
                drawingView.setEraserMode(false);
                eraserBtn.setSelected(false);
            });
        }

        //Изображения

        imagePreviewRow = findViewById(R.id.image_preview_row);
        imageOverlay = findViewById(R.id.image_overlay);
        LinearLayout imagePanel = findViewById(R.id.merge_image_panel);

        ImageButton imageButton = findViewById(R.id.image_button);
        imageButton.setOnClickListener(v -> {
            hideOtherPanelsExcept(imagePanel);
            togglePanelWithAnimation(imagePanel);
            ImageHelper.loadRecentImages(this, imagePreviewRow, imageOverlay, uri -> imageUri = uri);
        });

        findViewById(R.id.btn_add_image).setOnClickListener(v -> {
            ImageHelper.openGallery(this);
        });

        findViewById(R.id.btn_camera).setOnClickListener(v -> {
            ImageHelper.openCamera(this);
        });

        findViewById(R.id.btn_remove_image).setOnClickListener(v -> {
            imageOverlay.setImageDrawable(null);
            imageOverlay.setVisibility(View.GONE);
            imageUri = null;
        });

        //Напоминания

        ImageButton btnSetReminder = findViewById(R.id.reminder_button);
        btnSetReminder.setOnClickListener(v -> {
            ReminderHelper.showDateTimePicker(NoteActivity.this, selectedTime -> {
                reminderTimeMillis = selectedTime;
                Toast.makeText(this, "Напоминание выбрано!", Toast.LENGTH_SHORT).show();
            });
        });

        //Аудио

        audioHelper = new AudioHelper(this, inputField);

        audioButton.setOnClickListener(v -> {
            hideOtherPanelsExcept(audioPanelContainer);
            togglePanelWithAnimation(audioPanelContainer);
        });

        insertButton.setOnClickListener(v -> {
            if (!audioHelper.isRecording()) {
                audioHelper.startRecording();
                insertButton.setAlpha(0.5f);
            } else {
                audioHelper.stopRecording();
                audioHelper.insertAudioIcon(audioHelper.getAudioPath());
                insertButton.setAlpha(1f);
            }
        });

        recordingButton.setOnClickListener(v -> {
            audioHelper.selectAudioFile();
        });

        removeButton.setOnClickListener(v -> {
            audioHelper.removeAudio();
        });

        View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();

            int keypadHeight = screenHeight - r.bottom;
            boolean isKeyboardVisible = keypadHeight > screenHeight * 0.15;

            View drawingToolsPanel = findViewById(R.id.drawing_tools_panel);
            View mergeImagePanel = findViewById(R.id.merge_image_panel);
            View mergeAudioPanel = findViewById(R.id.merge_audio_panel);
            View bottomBar = findViewById(R.id.bottom_bar);

            if (isKeyboardVisible) {
                drawingToolsPanel.setVisibility(View.GONE);
                mergeImagePanel.setVisibility(View.GONE);
                mergeAudioPanel.setVisibility(View.GONE);
                bottomBar.setVisibility(View.GONE);
            } else {
                bottomBar.setVisibility(View.VISIBLE);
            }
        });

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            if (!hasChanges()) {
                finish();
                return;
            }

            new AlertDialog.Builder(NoteActivity.this)
                    .setTitle("Сохранить изменения?")
                    .setMessage("Хотите сохранить заметку?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        saveNote();
                    })
                    .setNegativeButton("Нет", (dialog, which) -> {
                        finish();
                    })
                    .show();
        });

        noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            loadNoteFromDatabase(noteId);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AudioHelper.REQUEST_AUDIO_FILE && resultCode == RESULT_OK && data != null) {
            Uri audioUri = data.getData();
            if (audioUri != null) {
                String copiedPath = copyAudioToLocalStorage(audioUri);
                if (copiedPath != null) {
                    audioHelper.stopPlaying(null);
                    audioHelper.setAudioPath(copiedPath);
                    audioHelper.insertAudioIcon(copiedPath);
                    Toast.makeText(this, "Аудио добавлено", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Ошибка при добавлении аудио", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == ImageHelper.REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                imageOverlay.setImageURI(imageUri);
                imageOverlay.setVisibility(View.VISIBLE);
            }
        } else if (requestCode == ImageHelper.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageOverlay.setVisibility(View.VISIBLE);
        }
    }


    private void checkSelectionStyles() {
        int start = noteInput.getSelectionStart();
        int end = noteInput.getSelectionEnd();
        updateStyleButtonStates(start, end);
    }

    private void selectPaintWidth(ImageButton selected, ImageButton... others) {
        selected.setSelected(true);
        for (ImageButton b : others) {
            b.setSelected(false);
        }
    }

    private void updateStyleButtonStates(int start, int end) {
        Editable text = noteInput.getText();

        ImageButton boldBtn = findViewById(R.id.bold_button);
        ImageButton italicBtn = findViewById(R.id.italic_button);
        ImageButton underlineBtn = findViewById(R.id.underline_button);
        ImageButton strikeBtn = findViewById(R.id.strike_button);

        boldBtn.setSelected(
                isSpanFullyApplied(text, start, end, StyleSpan.class) &&
                        hasStyle(text, start, end, Typeface.BOLD)
        );

        italicBtn.setSelected(
                isSpanFullyApplied(text, start, end, StyleSpan.class) &&
                        hasStyle(text, start, end, Typeface.ITALIC)
        );

        underlineBtn.setSelected(
                isSpanFullyApplied(text, start, end, UnderlineSpan.class)
        );

        strikeBtn.setSelected(
                isSpanFullyApplied(text, start, end, StrikethroughSpan.class)
        );

//        ImageButton alignLeft = findViewById(R.id.align_left);
//        ImageButton alignCenter = findViewById(R.id.align_center);
//        ImageButton alignRight = findViewById(R.id.align_right);
//
//        Layout layout = noteInput.getLayout();
//        if (layout != null && start < noteInput.length()) {
//            int line = layout.getLineForOffset(start);
//            Layout.Alignment alignment = layout.getParagraphAlignment(line);
//            currentAlignment = alignment;
//
//            alignLeft.setSelected(alignment == Layout.Alignment.ALIGN_NORMAL);
//            alignCenter.setSelected(alignment == Layout.Alignment.ALIGN_CENTER);
//            alignRight.setSelected(alignment == Layout.Alignment.ALIGN_OPPOSITE);
//        }
    }

    private boolean isSpanFullyApplied(Editable text, int start, int end, Class<?> spanClass) {
        Object[] spans = text.getSpans(start, end, spanClass);

        for (Object span : spans) {
            int spanStart = text.getSpanStart(span);
            int spanEnd = text.getSpanEnd(span);

            if (spanStart <= start && spanEnd >= end) {
                return true;
            }
        }

        return false;
    }

    private boolean hasStyle(Editable text, int start, int end, int styleType) {
        StyleSpan[] spans = text.getSpans(start, end, StyleSpan.class);

        for (StyleSpan span : spans) {
            int spanStart = text.getSpanStart(span);
            int spanEnd = text.getSpanEnd(span);

            if (spanStart <= start && spanEnd >= end && span.getStyle() == styleType) {
                return true;
            }
        }

        return false;
    }

    private void togglePanelWithAnimation(LinearLayout panel) {
        if (panel.getVisibility() == LinearLayout.VISIBLE) {
            panel.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> panel.setVisibility(LinearLayout.GONE))
                    .start();
        } else {
            panel.setAlpha(0f);
            panel.setVisibility(LinearLayout.VISIBLE);
            panel.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
        }
    }

    private void hideOtherPanelsExcept(View toShow) {
        LinearLayout[] panels = {textPanelContainer, drawingPanel, imagePanel, audioPanelContainer};

        for (LinearLayout panel : panels) {
            if (panel != toShow && panel.getVisibility() == View.VISIBLE) {
                TextFormatting.animateHide(panel);
            }
        }
    }

    private void highlightSearchResults(EditText editText, String query) {
        String text = editText.getText().toString();
        SpannableString spannable = new SpannableString(text);

        BackgroundColorSpan[] spans = spannable.getSpans(0, text.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : spans) {
            spannable.removeSpan(span);
        }

        if (!query.isEmpty()) {
            int index = text.toLowerCase().indexOf(query.toLowerCase());
            while (index >= 0) {
                int end = index + query.length();
                spannable.setSpan(
                        new BackgroundColorSpan(Color.YELLOW),
                        index,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                index = text.toLowerCase().indexOf(query.toLowerCase(), end);
            }
        }

        editText.setText(spannable);
        editText.setSelection(editText.length());
    }

    private String getImagePathIfExists() {
        if (imageUri != null && imageOverlay.getVisibility() == View.VISIBLE) {
            return imageUri.toString();
        }
        return null;
    }

    private String getAudioPathIfExists() {
        String path = audioHelper.getAudioPath();
        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            if (file.exists()) {
                return path;
            }
        }
        return null;
    }

    private void showImageIfExists(String path) {
        if (path != null && !path.isEmpty()) {
            imageUri = Uri.parse(path);
            imageOverlay.setImageURI(imageUri);
            imageOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void showAudioIfExists(String path) {
        if (path != null && !path.isEmpty()) {
            audioHelper.insertAudioIcon(path);
        }
    }

    private String copyAudioToLocalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File audioDir = new File(getExternalFilesDir(null), "audio_notes");
            if (!audioDir.exists()) audioDir.mkdirs();

            String fileName = "imported_" + System.currentTimeMillis() + ".3gp";
            File outFile = new File(audioDir, fileName);

            OutputStream outputStream = new FileOutputStream(outFile);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            return outFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void showDrawingIfExists(String path) {
        drawingView = findViewById(R.id.drawing_view);
        if (drawingView == null || path == null) return;

        File file = new File(path);
        Log.d("DrawingPath", "Loading drawing from: " + path);

        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                Log.d("DrawingLoad", "Bitmap decoded successfully, size: " +
                        bitmap.getWidth() + "x" + bitmap.getHeight());

                drawingView.post(() -> {
                    drawingView.setBitmap(bitmap);
                    Log.d("DrawingLoad", "Bitmap set to drawingView");
                });

            } else {
                Log.e("DrawingLoad", "Bitmap is null for path: " + path);
            }
        } else {
            Log.e("DrawingLoad", "File does not exist: " + path);
        }
    }

    private void saveNote() {

        if (getIntent().hasExtra("reminder_millis")) {
            reminderTimeMillis = getIntent().getLongExtra("reminder_millis", -1);
        }

        NotesDatabaseHelper dbHelper = new NotesDatabaseHelper(this);

        String text = Html.toHtml(noteInput.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        String title = titleInput.getText().toString().trim();


        if (title.isEmpty() && text.isEmpty()) {
            Toast.makeText(this, "Пустая заметка не сохранена", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
            return;
        }

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        String drawingPath = null;
        String imagePath   = null;
        String audioPath   = null;

        if (drawingView != null) {
            String newDrawingPath = DrawingHelper.saveDrawing(this, drawingView);
            Log.d("DrawingSave", "Saved path: " + newDrawingPath);
            if (newDrawingPath != null) {
                drawingPath = newDrawingPath;
            }
        }

        imagePath = getImagePathIfExists();
        audioPath = getAudioPathIfExists();

        Note note = noteId != -1
                ? dbHelper.getNoteById(noteId)
                : new Note();

        note.setTitle(title);
        note.setText(text);
        note.setDate(date);
        note.setDrawingPath(drawingPath);
        note.setImagePath(imagePath);
        note.setAudioPath(audioPath);
        note.setReminderTimeMillis(reminderTimeMillis);

        if (noteId != -1) {
            dbHelper.updateNote(note);
        } else {
            long newId = dbHelper.addNote(note);
            note.setId((int)newId);
        }

        if (reminderTimeMillis > 0) {
            ReminderHelper.scheduleAlarms(
                    this,
                    reminderTimeMillis,
                    note.getId(),
                    title.isEmpty() ? "Заметка без названия" : title
            );
        }

        Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("new_note_id", note.getId());
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    private void loadNoteFromDatabase(int id) {
        NotesDatabaseHelper dbHelper = new NotesDatabaseHelper(this);
        note = dbHelper.getNoteById(id);

        if (note != null) {
            titleInput.setText(note.getTitle());
            noteInput.setText(Html.fromHtml(note.getText(), Html.FROM_HTML_MODE_LEGACY));
            originalTitle = note.getTitle();
            originalText = note.getText();

            String drawingPath = note.getDrawingPath();
            Log.d("LoadNote", "Loaded drawingPath: " + drawingPath);
            if (drawingPath != null) {
                showDrawingIfExists(drawingPath);
            }
            showImageIfExists(note.getImagePath());
            showAudioIfExists(note.getAudioPath());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioHelper.release();
    }
}
