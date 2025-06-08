package com.example.your_note;

import android.widget.EditText;

public class ChecklistHelper {
    public static void insertChecklistItem(EditText noteInput) {
        int cursorPosition = noteInput.getSelectionStart();
        String currentText = noteInput.getText().toString();
        String checklistPrefix = "\u2022 ";
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
}
