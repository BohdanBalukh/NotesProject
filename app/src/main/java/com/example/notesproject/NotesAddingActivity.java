package com.example.notesproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.notesproject.Adapters.NotesListAdapter;
import com.example.notesproject.Models.Notes;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class NotesAddingActivity extends AppCompatActivity {
    private EditText editText_title, editText_category,editText_notes;
    private ImageView imageView_save,imageView_back, imageView_undo,imageView_redo;
    private RoundedImageView imageNote;
    private TextViewUndoRedo helper;
    private ImageButton addImage, deleteImage;
    private TextView fullDateTime;
    private Notes notes;
    private String selectedImagePath="";
    private boolean isOldNote = false;
    private static final int REQUEST_CODE_STORAGE_PERMISSION=1;
    private static final int REQUEST_CODE_SELECT_IMAGE=2;

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
        fullDateTime = findViewById(R.id.fullDateTime);

        addImage = findViewById(R.id.addImage);
        deleteImage = findViewById(R.id.deleteImage);
        imageNote=findViewById(R.id.imageNote);

        notes = new Notes();

        try{
            notes = (Notes) getIntent().getSerializableExtra("old_note");
            editText_title.setText(notes.getTitle());
            editText_category.setText(notes.getCategory());
            editText_notes.setText(notes.getNotes());


            if (notes.getImagePath() != null && !notes.getImagePath().trim().isEmpty()) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(notes.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            } else {
                imageNote.setVisibility(View.GONE);
            }

            /*
            if (notes.getImagePath() != null && !notes.getImagePath().trim().isEmpty()) {
                File imageFile = new File(notes.getImagePath());
                if (!imageFile.exists()) {
                    notes.setImagePath("");
                } else {
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(notes.getImagePath()));
                    imageNote.setVisibility(View.VISIBLE);
                }
            } else {
                imageNote.setVisibility(View.GONE);
            }*/


            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat=new SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(notes.getDate());
            fullDateTime.setText(outputFormat.format(date));
            fullDateTime.setVisibility(View.VISIBLE);
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

                if(descriptions.trim().isEmpty() && title.trim().isEmpty() && category.trim().isEmpty()&&imageNote.getDrawable()==null){
                    onBackPressed();
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
                if (!selectedImagePath.isEmpty()) {
                    notes.setImagePath(selectedImagePath);
                }

                Intent intent = new Intent();
                intent.putExtra("note",notes);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

        });

        imageView_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(deleteImage.getVisibility()==View.VISIBLE){
                    deleteImage.setVisibility(View.GONE);
                }else{
                    finish();
                    overridePendingTransition(R.anim.slide_in_reverse, R.anim.slide_out_reverse);}
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
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(NotesAddingActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else{
                    selectImage();
                }
            }
        });

        imageNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                deleteImage.setVisibility(View.VISIBLE);
                return true;
            }
        });
        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imageNote.setImageBitmap(null);
                        imageNote.setVisibility(View.GONE);
                        deleteImage.setVisibility(View.GONE);
                        if (notes == null) {
                            notes = new Notes();
                        }
                        notes.setImagePath("");
                        selectedImagePath = "";

                        if (!isOldNote) {
                            fullDateTime.setVisibility(View.GONE);
                        } else {
                            fullDateTime.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }
        });

    }
    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_STORAGE_PERMISSION && grantResults.length>0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageNote.setImageBitmap(bitmap);
                    imageNote.setVisibility(View.VISIBLE);
                    if (!isOldNote) {
                        fullDateTime.setVisibility(View.INVISIBLE);
                    }
                   selectedImagePath = getPathFromUri(selectedImageUri);

                } catch (Exception exception) {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri,null,null,null,null);
        if(cursor == null){
            filePath = contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onBackPressed() {
        if(deleteImage.getVisibility()==View.VISIBLE){
            deleteImage.setVisibility(View.GONE);
        }
        else{
            super.onBackPressed();
            finish();
            overridePendingTransition(R.anim.slide_in_reverse, R.anim.slide_out_reverse);
        }
    }
}
