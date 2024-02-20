package com.example.notesproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivityFirebase extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LinearLayout buttonLayout, navLayout;
    private NotesListAdapterFirebase notesListAdapter;
    private FloatingActionButton floatingActionButton;
    private ImageButton deleteButton,shareButton,chooseSelection,cancelSelection, pinButton, googleAccountButton;
    private SearchView searchView_home;
    private ImageView notesEmptyImage;
    private TextView notesEmptyText, userDisplayEmail,userDisplayName;

    //private TextView notesEmptyText, selectedItemsText;
    private boolean isGridMode = true;
    private boolean isPinnedMode = true;
    private boolean hasUpdateUICalled = false;
    public static TextView selectedItemsText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_firebase);

        recyclerView = findViewById(R.id.recycler_home);
        drawerLayout = findViewById(R.id.drawer_layout);
        googleAccountButton = findViewById(R.id.googleAccountButton);
        navigationView = findViewById(R.id.navigation_view);
        buttonLayout = findViewById(R.id.buttonLayout);
        navLayout = findViewById(R.id.navLayout);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        searchView_home = findViewById(R.id.searchView_home);

        deleteButton = findViewById(R.id.deleteButton);
        shareButton = findViewById(R.id.shareButton);
        pinButton = findViewById(R.id.pinButton);


        chooseSelection = findViewById(R.id.chooseSelection);
        cancelSelection = findViewById(R.id.cancelSelection);
        selectedItemsText = findViewById(R.id.selectedItemsText);

        notesEmptyImage = findViewById(R.id.notesEmptyImage);
        notesEmptyText = findViewById(R.id.notesEmptyText);

        setupRecyclerView();

        userDisplayEmail = navigationView.getHeaderView(0).findViewById(R.id.userDisplayEmail);
        userDisplayName = navigationView.getHeaderView(0).findViewById(R.id.userDisplayName);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userDisplayEmail.setText(currentUser.getEmail());
            userDisplayName.setText("User: "+currentUser.getUid().substring(0,4)+currentUser.getUid().substring(currentUser.getUid().length()-4));
        }
        googleAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.open();
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if(itemId == R.id.nav_add){
                    startActivity(new Intent(MainActivityFirebase.this, NotesAddingActivityFirebase.class));
                    searchView_home.onActionViewCollapsed();
                    updateEmptyStateVisibility();
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    drawerLayout.close();
                }
                if(itemId == R.id.nav_pinned){
                    Query query;
                    if(isPinnedMode){
                        query = Document.getCollectionReferenceForNotes()
                                .whereEqualTo("pinned", true)
                                .orderBy("date", Query.Direction.DESCENDING);
                    FirestoreRecyclerOptions<NotesFirebase> options = new FirestoreRecyclerOptions.Builder<NotesFirebase>()
                            .setQuery(query,NotesFirebase.class).build();
                    notesListAdapter.updateOptions(options);
                    navigationView.getMenu().findItem(R.id.nav_pinned).setTitle("All notes");
                    navigationView.getMenu().findItem(R.id.nav_pinned).setIcon(R.drawable.ic_all_notes);
                    }
                    else{
                        query = Document.getCollectionReferenceForNotes()
                                .orderBy("date", Query.Direction.DESCENDING);
                        FirestoreRecyclerOptions<NotesFirebase> options = new FirestoreRecyclerOptions.Builder<NotesFirebase>()
                                .setQuery(query,NotesFirebase.class).build();
                        notesListAdapter.updateOptions(options);
                        navigationView.getMenu().findItem(R.id.nav_pinned).setTitle("Pinned notes");
                        navigationView.getMenu().findItem(R.id.nav_pinned).setIcon(R.drawable.ic_pin);
                    }
                    notesListAdapter.notifyDataSetChanged();
                    isPinnedMode = !isPinnedMode;
                    updateEmptyStateVisibility();
                    drawerLayout.close();
                }

                if(itemId == R.id.nav_themes){
                    BottomSheetDialog dialog = new BottomSheetDialog(MainActivityFirebase.this);
                    dialog.setContentView(R.layout.bottomsheetlayout);

                    MaterialCardView asSystemTheme = dialog.findViewById(R.id.asSystemTheme);
                    MaterialCardView lightTheme = dialog.findViewById(R.id.lightTheme);
                    MaterialCardView darkTheme = dialog.findViewById(R.id.darkTheme);

                    ImageView asSystemThemeImage = dialog.findViewById(R.id.asSystemThemeImage);
                    TextView asSystemThemeText = dialog.findViewById(R.id.asSystemThemeText);


                    if (((UiModeManager) getSystemService(Context.UI_MODE_SERVICE)).getNightMode() == UiModeManager.MODE_NIGHT_NO) {
                       asSystemTheme.setCardBackgroundColor(getResources().getColor(R.color.white));
                       asSystemTheme.invalidate();
                    } else{
                        asSystemTheme.setCardBackgroundColor(getResources().getColor(R.color.gray));
                        asSystemThemeImage.setImageResource(R.drawable.as_system_image_dark);
                        asSystemThemeText.setTextColor(getResources().getColor(R.color.white));
                        asSystemTheme.invalidate();
                    }

                    SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
                    setStroke(asSystemTheme, sharedPreferences.getBoolean("asSystemTheme", false));
                    setStroke(lightTheme, sharedPreferences.getBoolean("lightTheme", false));
                    setStroke(darkTheme, sharedPreferences.getBoolean("darkTheme", false));

                    dialog.show();

                    asSystemTheme.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            saveThemePreference(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            saveIndicatorState("asSystemTheme", true);
                            saveIndicatorState("lightTheme", false);
                            saveIndicatorState("darkTheme", false);
                            dialog.dismiss();

                        }
                    });
                    lightTheme.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            saveThemePreference(AppCompatDelegate.MODE_NIGHT_NO);
                            saveIndicatorState("asSystemTheme", false);
                            saveIndicatorState("lightTheme", true);
                            saveIndicatorState("darkTheme", false);
                            dialog.dismiss();

                        }
                    });
                    darkTheme.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            saveThemePreference(AppCompatDelegate.MODE_NIGHT_YES);
                            saveIndicatorState("asSystemTheme", false);
                            saveIndicatorState("lightTheme", false);
                            saveIndicatorState("darkTheme", true);
                            dialog.dismiss();

                        }
                    });

                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.BOTTOM);
                }
                if(itemId == R.id.nav_format){
                    SharedPreferences sharedPreferences = getSharedPreferences("RecyclerPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (isGridMode) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivityFirebase.this));
                        editor.putBoolean("isGridMode", false);
                        navigationView.getMenu().findItem(R.id.nav_format).setIcon(R.drawable.ic_view_grid);
                        navigationView.getMenu().findItem(R.id.nav_format).setTitle("Grid format");
                    } else {
                        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
                        editor.putBoolean("isGridMode", true);
                        navigationView.getMenu().findItem(R.id.nav_format).setIcon(R.drawable.ic_view_list);
                        navigationView.getMenu().findItem(R.id.nav_format).setTitle("List format");
                    }
                    editor.apply();

                    isGridMode = !isGridMode;
                    drawerLayout.close();
                }
                if(itemId == R.id.nav_about){

                }
                if(itemId == R.id.nav_account_settings){

                }
                if(itemId == R.id.nav_logout){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityFirebase.this);
                    builder.setTitle("Logging Out");
                    builder.setMessage("Are you sure that you want to log out?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(MainActivityFirebase.this,LoginActivity.class));
                            drawerLayout.close();
                            finish();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    builder.create().show();
                }
                return false;
            }
        });


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
                notesListAdapter.setSearchQuery(newText);
                return true;
            }
        });


        searchView_home.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                notesListAdapter.clearSelections();
                updateEmptyStateVisibility();
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
                        if (!isPinnedMode) {
                            recreate();
                        }
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

                List<NotesFirebase> selectedNotes = new ArrayList<>();
                for (int position: notesListAdapter.getSelectedItems()) {
                    selectedNotes.add(notesListAdapter.getSnapshots().get(position));
                }
                if (selectedNotes.size() > 5) {
                    Toast.makeText(MainActivityFirebase.this, "You can share up to 5 notes at a time", Toast.LENGTH_SHORT).show();
                    return;
                }
                int count = 0;
                for (NotesFirebase note: selectedNotes) {
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
                for (int position : notesListAdapter.getSelectedItems()) {
                    NotesFirebase selectedNote = notesListAdapter.getItem(position);

                    String docId = notesListAdapter.getSnapshots().getSnapshot(position).getId();

                    DocumentReference documentReference = Document.getCollectionReferenceForNotes().document(docId);
                    documentReference.update("pinned", !selectedNote.isPinned());
                }

                if (!isPinnedMode) {
                    recreate();
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
       Query query = Document.getCollectionReferenceForNotes()
               .orderBy("date", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<NotesFirebase> options = new FirestoreRecyclerOptions.Builder<NotesFirebase>()
                .setQuery(query,NotesFirebase.class).build();
        SharedPreferences sharedPreferences = getSharedPreferences("RecyclerPrefs", Context.MODE_PRIVATE);
        isGridMode = sharedPreferences.getBoolean("isGridMode", true);
        if (isGridMode) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
            navigationView.getMenu().findItem(R.id.nav_format).setIcon(R.drawable.ic_view_list);
            navigationView.getMenu().findItem(R.id.nav_format).setTitle("List format");
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivityFirebase.this));
            navigationView.getMenu().findItem(R.id.nav_format).setIcon(R.drawable.ic_view_grid);
            navigationView.getMenu().findItem(R.id.nav_format).setTitle("Grid format");
        }
        notesListAdapter = new NotesListAdapterFirebase(options,this, notesClickListener);

        notesListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (notesListAdapter.getLongClickMode()) {
                    searchView_home.setVisibility(View.GONE);
                }
                else{
                    updateEmptyStateVisibility();
                }
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

    private void saveThemePreference(int mode) {
        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("themeMode", mode);
        editor.apply();
    }
    private void saveIndicatorState(String indicatorKey, boolean isSelected) {
        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(indicatorKey, isSelected);
        editor.apply();
    }
    private void setStroke(MaterialCardView materialCardView, boolean isSelected) {
        int color = isSelected ? R.color.orange : R.color.black;
        int strokeWidth = isSelected ? 10 : 1;
        materialCardView.setStrokeColor(getResources().getColor(color));
        materialCardView.setStrokeWidth(strokeWidth);
    }

    public void updateViewElementsAfterDelete() {
        floatingActionButton.setVisibility(View.VISIBLE);
        searchView_home.setVisibility(View.VISIBLE);
        navLayout.setVisibility(View.GONE);
        floatingActionButton.setEnabled(true);
        googleAccountButton.setEnabled(true);

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
        if (notesListAdapter.getItemCount() == 0) {
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
            googleAccountButton.setEnabled(false);

        }
        else{
            fadeShortClickAnim();
            floatingActionButton.setVisibility(View.VISIBLE);
            searchView_home.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.GONE);
            navLayout.setVisibility(View.GONE);
            searchView_home.setEnabled(true);
            floatingActionButton.setEnabled(true);
            googleAccountButton.setEnabled(true);
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
        googleAccountButton.startAnimation(fadeOutButton);
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
        googleAccountButton.startAnimation(fadeInButton);
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
        if (!searchView_home.isIconified() || notesListAdapter.getLongClickMode()||drawerLayout.isOpen()) {
            if (!searchView_home.isIconified()) {
                searchView_home.setIconified(true);
            }
            if (notesListAdapter.getLongClickMode()) {
                notesListAdapter.setLongClickMode(false);
                hasUpdateUICalled = false;
                updateUIVisibility(notesListAdapter.getLongClickMode());
                notesListAdapter.clearSelections();
            }
            if(drawerLayout.isOpen()){
                drawerLayout.close();
            }
        } else {
            super.onBackPressed();
        }
    }

}

