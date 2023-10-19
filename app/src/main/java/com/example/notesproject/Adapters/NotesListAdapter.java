package com.example.notesproject.Adapters;



import static com.example.notesproject.MainActivity.cancelSelection;
import static com.example.notesproject.MainActivity.chooseSelection;
import static com.example.notesproject.MainActivity.floatingActionButton;
import static com.example.notesproject.MainActivity.searchView_home;
import static com.example.notesproject.MainActivity.selectedItemsText;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesproject.Database.RoomDB;
import com.example.notesproject.MainActivity;
import com.example.notesproject.Models.Notes;
import com.example.notesproject.NotesClickListener;
import com.example.notesproject.R;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NotesListAdapter extends RecyclerView.Adapter<NotesViewHolder>{

    private Context context;
    private List<Notes> list;
    private List<Integer> selectedItems=new ArrayList<>();
    private NotesClickListener listener;
    private boolean isLongClickMode = false;

    public NotesListAdapter(Context context, List<Notes> list, NotesClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotesViewHolder(LayoutInflater.from(context).inflate(R.layout.notes_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.textView_title.setText(list.get(position).getTitle());
        holder.textView_title.setSelected(true);
        holder.textView_notes.setText(list.get(position).getNotes());
        holder.textView_category.setText(list.get(position).getCategory());

        setFormattedDate(holder.textView_date,list.get(position).getDate());


        if(list.get(position).isPinned()){
            holder.imageView_pin.setImageResource(R.drawable.ic_pin);
        }
        else{
            holder.imageView_pin.setImageResource(0);
        }

        boolean isSelected = selectedItems.contains(position);
        holder.notes_container.setChecked(isSelected);



        if(isLongClickMode){
            if(!selectedItems.isEmpty()){
                selectedItemsText.setText(String.valueOf(selectedItems.size())+" item selected");
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


        holder.notes_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLongClickMode) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (selectedItems.contains(adapterPosition)) {
                        selectedItems.remove(Integer.valueOf(adapterPosition));
                    } else {
                        selectedItems.add(adapterPosition);
                    }
                    notifyDataSetChanged();
                } else {
                    listener.onClick(list.get(holder.getAdapterPosition()));
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
                listener.onLongClick(list.get(adapterPosition), holder.notes_container);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public void filterList(List<Notes> filteredList){
        list=filteredList;
        notifyDataSetChanged();
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
        RoomDB database = RoomDB.getInstance(context);

        List<Notes> selectedNotes = new ArrayList<>();
        for (int position : selectedItems) {
            selectedNotes.add(list.get(position));
        }

        for (Notes note : selectedNotes) {
            database.mainDAO().delete(note);
        }

        list.removeAll(selectedNotes);
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedItems() {
        return selectedItems;
    }

    public void setLongClickMode(boolean isLongClickMode){
        this.isLongClickMode=isLongClickMode;
    }
    public boolean getLongClickMode(){return isLongClickMode;}
    private void setFormattedDate(TextView textView, String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat;
        try {
            Date date = inputFormat.parse(dateString);
            Date currentDate = new Date();
            //Date currentDate = inputFormat.parse("2023.09.17 00:00:00");
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
}
class NotesViewHolder extends RecyclerView.ViewHolder{

    MaterialCardView notes_container;
    TextView textView_title, textView_notes,textView_category,textView_date;
    ImageView imageView_pin;
    public NotesViewHolder(@NonNull View itemView) {
        super(itemView);
        notes_container = itemView.findViewById(R.id.notes_container);
        textView_title = itemView.findViewById(R.id.textView_title);
        textView_notes = itemView.findViewById(R.id.textView_notes);
        textView_category = itemView.findViewById(R.id.textView_category);
        textView_date = itemView.findViewById(R.id.textView_date);
        imageView_pin = itemView.findViewById(R.id.imageView_pin);

    }
}
