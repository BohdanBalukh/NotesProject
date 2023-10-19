package com.example.notesproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notesproject.Models.Notes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class NotesAddingActivity extends AppCompatActivity {
    private EditText editText_title, editText_category,editText_notes;
    private ImageView imageView_save,imageView_back, imageView_undo,imageView_redo;
    private TextViewUndoRedo helper;
    private Notes notes;
    private boolean isOldNote = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_adding);
        imageView_back = findViewById(R.id.imageView_back);
        imageView_save = findViewById(R.id.imageView_save);
        imageView_undo=findViewById(R.id.imageView_undo);
        imageView_redo=findViewById(R.id.imageView_redo);
        editText_title = findViewById(R.id.editText_title);
        editText_category=findViewById(R.id.editText_category);
        editText_notes = findViewById(R.id.editText_notes);
        notes = new Notes();
        try{
        notes = (Notes) getIntent().getSerializableExtra("old_note");
        editText_title.setText(notes.getTitle());
        editText_category.setText(notes.getCategory());
        editText_notes.setText(notes.getNotes());
        isOldNote = true;
       }catch(Exception e){
            e.printStackTrace();
        }

        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editText_title.getText().toString();
                String category = editText_category.getText().toString();
                String descriptions = editText_notes.getText().toString();
                if(descriptions.trim().isEmpty()||title.trim().isEmpty()||category.trim().isEmpty()){
                    Toast.makeText(NotesAddingActivity.this,"Empty fields!",Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
                Date noteDate = new Date();

                if(!isOldNote){
                    notes = new Notes();
                }
                notes.setTitle(title);
                notes.setCategory(category);
                notes.setNotes(descriptions);
                notes.setDate(simpleDateFormat.format(noteDate));

                Intent intent = new Intent();
                intent.putExtra("note",notes);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

        });
        imageView_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
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

}