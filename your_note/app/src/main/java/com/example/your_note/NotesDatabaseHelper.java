package com.example.your_note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class NotesDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "notes.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "notes";

    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_TEXT = "text";
    private static final String COL_DATE = "date";
    private static final String COL_IMAGE_PATH = "imagePath";
    private static final String COL_AUDIO_PATH = "audioPath";
    private static final String COL_DRAWING_PATH = "drawingPath";
    private static final String COL_REMINDER_TIME = "reminderTimeMillis";

    public NotesDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT, "
                + COL_TEXT + " TEXT, "
                + COL_DATE + " TEXT, "
                + COL_IMAGE_PATH + " TEXT, "
                + COL_AUDIO_PATH + " TEXT, "
                + COL_DRAWING_PATH + " TEXT, "
                + COL_REMINDER_TIME + " INTEGER"
                + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addNote(Note note) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, note.getTitle());
            values.put(COL_TEXT, note.getText());
            values.put(COL_DATE, note.getDate());
            values.put(COL_IMAGE_PATH, note.getImagePath());
            values.put(COL_AUDIO_PATH, note.getAudioPath());
            values.put(COL_DRAWING_PATH, note.getDrawingPath());
            values.put(COL_REMINDER_TIME, note.getReminderTimeMillis());

            return db.insert(TABLE_NAME, null, values);
        }
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, COL_DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                    note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
                    note.setText(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEXT)));
                    note.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
                    note.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH)));
                    note.setAudioPath(cursor.getString(cursor.getColumnIndexOrThrow(COL_AUDIO_PATH)));
                    note.setDrawingPath(cursor.getString(cursor.getColumnIndexOrThrow(COL_DRAWING_PATH)));
                    note.setReminderTimeMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COL_REMINDER_TIME)));

                    notes.add(note);
                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return notes;
    }

    public Note getNoteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_NAME, null, COL_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                Note note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
                note.setText(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEXT)));
                note.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)));
                note.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH)));
                note.setAudioPath(cursor.getString(cursor.getColumnIndexOrThrow(COL_AUDIO_PATH)));
                note.setDrawingPath(cursor.getString(cursor.getColumnIndexOrThrow(COL_DRAWING_PATH)));
                note.setReminderTimeMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COL_REMINDER_TIME)));

                return note;
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return null;
    }

    public int updateNote(Note note) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, note.getTitle());
            values.put(COL_TEXT, note.getText());
            values.put(COL_DATE, note.getDate());
            values.put(COL_IMAGE_PATH, note.getImagePath());
            values.put(COL_AUDIO_PATH, note.getAudioPath());
            values.put(COL_DRAWING_PATH, note.getDrawingPath());
            values.put(COL_REMINDER_TIME, note.getReminderTimeMillis());

            return db.update(TABLE_NAME, values, COL_ID + " = ?", new String[]{String.valueOf(note.getId())});
        }
    }

    public int deleteNote(int id) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            return db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(id)});
        }
    }
}
