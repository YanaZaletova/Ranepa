package com.example.your_note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.your_note.Note;

import java.util.ArrayList;
import java.util.List;

public class NotesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "notes.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "notes";

    public NotesDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "title TEXT,"
                + "text TEXT,"
                + "date TEXT,"
                + "imagePath TEXT,"
                + "audioPath TEXT,"
                + "drawingPath TEXT,"
                + "reminderTime TEXT"
                + ")";
        db.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("title", note.getTitle());
        values.put("text", note.getText());
        values.put("date", note.getDate());
        values.put("imagePath", note.getImagePath());
        values.put("audioPath", note.getAudioPath());
        values.put("drawingPath", note.getDrawingPath());
        values.put("reminderTime", note.getReminderTime());

        return db.insert(TABLE_NAME, null, values);
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, "date DESC");

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                note.setText(cursor.getString(cursor.getColumnIndexOrThrow("text")));
                note.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                note.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("imagePath")));
                note.setAudioPath(cursor.getString(cursor.getColumnIndexOrThrow("audioPath")));
                note.setDrawingPath(cursor.getString(cursor.getColumnIndexOrThrow("drawingPath")));
                note.setReminderTime(cursor.getString(cursor.getColumnIndexOrThrow("reminderTime")));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return notes;
    }

    public Note getNoteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Note note = new Note();
            note.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            note.setText(cursor.getString(cursor.getColumnIndexOrThrow("text")));
            note.setDate(cursor.getString(cursor.getColumnIndexOrThrow("date")));
            note.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("imagePath")));
            note.setAudioPath(cursor.getString(cursor.getColumnIndexOrThrow("audioPath")));
            note.setDrawingPath(cursor.getString(cursor.getColumnIndexOrThrow("drawingPath")));
            note.setReminderTime(cursor.getString(cursor.getColumnIndexOrThrow("reminderTime")));

            cursor.close();
            return note;
        }

        return null;
    }

    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("title", note.getTitle());
        values.put("text", note.getText());
        values.put("date", note.getDate());
        values.put("imagePath", note.getImagePath());
        values.put("audioPath", note.getAudioPath());
        values.put("drawingPath", note.getDrawingPath());
        values.put("reminderTime", note.getReminderTime());

        return db.update(TABLE_NAME, values, "id = ?", new String[]{String.valueOf(note.getId())});
    }

    public int deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(id)});
    }
}
