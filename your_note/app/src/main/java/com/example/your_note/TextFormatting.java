package com.example.your_note;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class TextFormatting {

    public static void toggleFormattingPanel(View panel) {
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

    public static void animateHide(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }

    public static void applyBold(EditText editText) {
        applyStyleSpan(editText, new StyleSpan(Typeface.BOLD), StyleSpan.class);
    }

    public static void applyItalic(EditText editText) {
        applyStyleSpan(editText, new StyleSpan(Typeface.ITALIC), StyleSpan.class);
    }

    public static void applyUnderline(EditText editText) {
        applyStyleSpan(editText, new UnderlineSpan(), UnderlineSpan.class);
    }

    public static void applyStrikethrough(EditText editText) {
        applyStyleSpan(editText, new StrikethroughSpan(), StrikethroughSpan.class);
    }

    public static void applyTextColor(EditText editText, int color) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start == end) return;

        Editable editable = editText.getText();
        ForegroundColorSpan[] spans = editable.getSpans(start, end, ForegroundColorSpan.class);

        for (ForegroundColorSpan span : spans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            if (spanStart < end && spanEnd > start) {
                editable.removeSpan(span);
            }
        }

        editable.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void applyStyleSpan(EditText editText, Object newSpan, Class<?> spanClass) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == end) return;

        Editable editable = editText.getText();
        boolean spanExists = false;

        Object[] spans = editable.getSpans(start, end, spanClass);

        for (Object oldSpan : spans) {
            int spanStart = editable.getSpanStart(oldSpan);
            int spanEnd = editable.getSpanEnd(oldSpan);

            if (spanStart < end && spanEnd > start) {
                editable.removeSpan(oldSpan);
                spanExists = true;
            }
        }

        if (!spanExists) {
            editable.setSpan(newSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public static void toggleButton(ImageButton button) {
        button.setSelected(!button.isSelected());
    }

    public static void setAlignmentSelected(ImageButton selected, ImageButton... others) {
        selected.setSelected(true);
        for (ImageButton btn : others) {
            btn.setSelected(false);
        }
    }

    public static void clearColorSelection(ImageButton[] colorButtons) {
        for (ImageButton btn : colorButtons) {
            btn.setSelected(false);
        }
    }

    public static void applyAlignment(EditText editText, Layout.Alignment alignment) {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start == end) return;

        Editable editable = editText.getText();

        AlignmentSpan[] spans = editable.getSpans(start, end, AlignmentSpan.class);
        for (AlignmentSpan span : spans) {
            editable.removeSpan(span);
        }

        editable.setSpan(new AlignmentSpan.Standard(alignment), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
