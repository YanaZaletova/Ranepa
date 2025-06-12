package com.example.your_note;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;
    private Context context;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
    }

    private OnNoteClickListener listener;

    public NotesAdapter(Context context, List<Note> notes, OnNoteClickListener listener) {
        this.context = context;
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.title.setText(note.getTitle() != null ? note.getTitle() : "Без названия");

        if (note.getText() != null && !note.getText().isEmpty()) {
            holder.text.setText(Html.fromHtml(note.getText(), Html.FROM_HTML_MODE_LEGACY));
            holder.text.setVisibility(View.VISIBLE);
        } else {
            holder.text.setVisibility(View.GONE);
        }

        if (note.getImagePath() != null && !note.getImagePath().isEmpty()) {
            holder.image.setImageURI(Uri.parse(note.getImagePath()));
            holder.image.setVisibility(View.VISIBLE);
        } else if (note.getDrawingPath() != null && !note.getDrawingPath().isEmpty()) {
            holder.image.setImageURI(Uri.parse(note.getDrawingPath()));
            holder.image.setVisibility(View.VISIBLE);
        } else {
            holder.image.setVisibility(View.GONE);
        }

        if (note.getAudioPath() != null && !note.getAudioPath().isEmpty()) {
            holder.audioIcon.setVisibility(View.VISIBLE);
        } else {
            holder.audioIcon.setVisibility(View.GONE);
        }

        holder.date.setText(note.getDate() != null ? note.getDate() : "");

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));

        holder.itemView.setOnLongClickListener(v -> {
            listener.onNoteLongClick(note);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, text, date;
        ImageView image, audioIcon;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            text = itemView.findViewById(R.id.text);
            image = itemView.findViewById(R.id.image);
            audioIcon = itemView.findViewById(R.id.audio_icon);
            date = itemView.findViewById(R.id.date);
        }
    }
}
