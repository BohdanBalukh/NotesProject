package com.example.notesproject;

import androidx.cardview.widget.CardView;

import com.example.notesproject.Models.Notes;
import com.google.android.material.card.MaterialCardView;

public interface NotesClickListener {
    void onClick(Notes notes);
    void onLongClick(Notes notes, MaterialCardView cardView);
}
