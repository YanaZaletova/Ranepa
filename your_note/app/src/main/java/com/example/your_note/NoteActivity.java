package com.example.your_note;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    private EditText titleInput, noteInput;
    private TextView time;
    private LinearLayout textFormattingPanel;
    private int currentTextColor = Color.BLACK;

    private DrawingView drawingView;
    private LinearLayout drawingToolsPanel;
    private boolean eraserEnabled = false;
    private int currentTextStyle = Typeface.NORMAL;

    private static final int REQUEST_PICK_IMAGE = 123;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private ImageView imageOverlay;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note);

        imageOverlay = findViewById(R.id.image_overlay);
        ImageButton btnCamera = findViewById(R.id.btn_camera);

        loadRecentImages();

        titleInput = findViewById(R.id.title);
        noteInput = findViewById(R.id.note_input);
        noteInput.setGravity(Gravity.START);
        time = findViewById(R.id.time);
        ImageButton backButton = findViewById(R.id.back_button);

        noteInput.addTextChangedListener(new TextWatcher() {
            private int start   = 0;
            private int count   = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int after) {
                this.start = st;
                this.count = after;
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int before, int c) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (count <= 0 || start < 0 || start + count > s.length()) return;

                android.text.style.StyleSpan[] styleSpans = s.getSpans(start, start + count, android.text.style.StyleSpan.class);
                for (android.text.style.StyleSpan span : styleSpans) {
                    s.removeSpan(span);
                }
                UnderlineSpan[] underlineSpans = s.getSpans(start, start + count, UnderlineSpan.class);
                for (UnderlineSpan span : underlineSpans) {
                    s.removeSpan(span);
                }
                StrikethroughSpan[] strikeSpans = s.getSpans(start, start + count, StrikethroughSpan.class);
                for (StrikethroughSpan span : strikeSpans) {
                    s.removeSpan(span);
                }
                android.text.style.ForegroundColorSpan[] colorSpans = s.getSpans(start, start + count, android.text.style.ForegroundColorSpan.class);
                for (android.text.style.ForegroundColorSpan span : colorSpans) {
                    s.removeSpan(span);
                }

                if ((currentTextStyle & Typeface.BOLD) != 0) {
                    s.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), start, start + count, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if ((currentTextStyle & Typeface.ITALIC) != 0) {
                    s.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), start, start + count, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (isUnderline) {
                    s.setSpan(new UnderlineSpan(), start, start + count, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (isStrikethrough) {
                    s.setSpan(new StrikethroughSpan(), start, start + count, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (currentTextColor != Color.BLACK) {
                    s.setSpan(new android.text.style.ForegroundColorSpan(currentTextColor), start, start + count, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

        });

        textFormattingPanel = findViewById(R.id.text_formatting_panel);
        textFormattingPanel.setVisibility(View.GONE);

        ImageButton textButton = findViewById(R.id.text_button);
        textButton.setOnClickListener(v -> toggleFormattingPanel(textFormattingPanel));

        ImageButton boldBtn    = findViewById(R.id.bold_button);
        ImageButton italicBtn  = findViewById(R.id.italic_button);
        ImageButton underlineBtn = findViewById(R.id.underline_button);
        ImageButton strikeBtn  = findViewById(R.id.strike_button);
        ImageButton alignLeft  = findViewById(R.id.align_left);
        ImageButton alignCenter= findViewById(R.id.align_center);
        ImageButton alignRight = findViewById(R.id.align_right);

        boldBtn.setOnClickListener(v -> toggleStyle(boldBtn, Typeface.BOLD));
        italicBtn.setOnClickListener(v -> toggleStyle(italicBtn, Typeface.ITALIC));
        strikeBtn.setOnClickListener(v -> toggleStrikethrough(strikeBtn));
        underlineBtn.setOnClickListener(v -> toggleUnderline(underlineBtn));

        int[] colorIds = {
                R.id.color_red, R.id.color_orange, R.id.color_yellow, R.id.color_green,
                R.id.color_light_blue, R.id.color_blue, R.id.color_purple, R.id.color_black
        };
        int[] colors = {
                Color.RED, 0xFFFF9800, 0xFFFFFF00, Color.GREEN,
                0xFF03A9F4, Color.BLUE, 0xFF9C27B0, Color.BLACK
        };
        for (int i = 0; i < colorIds.length; i++) {
            final int col = colors[i];
            final ImageButton btn = findViewById(colorIds[i]);
            btn.setOnClickListener(v -> {
                currentTextColor = col;
                clearColorSelection();
                btn.setSelected(true);
            });
        }

        alignLeft.setOnClickListener(v -> {
            noteInput.setGravity(Gravity.START);
            setAlignmentSelected(alignLeft, alignCenter, alignRight);
        });
        alignCenter.setOnClickListener(v -> {
            noteInput.setGravity(Gravity.CENTER_HORIZONTAL);
            setAlignmentSelected(alignCenter, alignLeft, alignRight);
        });
        alignRight.setOnClickListener(v -> {
            noteInput.setGravity(Gravity.END);
            setAlignmentSelected(alignRight, alignLeft, alignCenter);
        });

        ImageButton checklistButton = findViewById(R.id.checklist_button);
        checklistButton.setOnClickListener(v -> insertChecklistItem());

        Intent intent = getIntent();
        String dateTime = intent.getStringExtra("time");
        if (dateTime != null && !dateTime.isEmpty()) {
            time.setText(dateTime);
        } else {
            time.setText(getCurrentTime());
        }

        drawingToolsPanel = findViewById(R.id.drawing_tools_panel);

        FrameLayout drawingContainer = findViewById(R.id.drawing_container);
        drawingView = new DrawingView(this);
        drawingView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        drawingContainer.addView(drawingView);

        drawingView.setVisibility(View.GONE);
        drawingToolsPanel.setVisibility(View.GONE);

        ImageButton drawButton = findViewById(R.id.draw_button);
        drawButton.setOnClickListener(v -> {
            boolean isDrawingMode = (drawingToolsPanel.getVisibility() == View.VISIBLE);

            if (isDrawingMode) {
                animateHide(drawingToolsPanel);
                animateHide(drawingView);
                noteInput.setVisibility(View.VISIBLE);
                textFormattingPanel.setVisibility(View.VISIBLE);

                eraserEnabled = false;
                drawingView.enableEraser(false);
                drawingView.setPaintColor(Color.BLACK);
                drawingView.setStrokeWidth(10f);
                selectEraserButton(false);
                selectColorButton(R.id.color_black);
                selectStrokeWidthButton(R.id.paint_medium);

            } else {
                noteInput.setVisibility(View.GONE);
                textFormattingPanel.setVisibility(View.GONE);
                animateShow(drawingToolsPanel);
                animateShow(drawingView);
            }
        });

        findViewById(R.id.clean).setOnClickListener(v -> drawingView.clear());

        int[] colorButtons = {
                R.id.draw_color_red, R.id.draw_color_orange, R.id.draw_color_yellow,
                R.id.draw_color_green, R.id.draw_color_light_blue, R.id.draw_color_blue,
                R.id.draw_color_purple, R.id.draw_color_black
        };
        int[] colorValues = {
                Color.RED, 0xFFFF9800, 0xFFFFFF00,
                Color.GREEN, 0xFF03A9F4, Color.BLUE,
                0xFF9C27B0, Color.BLACK
        };
        for (int i = 0; i < colorButtons.length; i++) {
            final int col = colorValues[i];
            final int btnId = colorButtons[i];
            findViewById(btnId).setOnClickListener(v -> {
                eraserEnabled = false;
                drawingView.enableEraser(false);
                drawingView.setPaintColor(col);
                selectColorButton(btnId);
                selectEraserButton(false);
                selectStrokeWidthButton(getCurrentStrokeWidthButtonId());
            });
        }

        findViewById(R.id.paint_thin).setOnClickListener(v -> {
            drawingView.setStrokeWidth(5f);
            selectStrokeWidthButton(R.id.paint_thin);
            drawingView.enableEraser(eraserEnabled);
            selectEraserButton(eraserEnabled);
        });
        findViewById(R.id.paint_medium).setOnClickListener(v -> {
            drawingView.setStrokeWidth(10f);
            selectStrokeWidthButton(R.id.paint_medium);
            drawingView.enableEraser(eraserEnabled);
            selectEraserButton(eraserEnabled);
        });
        findViewById(R.id.paint_thick).setOnClickListener(v -> {
            drawingView.setStrokeWidth(20f);
            selectStrokeWidthButton(R.id.paint_thick);
            drawingView.enableEraser(eraserEnabled);
            selectEraserButton(eraserEnabled);
        });

        findViewById(R.id.paint_eraser).setOnClickListener(v -> {
            eraserEnabled = !eraserEnabled;
            drawingView.enableEraser(eraserEnabled);
            selectEraserButton(eraserEnabled);

            if (eraserEnabled) {
                clearStrokeSelection();
                clearColorSelection();
            } else {
                selectColorButton(R.id.color_black);
            }
            selectStrokeWidthButton(getCurrentStrokeWidthButtonId());
        });

        drawingView.setPaintColor(Color.BLACK);
        drawingView.setStrokeWidth(10f);
        eraserEnabled = false;
        selectStrokeWidthButton(R.id.paint_medium);
        selectColorButton(R.id.color_black);
        selectEraserButton(false);

        ImageButton imageButton = findViewById(R.id.image_button);
        View imagePanelContainer = findViewById(R.id.merge_image_panel);

        int newHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        ViewGroup.LayoutParams params = imageOverlay.getLayoutParams();
        params.height = newHeight;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        imageOverlay.setLayoutParams(params);

        imageOverlay.post(() -> {
            int imageHeight = imageOverlay.getHeight();
            noteInput.setPadding(
                    noteInput.getPaddingLeft(),
                    imageHeight + 16,
                    noteInput.getPaddingRight(),
                    noteInput.getPaddingBottom()
            );
        });

        imageButton.setOnClickListener(v -> toggleFormattingPanel(imagePanelContainer));
        btnCamera.setOnClickListener(v -> openCamera());
        ImageButton addImageButton = findViewById(R.id.btn_add_image);
        addImageButton.setOnClickListener(v -> openGallery());
        ImageButton btnRemoveImage = findViewById(R.id.btn_remove_image);
        btnRemoveImage.setOnClickListener(v -> {
            imageOverlay.setImageDrawable(null);
            imageOverlay.setVisibility(View.GONE);
        });

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

    private void toggleFormattingPanel(View panel) {
        if (panel.getVisibility() == View.GONE) {
            panel.setAlpha(0f);
            panel.setVisibility(View.VISIBLE);
            panel.animate().alpha(1f).setDuration(200).start();
        } else {
            panel.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> panel.setVisibility(View.GONE))
                    .start();
        }
    }

    private void animateShow(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(1f).setDuration(200).start();
        }
    }

    private void animateHide(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }

    private boolean isUnderline = false;
    private boolean isStrikethrough = false;

    private void toggleStyle(ImageButton button, int style) {
        button.setSelected(!button.isSelected());
        if (button.isSelected()) {
            currentTextStyle |= style;
        } else {
            currentTextStyle &= ~style;
        }
    }

    private void toggleUnderline(ImageButton button) {
        isUnderline = !isUnderline;
        button.setSelected(isUnderline);
    }

    private void toggleStrikethrough(ImageButton button) {
        isStrikethrough = !isStrikethrough;
        button.setSelected(isStrikethrough);
    }


    private void setAlignmentSelected(ImageButton selected, ImageButton... others) {
        selected.setSelected(true);
        for (ImageButton btn : others) btn.setSelected(false);
    }

    private void insertChecklistItem() {
        int cursorPosition = noteInput.getSelectionStart();
        String currentText = noteInput.getText().toString();
        String checklistPrefix = "• ";
        StringBuilder newText = new StringBuilder(currentText);
        if (cursorPosition > 0 && currentText.charAt(cursorPosition - 1) != '\n') {
            newText.insert(cursorPosition, "\n" + checklistPrefix);
            cursorPosition += checklistPrefix.length() + 1;
        } else {
            newText.insert(cursorPosition, checklistPrefix);
            cursorPosition += checklistPrefix.length();
        }
        noteInput.setText(newText.toString());
        noteInput.setSelection(cursorPosition);
    }

    private int getCurrentStrokeWidthButtonId() {
        float w = drawingView.getCurrentStrokeWidth();
        if (w <= 5f) return R.id.paint_thin;
        if (w <= 10f) return R.id.paint_medium;
        return R.id.paint_thick;
    }

    private void clearStrokeSelection() {
        findViewById(R.id.paint_thin).setSelected(false);
        findViewById(R.id.paint_medium).setSelected(false);
        findViewById(R.id.paint_thick).setSelected(false);
    }

    private void clearColorSelection() {
        int[] colorButtons = {
                R.id.color_red, R.id.color_orange, R.id.color_yellow,
                R.id.color_green, R.id.color_light_blue, R.id.color_blue,
                R.id.color_purple, R.id.color_black
        };
        for (int id : colorButtons) {
            findViewById(id).setSelected(false);
        }
    }

    private void selectStrokeWidthButton(int selectedId) {
        clearStrokeSelection();
        if (selectedId != -1) findViewById(selectedId).setSelected(true);
    }

    private void selectColorButton(int selectedId) {
        clearColorSelection();
        findViewById(selectedId).setSelected(true);
    }

    private void selectEraserButton(boolean enabled) {
        findViewById(R.id.paint_eraser).setSelected(enabled);
    }

    private void loadRecentImages() {
        LinearLayout previewRow = findViewById(R.id.image_preview_row);
        previewRow.removeAllViews();

        String[] projection = {MediaStore.Images.Media._ID};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        );

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int count = 0;

            while (cursor.moveToNext() && count < 5) {
                long id = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                FrameLayout frame = new FrameLayout(this);
                LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(200, 200, 1f);
                frameParams.setMargins(8, 0, 8, 0);
                frame.setLayoutParams(frameParams);

                frame.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_outline));
                frame.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
                frame.setClipToOutline(true);

                ImageView imageView = new ImageView(this);
                FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                imageView.setLayoutParams(imageParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Glide.with(this).load(contentUri).into(imageView);

                imageView.setOnClickListener(v -> {
                    imageOverlay.setImageURI(contentUri);
                    imageOverlay.setVisibility(View.VISIBLE);
                });

                frame.addView(imageView);
                previewRow.addView(frame);
                count++;
            }
            cursor.close();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + timeStamp + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/YourNote");

        photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (photoUri != null) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Не удалось создать файл", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageOverlay.setImageURI(photoUri);
            imageOverlay.setVisibility(View.VISIBLE);
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imageOverlay.setImageURI(selectedImageUri);
                imageOverlay.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadRecentImages();
        } else {
            Toast.makeText(this, "Необходимы разрешения", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
}
