package com.example.notesproject.Models;

import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "notes")
public class Notes implements Serializable {
    @PrimaryKey(autoGenerate = true)
    int ID = 0;
    @ColumnInfo(name = "title")
    String title = "";
    @ColumnInfo(name = "notes")
    String notes = "";
    @ColumnInfo(name = "category")
    String category = "";
    @ColumnInfo(name = "date")
    String date = "";
    @ColumnInfo(name="imagePath")
    String imagePath;
    @ColumnInfo(name = "pinned")
    boolean pinned = false;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setImagePath(String imagePath) {this.imagePath = imagePath;}

    public String getImagePath() {return imagePath;}


    public boolean isPinned() {
        return pinned;
    }
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}