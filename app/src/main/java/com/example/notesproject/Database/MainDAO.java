package com.example.notesproject.Database;

import static androidx.room.OnConflictStrategy.REPLACE;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import com.example.notesproject.Models.Notes;

import java.util.List;


@Dao
public interface MainDAO {
    @Insert(onConflict = REPLACE)
    void insert(Notes notes);

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Notes> getAll();

    @Query("UPDATE notes SET title = :title, category =:category, notes = :notes, date=:date WHERE ID = :id")
    void update(int id, String title, String category,String notes, String date);

    @Delete
    void delete(Notes notes);

}