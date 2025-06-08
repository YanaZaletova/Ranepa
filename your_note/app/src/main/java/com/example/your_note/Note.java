package com.example.your_note;

public class Note {
    private int id;
    private String title;
    private String text;
    private String date;
    private String drawingPath;
    private String imagePath;
    private String reminderTime;
    private String audioPath;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getDate() { return date; }
    public String getDrawingPath() { return drawingPath; }
    public String getImagePath() { return imagePath; }
    public String getReminderTime() { return reminderTime; }
    public String getAudioPath() { return audioPath; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
    public void setDate(String date) { this.date = date; }
    public void setDrawingPath(String drawingPath) { this.drawingPath = drawingPath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }
}
