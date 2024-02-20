package com.example.notesproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesproject.Adapters.NotesListAdapter;
import com.example.notesproject.Database.RoomDB;
import com.example.notesproject.Models.Notes;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private LinearLayout buttonLayout, navLayout;
    private NotesListAdapter notesListAdapter;
    private List<Notes> notes = new ArrayList<>();
    private RoomDB database;
    private FloatingActionButton floatingActionButton;
    private ImageButton deleteButton,shareButton,chooseSelection,cancelSelection, switchGridMode, pinButton;
    private SearchView searchView_home;
    private ImageView notesEmptyImage;
    private TextView notesEmptyText;
    //private TextView notesEmptyText, selectedItemsText;
    private boolean isGridMode = true;
    private boolean hasUpdateUICalled = false;

    public static TextView selectedItemsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_home);
        buttonLayout = findViewById(R.id.buttonLayout);
        navLayout = findViewById(R.id.navLayout);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        searchView_home = findViewById(R.id.searchView_home);
        switchGridMode = findViewById(R.id.switchGridMode);

        deleteButton = findViewById(R.id.deleteButton);
        shareButton = findViewById(R.id.shareButton);
        pinButton = findViewById(R.id.pinButton);


        chooseSelection = findViewById(R.id.chooseSelection);
        cancelSelection = findViewById(R.id.cancelSelection);
        selectedItemsText = findViewById(R.id.selectedItemsText);

        notesEmptyImage = findViewById(R.id.notesEmptyImage);
        notesEmptyText = findViewById(R.id.notesEmptyText);


        database = RoomDB.getInstance(this);
        notes = database.mainDAO().getAllSorted();

        updateRecycler(notes);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NotesAddingActivity.class);
                startActivityForResult(intent,101);
                searchView_home.onActionViewCollapsed();
                updateEmptyStateVisibility();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                notesListAdapter.setSearchQuery(newText);
                filter(newText);
                return true;
            }
        });
        searchView_home.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                updateRecycler();
                return false;
            }
        });
        chooseSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListAdapter.selectItems();
            }
        });
        cancelSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListAdapter.setLongClickMode(false);
                hasUpdateUICalled = false;
                updateUIVisibility(notesListAdapter.getLongClickMode());
                notesListAdapter.clearSelections();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete data");
                if(notesListAdapter.getSelectedItems().size()<=1){
                      builder.setMessage("Are you sure that you want to delete selected note?");
                }
                else{
                    builder.setMessage("Are you sure that you want to delete selected notes?");
                }
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        notesListAdapter.deleteSelectedItems();
                        notesListAdapter.setLongClickMode(false);
                        hasUpdateUICalled = false;
                        updateRecycler();
                        updateViewElementsAfterDelete();
                        Toast.makeText(MainActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                        buttonLayout.setVisibility(View.INVISIBLE);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.create().show();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder shareText = new StringBuilder();

                List<Notes> selectedNotes = new ArrayList<>();
                for (int position: notesListAdapter.getSelectedItems()) {
                    selectedNotes.add(notes.get(position));
                }
                if (selectedNotes.size() > 5) {
                    Toast.makeText(MainActivity.this, "You can share up to 5 notes at a time", Toast.LENGTH_SHORT).show();
                    return;
                }
                int count = 0;
                for (Notes note: selectedNotes) {
                    if(selectedNotes.size()>1){
                    count++;
                    shareText.append("\n").append(count).append(")\nTitle: ").append(note.getTitle()).append("\n").append("Category: ").append(note.getCategory()).append("\n").append("Note: \n").append(note.getNotes()).append("\n\n\n");
                    }
                    else{
                        shareText.append("Title: ").append(note.getTitle()).append("\n").append("Category: ").append(note.getCategory()).append("\n").append("Note: \n").append(note.getNotes());
                    }
                }
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
                intent.setType("text/plain");
                if(intent.resolveActivity(getPackageManager())!=null){
                    startActivity(intent);
                }
            }
        });
        pinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int position: notesListAdapter.getSelectedItems()) {
                    if(notes.get(position).isPinned()){
                        database.mainDAO().pin(notes.get(position).getID(),false);
                    }
                    else{
                        database.mainDAO().pin(notes.get(position).getID(),true);
                    }
                }

                notesListAdapter.setLongClickMode(false);
                hasUpdateUICalled = false;
                updateUIVisibility(notesListAdapter.getLongClickMode());
                notesListAdapter.clearSelections();
                notes.clear();
                notes.addAll(database.mainDAO().getAllSorted());
                notesListAdapter.notifyDataSetChanged();
            }
        });

        switchGridMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGridMode) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                } else {
                    recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
                }
                isGridMode = !isGridMode;
                updateRecycler();
            }
        });

    }

    private void filter(String s) {
        if(!s.trim().isEmpty()){
            List<Notes> filteredList = new ArrayList<>();
            for (Notes singleNote:
                    notes) {
                if(singleNote.getTitle().toLowerCase().contains(s.toLowerCase())
                        || singleNote.getNotes().toLowerCase().contains(s.toLowerCase())
                        || singleNote.getCategory().toLowerCase().contains(s.toLowerCase())){
                    filteredList.add(singleNote);
                }
                notesListAdapter.filterList(filteredList);
                if (filteredList.isEmpty()) {
                    notesEmptyImage.setVisibility(View.VISIBLE);
                    notesEmptyText.setVisibility(View.VISIBLE);
                } else {
                    notesEmptyImage.setVisibility(View.GONE);
                    notesEmptyText.setVisibility(View.GONE);
                }
            }
        }
        else{
            notesListAdapter.filterList(notes);
            notesEmptyImage.setVisibility(View.GONE);
            notesEmptyText.setVisibility(View.GONE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==101){
            if(resultCode==Activity.RESULT_OK){
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                database.mainDAO().insert(new_notes);
                updateRecycler();
                Toast.makeText(MainActivity.this,"Added!",Toast.LENGTH_SHORT).show();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        }
        else if(requestCode==102){
            if(resultCode==Activity.RESULT_OK){
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                database.mainDAO().update(new_notes.getID(), new_notes.getTitle(), new_notes.getCategory(),new_notes.getNotes(),new_notes.getDate(), new_notes.getImagePath());
                updateRecycler();
                Toast.makeText(MainActivity.this,"Refreshed!",Toast.LENGTH_SHORT).show();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        }
    }
    private void updateRecycler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, notes,notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
        updateEmptyStateVisibility();
    }
    private void updateRecycler(){
        notes.clear();
        notes.addAll(database.mainDAO().getAllSorted());
        notesListAdapter.clearSelections();
        notesListAdapter.notifyDataSetChanged();
        updateEmptyStateVisibility();
    }

    public void updateViewElementsAfterDelete(){
        floatingActionButton.setVisibility(View.VISIBLE);
        searchView_home.setVisibility(View.VISIBLE);
        navLayout.setVisibility(View.GONE);
        floatingActionButton.setEnabled(true);
        if(notes.isEmpty()){
            searchView_home.setVisibility(View.GONE);
            searchView_home.setEnabled(false);
        }
        else{
            searchView_home.setVisibility(View.VISIBLE);
            searchView_home.setEnabled(true);
        }
        if (Objects.equals(getString(R.string.mode), "Day")) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
        }
        else if(Objects.equals(getString(R.string.mode), "Night")){
            getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
        }
    }
    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this, NotesAddingActivity.class);
            intent.putExtra("old_note", notes);
            startActivityForResult(intent, 102);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }
        @Override
        public void onLongClick(Notes notes, MaterialCardView cardView) {
            if (!hasUpdateUICalled) {
                updateUIVisibility(notesListAdapter.getLongClickMode());
                hasUpdateUICalled = true;
            }
        }
    };

    private void updateEmptyStateVisibility() {
        if (notes.isEmpty()) {
            notesEmptyImage.setVisibility(View.VISIBLE);
            notesEmptyText.setVisibility(View.VISIBLE);
            searchView_home.setVisibility(View.GONE);
            searchView_home.setEnabled(false);
        } else {
            notesEmptyImage.setVisibility(View.GONE);
            notesEmptyText.setVisibility(View.GONE);
           searchView_home.setVisibility(View.VISIBLE);
            searchView_home.setEnabled(true);
        }
    }
    private void updateUIVisibility(boolean isLongClickMode){
        if(isLongClickMode){
            fadeLongClickAnim();
            floatingActionButton.setVisibility(View.GONE);
            searchView_home.setVisibility(View.GONE);
            buttonLayout.setVisibility(View.VISIBLE);
            navLayout.setVisibility(View.VISIBLE);
            searchView_home.setEnabled(false);
            floatingActionButton.setEnabled(false);
        }
        else{
            fadeShortClickAnim();
            floatingActionButton.setVisibility(View.VISIBLE);
            searchView_home.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.GONE);
            navLayout.setVisibility(View.GONE);
            searchView_home.setEnabled(true);
            floatingActionButton.setEnabled(true);
        }
    }
    private void fadeLongClickAnim(){
        Animation fadeInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        Animation fadeInTop = AnimationUtils.loadAnimation(this, R.anim.slide_in_top_center);
        Animation fadeOutButton = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
        Animation fadeOutSearchView = AnimationUtils.loadAnimation(this, R.anim.slide_out_top_center);
        buttonLayout.startAnimation(fadeInBottom);
        navLayout.startAnimation(fadeInTop);
        floatingActionButton.startAnimation(fadeOutButton);
        searchView_home.startAnimation(fadeOutSearchView);

        if (Objects.equals(getString(R.string.mode), "Day")) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.main_gray));
        }
        else if(Objects.equals(getString(R.string.mode), "Night")){
           getWindow().setNavigationBarColor(getResources().getColor(R.color.gray));
        }
    }
    private void fadeShortClickAnim(){
        Animation fadeOutBottom = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
        Animation fadeOutTop = AnimationUtils.loadAnimation(this, R.anim.slide_out_top_center);
        Animation fadeInButton = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        Animation fadeInSearchView = AnimationUtils.loadAnimation(this, R.anim.slide_in_top_center);
        buttonLayout.startAnimation(fadeOutBottom);
        navLayout.startAnimation(fadeOutTop);
        floatingActionButton.startAnimation(fadeInButton);
        searchView_home.startAnimation(fadeInSearchView);

        if (Objects.equals(getString(R.string.mode), "Day")) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
        }
        else if(Objects.equals(getString(R.string.mode), "Night")){
            getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView_home.isIconified() || notesListAdapter.getLongClickMode()) {
            if (!searchView_home.isIconified()) {
                searchView_home.setIconified(true);
            }
            if (notesListAdapter.getLongClickMode()) {
                notesListAdapter.setLongClickMode(false);
                hasUpdateUICalled = false;
                updateUIVisibility(notesListAdapter.getLongClickMode());
                notesListAdapter.clearSelections();
            }
        } else {
            super.onBackPressed();
        }
    }

}
