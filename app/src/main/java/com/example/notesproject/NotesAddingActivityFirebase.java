package com.example.notesproject;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesproject.Models.NotesFirebase;
import com.example.notesproject.Utility.Document;
import com.example.notesproject.Utility.TextViewUndoRedo;
import com.example.notesproject.Utility.UiUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotesAddingActivityFirebase extends AppCompatActivity {

    private EditText editText_title, editText_category,editText_notes;
    private ProgressBar progressBar;
    private String title,category,note,stringDate, docId;
    private ImageView imageView_save,imageView_back, imageView_undo,imageView_redo;
    private TextView fullDateTime;
    private TextViewUndoRedo helper;
    private boolean isOldNote = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_adding_firebase);
        imageView_back = findViewById(R.id.imageView_back);
        imageView_save = findViewById(R.id.imageView_save);
        imageView_undo=findViewById(R.id.imageView_undo);
        imageView_redo=findViewById(R.id.imageView_redo);
        editText_title = findViewById(R.id.editText_title);
        editText_category=findViewById(R.id.editText_category);
        editText_notes = findViewById(R.id.editText_notes);
        fullDateTime = findViewById(R.id.fullDateTime);
        progressBar = findViewById(R.id.progressBar_save);

        try{
           Intent intent = getIntent();
           title = intent.getStringExtra("title");
           category = intent.getStringExtra("category");
           note = intent.getStringExtra("note");
           stringDate = intent.getStringExtra("date");
           docId = getIntent().getStringExtra("docId");

            if(docId!=null && !docId.isEmpty()){
                isOldNote = true;
            }
           editText_title.setText(title);
           editText_category.setText(category);
           editText_notes.setText(note);

           SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
           SimpleDateFormat outputFormat=new SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.getDefault());

           Date date = inputFormat.parse(stringDate);
           fullDateTime.setText(outputFormat.format(date));
           fullDateTime.setVisibility(View.VISIBLE);
        }
        catch (Exception e){
            e.printStackTrace();
        }


        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editText_title.getText().toString();
                String category = editText_category.getText().toString();
                String descriptions = editText_notes.getText().toString();

                if(descriptions.trim().isEmpty() && title.trim().isEmpty() && category.trim().isEmpty()){
                    onBackPressed();
                    return;
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
                Date noteDate = new Date();

                NotesFirebase notes = new NotesFirebase();
                notes.setTitle(title);
                notes.setCategory(category);
                notes.setNotes(descriptions);
                notes.setDate(simpleDateFormat.format(noteDate));

                saveNoteToFirebase(notes);
            }
        });
        imageView_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_reverse, R.anim.slide_out_reverse);}
        });

        helper = new TextViewUndoRedo(editText_notes);
        imageView_undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.undo();
            }
        });
        imageView_redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.redo();
            }
        });
    }

    void saveNoteToFirebase(NotesFirebase notesFirebase) {
        DocumentReference documentReference;
        UiUtils.changeInProgressAddingNote(progressBar,imageView_save,true);

        if (isOldNote) {
            // Отримати попередню нотатку для порівняння та оновлення
            documentReference = Document.getCollectionReferenceForNotes().document(docId);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot oldNoteSnapshot = task.getResult();
                        if (oldNoteSnapshot.exists()) {
                            boolean wasPinned = oldNoteSnapshot.getBoolean("pinned");

                            notesFirebase.setPinned(wasPinned);
                            documentReference.set(notesFirebase).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    UiUtils.changeInProgressAddingNote(progressBar,imageView_save,false);
                                    String message = task.isSuccessful() ? "Refreshed!" : "Error!";
                                    Toast.makeText(NotesAddingActivityFirebase.this, message, Toast.LENGTH_SHORT).show();

                                    if (task.isSuccessful()) {
                                        finish();
                                    }
                                }
                            });
                        }
                    }
                }
            });
        } else {
            documentReference = Document.getCollectionReferenceForNotes().document();
            documentReference.set(notesFirebase).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    UiUtils.changeInProgressAddingNote(progressBar,imageView_save,false);
                    String message = task.isSuccessful() ? "Added!" : "Error!";
                    Toast.makeText(NotesAddingActivityFirebase.this, message, Toast.LENGTH_SHORT).show();

                    if (task.isSuccessful()) {
                        finish();
                    }
                }
            });
        }
    }

}



