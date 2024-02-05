package com.example.notesproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesproject.Adapters.NotesListAdapterFirebase;
import com.example.notesproject.Models.NotesFirebase;
import com.example.notesproject.Utility.Document;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.Objects;


public class MainActivityFirebase extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout buttonLayout, navLayout;
    private NotesListAdapterFirebase notesListAdapter;
    private FloatingActionButton floatingActionButton;
    private ImageButton deleteButton,shareButton,chooseSelection,cancelSelection, switchGridMode, pinButton, logOut;
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
        setContentView(R.layout.activity_main_firebase);
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
        logOut = findViewById(R.id.logOut);

        setupRecyclerView();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivityFirebase.this, NotesAddingActivityFirebase.class));
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
                return true;
            }
        });


        searchView_home.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                notesListAdapter.clearSelections();
                notesListAdapter.notifyDataSetChanged();
                updateEmptyStateVisibility();
                return false;
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivityFirebase.this,LoginActivity.class));
                finish();
            }
        });
        switchGridMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGridMode) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivityFirebase.this));
                } else {
                    recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
                }
                isGridMode = !isGridMode;
                notesListAdapter.clearSelections();
                notesListAdapter.notifyDataSetChanged();
                updateEmptyStateVisibility();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityFirebase.this);
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
                        updateViewElementsAfterDelete();
                        Toast.makeText(MainActivityFirebase.this, "Deleted!", Toast.LENGTH_SHORT).show();
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

        pinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int position : notesListAdapter.getSelectedItems()) {
                    NotesFirebase selectedNote = notesListAdapter.getItem(position);

                    if (selectedNote != null) {
                        String docId = notesListAdapter.getSnapshots().getSnapshot(position).getId();

                        DocumentReference documentReference = Document.getCollectionReferenceForNotes().document(docId);
                        documentReference.update("pinned", !selectedNote.isPinned());
                    }
                }

                notesListAdapter.setLongClickMode(false);
                hasUpdateUICalled = false;
                updateUIVisibility(notesListAdapter.getLongClickMode());
                notesListAdapter.clearSelections();
            }
        });



    }
    private final NotesClickListenerFirebase notesClickListener = new NotesClickListenerFirebase() {
        @Override
        public void onClick(NotesFirebase notes, String docId) {
            Intent intent = new Intent(MainActivityFirebase.this, NotesAddingActivityFirebase.class);
            intent.putExtra("title", notes.getTitle());
            intent.putExtra("category", notes.getCategory());
            intent.putExtra("note", notes.getNotes());
            intent.putExtra("date", notes.getDate());
            intent.putExtra("docId", docId);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }

        @Override
        public void onLongClick(NotesFirebase notes, MaterialCardView cardView) {
            if (!hasUpdateUICalled) {
                updateUIVisibility(notesListAdapter.getLongClickMode());
                hasUpdateUICalled = true;
            }
        }
    };


    void setupRecyclerView(){
        Query query = Document.getCollectionReferenceForNotes().orderBy("date", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<NotesFirebase> options = new FirestoreRecyclerOptions.Builder<NotesFirebase>()
                .setQuery(query,NotesFirebase.class).build();
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapterFirebase(options,this, notesClickListener);
        notesListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                updateEmptyStateVisibility();
            }
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                updateEmptyStateVisibility();
            }

        });
       recyclerView.setAdapter(notesListAdapter);
        updateEmptyStateVisibility();
    }


    @Override
    protected void onStart() {
        super.onStart();
        notesListAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        notesListAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notesListAdapter.notifyDataSetChanged();
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

    public void updateViewElementsAfterDelete() {
        floatingActionButton.setVisibility(View.VISIBLE);
        searchView_home.setVisibility(View.VISIBLE);
        navLayout.setVisibility(View.GONE);
        floatingActionButton.setEnabled(true);

        if (notesListAdapter.getItemCount() == 0) {
            searchView_home.setVisibility(View.GONE);
            searchView_home.setEnabled(false);
        } else {
            searchView_home.setVisibility(View.VISIBLE);
            searchView_home.setEnabled(true);
        }

        if (Objects.equals(getString(R.string.mode), "Day")) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
        } else if (Objects.equals(getString(R.string.mode), "Night")) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
        }
    }

    private void updateEmptyStateVisibility() {
        if (notesListAdapter.getItemCount()==0) {
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

    //TODO SEND TO UTILS
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
    //TODO SEND TO UTILS
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

