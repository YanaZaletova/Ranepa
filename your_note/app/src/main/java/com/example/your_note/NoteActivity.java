package com.example.your_note;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note);

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

        FrameLayout inputContainer = findViewById(R.id.input_container);
        drawingView = new DrawingView(this);
        drawingView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        inputContainer.addView(drawingView);
        drawingView.setVisibility(View.GONE);
        drawingToolsPanel.setVisibility(View.GONE);

        ImageButton drawButton = findViewById(R.id.draw_button);
        drawButton.setOnClickListener(v -> {
            boolean isDrawingMode = (drawingToolsPanel.getVisibility() == View.VISIBLE);

            if (isDrawingMode) {
                drawingToolsPanel.setVisibility(View.GONE);
                drawingView.setVisibility(View.GONE);
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
                drawingToolsPanel.setVisibility(View.VISIBLE);
                drawingView.setVisibility(View.VISIBLE);
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

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
}
