package com.example.tahmidsnotes;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class NotesViewModel extends ViewModel {
    private NoteRepository noteRepository;
    private MutableLiveData<List<Note>> noteListLiveData;
    private MutableLiveData<Boolean> isNoteListLoading;

    public NotesViewModel() {
        noteRepository = new NoteRepository();
        isNoteListLoading = new MutableLiveData<>();
        isNoteListLoading.setValue(true);

        noteRepository.fetchNotes(new NoteRepository.UiUpdateCallback() {
            @Override
            public void onComplete(List<Note> notes) {
                noteListLiveData.postValue(notes);
                isNoteListLoading.postValue(false);
            }
        });
    }

    public MutableLiveData<List<Note>> getNoteListLiveData() {
        if (noteListLiveData == null) {
            noteListLiveData = new MutableLiveData<>();
        }

        return noteListLiveData;
    }

    public MutableLiveData<Boolean> getIsNoteListLoading() {
        return isNoteListLoading;
    }
}
