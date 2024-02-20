package com.example.notesproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE).getInt("themeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if(currentUser==null){
                    startActivity(new Intent(SplashScreenActivity.this,LoginActivity.class));
                }else{
                    startActivity(new Intent(SplashScreenActivity.this,MainActivityFirebase.class));
                }
                finish();
            }
        },1000);
    }
}