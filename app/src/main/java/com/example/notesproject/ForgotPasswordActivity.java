package com.example.notesproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesproject.Utility.UiUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity implements Validable{

    private TextView goBackToLogin;
    private Button passwordRecoverButton;
    private ProgressBar progressBar;
    private EditText emailToRecoverPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        goBackToLogin = findViewById(R.id.goBackToLogin);
        passwordRecoverButton=findViewById(R.id.passwordRecoverButton);
        emailToRecoverPassword = findViewById(R.id.emailToRecoverPassword);
        progressBar = findViewById(R.id.progress_bar);

        goBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        passwordRecoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recoverPassword();
            }
        });


    }
    private void recoverPassword(){
        String email  = emailToRecoverPassword.getText().toString();

        boolean isValidated = validateData(email);
        if(!isValidated){
            return;
        }
        recoverPasswordInFirebase(email);
    }

    void recoverPasswordInFirebase(String email){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        UiUtils.changeInProgress(progressBar,passwordRecoverButton,true);
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                UiUtils.changeInProgress(progressBar,passwordRecoverButton,false);
                if (task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),"Recovering message has sent to your email",Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(ForgotPasswordActivity.this,LoginActivity.class));
                }
                else
                {
                    Toast.makeText(ForgotPasswordActivity.this,task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public boolean validateData(String... data) {
        String email = data[0];
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailToRecoverPassword.setError("Email is invalid");
            return false;
        }
        return true;
    }
}