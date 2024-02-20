package com.example.notesproject.Adapters;

import static com.example.notesproject.MainActivityFirebase.selectedItemsText;

import android.content.Context;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.notesproject.Models.NotesFirebase;
import com.example.notesproject.NotesClickListenerFirebase;
import com.example.notesproject.R;
import com.example.notesproject.Utility.Document;
import com.example.notesproject.Utility.UiUtils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesListAdapterFirebase extends FirestoreRecyclerAdapter<NotesFirebase, NotesListAdapterFirebase.NoteViewHolderFirebase> {
    private Context context;
    private boolean isLongClickMode = false;
    private List<Integer> selectedItems = new ArrayList<>();
    private NotesClickListenerFirebase listener;

    private String searchQuery = "";


    public NotesListAdapterFirebase(@NonNull FirestoreRecyclerOptions<NotesFirebase> options, Context context, NotesClickListenerFirebase listener) {
        super(options);
        this.context = context;
        this.listener = listener;
    }


    @NonNull
    @Override
    public NoteViewHolderFirebase onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolderFirebase(LayoutInflater.from(context).inflate(R.layout.notes_list, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteViewHolderFirebase holder, int position, @NonNull NotesFirebase note) {
        holder.textView_title.setText(note.getTitle());
        holder.textView_title.setSelected(true);
        holder.textView_notes.setText(note.getNotes());
        holder.textView_category.setText(note.getCategory());
        setFormattedDate(holder.textView_date,note.getDate());

        if (note.getImageUrl() != null && !note.getImageUrl().isEmpty()) {
            UiUtils.changeInProgress(holder.progressBar_loadingImage, true);

            holder.imageNote.setImageDrawable(null);
            Glide.with(context)
                    .load(note.getImageUrl())
                    .centerCrop()
                    .override(300, 300)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            UiUtils.changeInProgress(holder.progressBar_loadingImage, false);
                            holder.imageNote.setVisibility(View.VISIBLE);
                            holder.imageNote.setImageDrawable(resource);
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        } else {
            holder.imageNote.setVisibility(View.GONE);
        }

        setHighlightText(holder.textView_title, note.getTitle(), searchQuery);
        setHighlightText(holder.textView_notes, note.getNotes(), searchQuery);
        setHighlightText(holder.textView_category, note.getCategory(), searchQuery);

        if(note.isPinned()){
            holder.pinLayot.setVisibility(View.VISIBLE);
        }
        else{
            holder.pinLayot.setVisibility(View.GONE);
        }

        boolean isSelected = selectedItems.contains(position);
        holder.notes_container.setChecked(isSelected);


        if(isLongClickMode){
            if(!selectedItems.isEmpty()){
                selectedItemsText.setText(selectedItems.size() +" item selected");
            }
            else{
                selectedItemsText.setText("Select items");
            }
        }

        holder.notes_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (isLongClickMode) {
                    if (selectedItems.contains(adapterPosition)) {
                        selectedItems.remove(Integer.valueOf(adapterPosition));
                    } else {
                        selectedItems.add(adapterPosition);
                    }
                    notifyDataSetChanged();
                } else {
                    String docId = getSnapshots().getSnapshot(adapterPosition).getId();
                    listener.onClick(getItem(adapterPosition), docId);
                }
            }
        });
        holder.notes_container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int adapterPosition = holder.getAdapterPosition();

                if (!isLongClickMode) {
                    isLongClickMode = true;
                    selectedItems.clear();
                }

                if (selectedItems.contains(adapterPosition)) {
                    selectedItems.remove(Integer.valueOf(adapterPosition));
                } else {
                    selectedItems.add(adapterPosition);
                }
                notifyDataSetChanged();
                listener.onLongClick(getItem(adapterPosition), holder.notes_container);
                return true;
            }
        });

    }
    public void selectItems() {
        if (selectedItems.size() == getItemCount()) {
            clearSelections();
        } else {
            selectedItems.clear();
            for (int i = 0; i < getItemCount(); i++) {
                selectedItems.add(i);
            }
            notifyDataSetChanged();
        }
    }
    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public void deleteSelectedItems() {
        ObservableSnapshotArray<NotesFirebase> snapshots = getSnapshots();
        List<Integer> selectedItemsCopy = new ArrayList<>(selectedItems);

        for (int position : selectedItemsCopy) {
            DocumentSnapshot snapshot = snapshots.getSnapshot(position);
            String docId = snapshot.getId();
            NotesFirebase note = snapshot.toObject(NotesFirebase.class);
            if (note != null && note.getImageUrl() != null) {
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(note.getImageUrl());
                imageRef.delete();
            }
            DocumentReference documentReference = Document.getCollectionReferenceForNotes().document(docId);
            documentReference.delete();
        }

        selectedItems.clear();
        notifyDataSetChanged();
    }


    public List<Integer> getSelectedItems() {
        return selectedItems;
    }
    public void setLongClickMode(boolean isLongClickMode){
        this.isLongClickMode=isLongClickMode;
    }

    public boolean getLongClickMode(){
        return isLongClickMode;
    }

    private void setFormattedDate(TextView textView, String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat;
        try {
            Date date = inputFormat.parse(dateString);
            Date currentDate = new Date();
            //Date currentDate = inputFormat.parse("2024.02.04 00:00:00");
            if (isSameDay(date,currentDate)) {
                outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            }
            else if (isSameWeek(date,currentDate)) {
                outputFormat = new SimpleDateFormat("EEEE HH:mm", Locale.getDefault());
            } else if (isSameYear(date,currentDate)) {
                outputFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
            } else {
                outputFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
            }
            textView.setText(outputFormat.format(date));
            textView.setSelected(true);
        } catch (ParseException e) {
            Date CatchDate = new Date();
            textView.setText(inputFormat.format(CatchDate));
        }
    }
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    private boolean isSameWeek(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }
    private boolean isSameYear(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }


    public void setSearchQuery(String query) {
        searchQuery = query;
        notifyDataSetChanged();
    }
    public void setFilterQuery(String query) {
        Query filteredQuery = Document.getCollectionReferenceForNotes()
                .whereEqualTo("notes", query);
        FirestoreRecyclerOptions<NotesFirebase> filteredOptions = new FirestoreRecyclerOptions.Builder<NotesFirebase>()
                .setQuery(filteredQuery, NotesFirebase.class).build();
        updateOptions(filteredOptions);
    }


    private void setHighlightText(TextView textView, String fullText, String query) {
        int startPos = fullText.toLowerCase().indexOf(query.toLowerCase());
        int endPos = startPos + query.length();

        if (startPos != -1) {
            Spannable spannable = new SpannableString(fullText);
            ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{}}, new int[]{context.getResources().getColor(R.color.orange)});
            TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, colorStateList, null);
            spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spannable);
        } else {
            textView.setText(fullText);
        }
    }

    class NoteViewHolderFirebase extends RecyclerView.ViewHolder{

        MaterialCardView notes_container;
        ProgressBar progressBar_loadingImage;
        RoundedImageView imageNote;
        TextView textView_title, textView_notes,textView_category,textView_date;
        RelativeLayout pinLayot;

        public NoteViewHolderFirebase(@NonNull View itemView) {
            super(itemView);
            notes_container = itemView.findViewById(R.id.notes_container);
            textView_title = itemView.findViewById(R.id.textView_title);
            textView_notes = itemView.findViewById(R.id.textView_notes);
            textView_category = itemView.findViewById(R.id.textView_category);
            textView_date = itemView.findViewById(R.id.textView_date);
            progressBar_loadingImage = itemView.findViewById(R.id.progressBar_loadingImage);
            imageNote = itemView.findViewById(R.id.imageNote);
            pinLayot = itemView.findViewById(R.id.pinLayout);
        }
    }
}


