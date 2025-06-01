package com.example.your_note;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {

    private final List<ChecklistItem> items;

    public ChecklistAdapter(List<ChecklistItem> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        EditText editText;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.check_box);
            editText = itemView.findViewById(R.id.checklist_text);
        }

        public void bind(ChecklistItem item) {
            editText.setText(item.getText());
            checkBox.setChecked(item.isChecked());
            updateStyle(item.isChecked());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setChecked(isChecked);
                updateStyle(isChecked);
            });

            editText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    item.setText(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        private void updateStyle(boolean checked) {
            if (checked) {
                editText.setPaintFlags(editText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                editText.setTextColor(Color.GRAY);
            } else {
                editText.setPaintFlags(editText.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                editText.setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    public ChecklistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checklist_item, parent, false);
        return new ChecklistAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChecklistAdapter.ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem() {
        items.add(new ChecklistItem("", false));
        notifyItemInserted(items.size() - 1);
    }
}
