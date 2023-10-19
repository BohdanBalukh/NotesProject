package com.example.notesproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesproject.Adapters.NotesListAdapter;
import com.example.notesproject.Database.RoomDB;
import com.example.notesproject.Models.Notes;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private NotesListAdapter notesListAdapter;
    private List<Notes> notes = new ArrayList<>();
    private RoomDB database;
    public static FloatingActionButton floatingActionButton;
    public static SearchView searchView_home;
    public static ImageView notesEmptyImage, chooseSelection, cancelSelection;
    public static TextView notesEmptyText, selectedItemsText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_home);

        floatingActionButton = findViewById(R.id.floatingActionButton);
        searchView_home = findViewById(R.id.searchView_home);

        chooseSelection = findViewById(R.id.chooseSelection);
        cancelSelection = findViewById(R.id.cancelSelection);
        selectedItemsText = findViewById(R.id.selectedItemsText);

        notesEmptyImage = findViewById(R.id.notesEmptyImage);
        notesEmptyText = findViewById(R.id.notesEmptyText);

        database = RoomDB.getInstance(this);
        notes = database.mainDAO().getAll();

        updateRecycler(notes);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NotesAddingActivity.class);
                startActivityForResult(intent,101);
                searchView_home.onActionViewCollapsed();
            }
        });

        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
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
                //updateUIVisibility(notesListAdapter.getLongClickMode());
            }
        });
        cancelSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListAdapter.setLongClickMode(false);
                //updateUIVisibility(notesListAdapter.getLongClickMode());
                notesListAdapter.clearSelections();
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
            }
        }
        else{
            notesListAdapter.filterList(notes);
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
            }
        }
        else if(requestCode==102){
            if(resultCode==Activity.RESULT_OK){
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                database.mainDAO().update(new_notes.getID(), new_notes.getTitle(), new_notes.getCategory(),new_notes.getNotes(),new_notes.getDate());
                updateRecycler();
                Toast.makeText(MainActivity.this,"Refreshed!",Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void updateRecycler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, notes,notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }
    private void updateRecycler(){
        notes.clear();
        notes.addAll(database.mainDAO().getAll());
        notesListAdapter.clearSelections();
        notesListAdapter.notifyDataSetChanged();
    }
    public void updateViewElementsAfterDelete(){
        floatingActionButton.setVisibility(View.VISIBLE);
        searchView_home.setVisibility(View.VISIBLE);
        chooseSelection.setVisibility(View.GONE);
        cancelSelection.setVisibility(View.GONE);
        selectedItemsText.setVisibility(View.GONE);
        searchView_home.setEnabled(true);
        floatingActionButton.setEnabled(true);
    }
    private void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);
        LinearLayout deleteLayout = dialog.findViewById(R.id.layoutDelete);
        LinearLayout shareLayout = dialog.findViewById(R.id.layoutShare);
        deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete data");
                builder.setMessage("Are you sure that you want to delete selected note/notes?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        notesListAdapter.deleteSelectedItems();
                        notesListAdapter.setLongClickMode(false);
                        updateRecycler();
                        updateViewElementsAfterDelete();
                        Toast.makeText(MainActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
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

        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareText = "";
                List<Notes> selectedNotes = new ArrayList<>();
                for (int position: notesListAdapter.getSelectedItems()) {
                    selectedNotes.add(notes.get(position));
                }
                int count = 0;
                for (Notes note:
                        selectedNotes) {
                    count++;
                    shareText+="\n"+count+")\nTitle: "+note.getTitle()+"\n" +
                            "Category: "+note.getCategory()+"\n" +
                            "Note: \n"+note.getNotes()+"\n\n\n";
                }
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,shareText);
                intent.setType("text/plain");
                if(intent.resolveActivity(getPackageManager())!=null){
                    startActivity(intent);
                }
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {

        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this, NotesAddingActivity.class);
            intent.putExtra("old_note", notes);
            startActivityForResult(intent, 102);
        }
        @Override
        public void onLongClick(Notes notes, MaterialCardView cardView) {
            //updateUIVisibility(notesListAdapter.getLongClickMode());
            showDialog();
        }
    };

    /*
    private void updateUIVisibility(boolean isLongClickMode){

        if(isLongClickMode){
            if(!notesListAdapter.getSelectedItems().isEmpty()){
                selectedItemsText.setText(String.valueOf(notesListAdapter.getSelectedItems().size())+" item selected");
            }
            else{
                selectedItemsText.setText("Select items");
            }
            floatingActionButton.setVisibility(View.GONE);
            searchView_home.setVisibility(View.GONE);
            chooseSelection.setVisibility(View.VISIBLE);
            cancelSelection.setVisibility(View.VISIBLE);
            selectedItemsText.setVisibility(View.VISIBLE);
            searchView_home.setEnabled(false);
            floatingActionButton.setEnabled(false);
        }
        else{
            floatingActionButton.setVisibility(View.VISIBLE);
            searchView_home.setVisibility(View.VISIBLE);
            chooseSelection.setVisibility(View.GONE);
            cancelSelection.setVisibility(View.GONE);
            selectedItemsText.setVisibility(View.GONE);
            searchView_home.setEnabled(true);
            floatingActionButton.setEnabled(true);
        }
    }*/

}
