package com.example.your_note;

public class Note {
    private int id;
    private String title;
    private String text;
    private String date;
    private String drawingPath;
    private String imagePath;
    private long reminderTimeMillis;
    private String audioPath;
    private String category;

    public int getId() { return id; }
    public String getTitle() { return title; }

    public String getText() { return text; }
    public String getDate() { return date; }
    public String getDrawingPath() { return drawingPath; }
    public String getImagePath() { return imagePath; }
    public long getReminderTimeMillis() { return reminderTimeMillis; }
    public String getAudioPath() { return audioPath; }

    public String getCategory() { return category; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
    public void setDate(String date) { this.date = date; }
    public void setDrawingPath(String drawingPath) { this.drawingPath = drawingPath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setReminderTimeMillis(long reminderTimeMillis) { this.reminderTimeMillis = reminderTimeMillis; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }


    public void setCategory(String category) { this.category = category; }
}
