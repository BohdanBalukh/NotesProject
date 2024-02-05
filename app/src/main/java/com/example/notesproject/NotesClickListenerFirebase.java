package com.example.notesproject;

import com.example.notesproject.Models.Notes;
import com.example.notesproject.Models.NotesFirebase;
import com.google.android.material.card.MaterialCardView;

public interface NotesClickListenerFirebase {
    void onClick(NotesFirebase notes, String docId);
    void onLongClick(NotesFirebase notes, MaterialCardView cardView);
}
