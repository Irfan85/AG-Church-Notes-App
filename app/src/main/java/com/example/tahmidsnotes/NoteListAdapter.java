package com.example.tahmidsnotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class NoteListAdapter extends ArrayAdapter<Note> {

    public NoteListAdapter(@NonNull Context context, @NonNull List<Note> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View noteListItem = convertView;

        if(noteListItem == null){
            noteListItem = LayoutInflater.from(getContext()).inflate(R.layout.note_list_item, parent, false);
        }

        Note currentNote = getItem(position);

        assert currentNote != null;
        TextView noteDescriptionTextView = noteListItem.findViewById(R.id.noteDescription);
        noteDescriptionTextView.setText(currentNote.getNoteDescription());

        TextView noteIssueDateTextView = noteListItem.findViewById(R.id.noteIssueDate);
        noteIssueDateTextView.setText(currentNote.getIssueDate());

        return noteListItem;
    }
}
