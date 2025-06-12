package com.example.your_note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class AudioHelper {

    public static final int REQUEST_AUDIO_FILE = 3;

    private MediaRecorder recorder;
    private MediaPlayer player;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private String audioPath;
    private ImageView currentAudioIcon;
    private final Activity activity;
    private final LinearLayout inputField;

    public AudioHelper(Activity activity, LinearLayout inputField) {
        this.activity = activity;
        this.inputField = inputField;
    }

    public void startRecording() {
        try {
            File audioDir = new File(activity.getExternalFilesDir(null), "audio_notes");
            if (!audioDir.exists()) audioDir.mkdirs();

            audioPath = audioDir.getAbsolutePath() + "/note_" + System.currentTimeMillis() + ".3gp";

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setOutputFile(audioPath);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.prepare();
            recorder.start();
            isRecording = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (recorder != null && isRecording) {
            recorder.stop();
            recorder.release();
            recorder = null;
            isRecording = false;
            Toast.makeText(activity, "Аудио записано: " + audioPath, Toast.LENGTH_SHORT).show();
        }
    }

    public void startPlaying(Context context, Uri uri, ImageView iconView) {
        stopPlaying(iconView);
        player = new MediaPlayer();
        try {
            player.setDataSource(context, uri);
            player.prepare();
            player.start();
            isPlaying = true;
            iconView.setAlpha(0.5f);

            player.setOnCompletionListener(mp -> stopPlaying(iconView));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка воспроизведения аудио", Toast.LENGTH_SHORT).show();
        }
    }

    public void pausePlaying(ImageView iconView) {
        if (player != null && player.isPlaying()) {
            player.pause();
            isPlaying = false;
            iconView.setAlpha(1f);
        }
    }

    public void stopPlaying(ImageView iconView) {
        if (player != null) {
            player.release();
            player = null;
        }
        isPlaying = false;
        if (iconView != null) {
            iconView.setAlpha(1f);
        }
    }

    public void selectAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        activity.startActivityForResult(intent, REQUEST_AUDIO_FILE);
    }

    public void insertAudioIcon(String path) {
        if (currentAudioIcon != null) {
            inputField.removeView(currentAudioIcon);
        }

        ImageView audioIcon = new ImageView(activity);
        audioIcon.setImageResource(R.drawable.audio_recording);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        audioIcon.setLayoutParams(params);
        audioIcon.setAdjustViewBounds(true);
        audioIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (inputField.getVisibility() != LinearLayout.VISIBLE) {
            inputField.setVisibility(LinearLayout.VISIBLE);
        }

        inputField.addView(audioIcon);

        Uri uri = Uri.parse(path);

        audioIcon.setOnClickListener(v -> {
            if (!isPlaying) {
                startPlaying(activity, uri, audioIcon);
            } else {
                pausePlaying(audioIcon);
            }
        });

        currentAudioIcon = audioIcon;
    }
    public void removeAudio() {
        stopPlaying(currentAudioIcon);

        if (currentAudioIcon != null && inputField.indexOfChild(currentAudioIcon) != -1) {
            inputField.removeView(currentAudioIcon);
            currentAudioIcon = null;
        }

        if (audioPath != null) {
            File file = new File(audioPath);
            if (file.exists() && audioPath.contains(activity.getExternalFilesDir(null).getAbsolutePath())) {
                file.delete();
            }
        }

        audioPath = null;

        Toast.makeText(activity, "Аудио удалено", Toast.LENGTH_SHORT).show();
    }


    public void release() {
        if (recorder != null) recorder.release();
        if (player != null) player.release();
    }

    public boolean isRecording() {
        return isRecording;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String path) {
        this.audioPath = path;
    }
}
