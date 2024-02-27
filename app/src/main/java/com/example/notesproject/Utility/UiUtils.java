package com.example.notesproject.Utility;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;


public class UiUtils {
    public static void changeInProgress(ProgressBar progressBar, Button button, boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }
    }
    public static void changeInProgress(ProgressBar progressBar, ImageView button, boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        }
    }
    public static void changeInProgress(ProgressBar progressBar, boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

}
