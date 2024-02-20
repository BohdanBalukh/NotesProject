package com.example.notesproject;



import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.notesproject.Models.NotesFirebase;
import com.example.notesproject.Utility.Document;
import com.example.notesproject.Utility.TextViewUndoRedo;
import com.example.notesproject.Utility.UiUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class NotesAddingActivityFirebase extends AppCompatActivity {
    private EditText editText_title, editText_category,editText_notes;
    private ProgressBar progressBar;
    private String title,category,note,stringDate, docId;
    private StorageReference storageReference;
    private Uri image;
    private ImageButton addImage, deleteImage, taskReminder;
    private ImageView imageView_save,imageView_back, imageView_undo,imageView_redo;
    private RoundedImageView imageNote;
    private TextView fullDateTime, textForWait;
    private TextViewUndoRedo helper;
    private boolean isOldNote = false;


    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                 if(result.getData()!=null){
                 image = result.getData().getData();
                 Glide.with(getApplicationContext())
                         .load(image)
                         .skipMemoryCache(true)
                         .into(imageNote);
                 imageNote.setVisibility(View.VISIBLE);
                 if (!isOldNote) {
                     fullDateTime.setVisibility(View.INVISIBLE);
                 }
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_adding_firebase);
        imageView_back = findViewById(R.id.imageView_back);
        imageView_save = findViewById(R.id.imageView_save);
        imageView_undo = findViewById(R.id.imageView_undo);
        imageView_redo = findViewById(R.id.imageView_redo);
        editText_title = findViewById(R.id.editText_title);
        editText_category = findViewById(R.id.editText_category);
        editText_notes = findViewById(R.id.editText_notes);
        fullDateTime = findViewById(R.id.fullDateTime);
        textForWait = findViewById(R.id.textForWait);
        progressBar = findViewById(R.id.progressBar_save);


        addImage = findViewById(R.id.addImage);
        deleteImage = findViewById(R.id.deleteImage);
        taskReminder = findViewById(R.id.taskReminder);
        imageNote = findViewById(R.id.imageNote);

        FirebaseApp.initializeApp(NotesAddingActivityFirebase.this);
        storageReference = FirebaseStorage.getInstance().getReference();

        try {
            Intent intent = getIntent();
            title = intent.getStringExtra("title");
            category = intent.getStringExtra("category");
            note = intent.getStringExtra("note");
            stringDate = intent.getStringExtra("date");
            docId = getIntent().getStringExtra("docId");

            if (docId != null && !docId.isEmpty()) {
                isOldNote = true;
                DocumentReference documentReference = Document.getCollectionReferenceForNotes().document(docId);
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            NotesFirebase note = documentSnapshot.toObject(NotesFirebase.class);
                            if (note != null && note.getImageUrl() != null) {
                                Glide.with(NotesAddingActivityFirebase.this)
                                        .load(note.getImageUrl())
                                        .skipMemoryCache(true)
                                        .into(imageNote);
                                imageNote.setVisibility(View.VISIBLE);
                            }
                        }

                    }
                });
            }

            editText_title.setText(title);
            editText_category.setText(category);
            editText_notes.setText(note);

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(stringDate);
            fullDateTime.setText(outputFormat.format(date));
            fullDateTime.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        imageView_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editText_title.getText().toString();
                String category = editText_category.getText().toString();
                String descriptions = editText_notes.getText().toString();

                if (descriptions.trim().isEmpty() && title.trim().isEmpty() && category.trim().isEmpty() && imageNote.getDrawable()==null) {
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
                updateUIonSavingNote();
            }
        });
        imageView_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });
        imageNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                deleteImage.setVisibility(View.VISIBLE);
                imageNote.setAlpha(0.5f);
                return true;
            }
        });

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                image = null;
                imageNote.setVisibility(View.GONE);
                deleteImage.setVisibility(View.GONE);

                if (isOldNote) {
                    DocumentReference documentReference = Document.getCollectionReferenceForNotes().document(docId);
                    documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String imageUrl = documentSnapshot.getString("imageUrl");
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                    imageRef.delete();
                                    documentReference.update("imageUrl", null);
                                    fullDateTime.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });
                } else {
                    fullDateTime.setVisibility(View.GONE);
                }
            }
        });


    }


    private void saveNoteToFirebase(NotesFirebase notesFirebase) {
        UiUtils.changeInProgress(progressBar, imageView_save, true);

        if (image != null) {
            StorageReference newImageReference = storageReference.child("images/" + UUID.randomUUID());
            newImageReference.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    newImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String newImageUrl = uri.toString();
                            notesFirebase.setImageUrl(newImageUrl);

                            if (isOldNote) {
                                DocumentReference oldDocumentReference = Document.getCollectionReferenceForNotes().document(docId);
                                oldDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            String oldImageUrl = documentSnapshot.getString("imageUrl");
                                            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                                                StorageReference oldImageReference = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl);
                                                oldImageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        save(notesFirebase);
                                                    }
                                                });
                                            } else {
                                                save(notesFirebase);
                                            }
                                        } else {
                                            save(notesFirebase);
                                        }
                                    }
                                });
                            } else {
                                save(notesFirebase);
                            }
                        }
                    });
                }
            });
        } else {
            save(notesFirebase);
        }
    }


    private void save(NotesFirebase notesFirebase) {
        DocumentReference documentReference;
        if (isOldNote) {
            documentReference = Document.getCollectionReferenceForNotes().document(docId);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot oldNoteSnapshot = task.getResult();
                        if (oldNoteSnapshot.exists()) {
                            boolean wasPinned = oldNoteSnapshot.getBoolean("pinned");

                            String imageUrl = notesFirebase.getImageUrl();
                            if (imageUrl == null) {
                                imageUrl = oldNoteSnapshot.getString("imageUrl");
                                notesFirebase.setImageUrl(imageUrl);
                            }

                            notesFirebase.setPinned(wasPinned);
                            documentReference.set(notesFirebase).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    String message = task.isSuccessful() ? "Refreshed!" : "Error!";
                                    Toast.makeText(NotesAddingActivityFirebase.this, message, Toast.LENGTH_SHORT).show();

                                    if (task.isSuccessful()) {
                                        finish();
                                        UiUtils.changeInProgress(progressBar, imageView_save, false);
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
                    String message = task.isSuccessful() ? "Added!" : "Error!";
                    Toast.makeText(NotesAddingActivityFirebase.this, message, Toast.LENGTH_SHORT).show();
                    if (task.isSuccessful()) {
                        finish();
                        UiUtils.changeInProgress(progressBar, imageView_save, false);
                    }
                }
            });
        }
    }

    private void updateUIonSavingNote(){
        imageView_back.setEnabled(false);
        imageView_undo.setEnabled(false);
        imageView_redo.setEnabled(false);
        addImage.setEnabled(false);
        deleteImage.setEnabled(false);
        imageNote.setEnabled(false);
        editText_notes.setEnabled(false);
        editText_title.setEnabled(false);
        editText_category.setEnabled(false);
        textForWait.setVisibility(View.VISIBLE);
        imageView_back.setVisibility(View.GONE);
        imageView_undo.setVisibility(View.GONE);
        imageView_redo.setVisibility(View.GONE);
        addImage.setVisibility(View.GONE);
        deleteImage.setVisibility(View.GONE);
    }


    @Override
    public void onBackPressed() {
        if(deleteImage.getVisibility()== View.VISIBLE){
            deleteImage.setVisibility(View.GONE);
            imageNote.setAlpha(1f);
        }
        else{
            super.onBackPressed();
            finish();
            overridePendingTransition(R.anim.slide_in_reverse, R.anim.slide_out_reverse);
        }
    }


}



