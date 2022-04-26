package com.example.doan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginIntro extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    Button btn_GetStarted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_intro);
        firebaseAuth = FirebaseAuth.getInstance();
        btn_GetStarted = findViewById(R.id.btn_GetStarted);
        if(firebaseAuth.getCurrentUser() != null){
            Toast.makeText(LoginIntro.this, "User is already logged in", Toast.LENGTH_SHORT).show();
            redirect("MAIN");
        }
        btn_GetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirect("LOGIN");
            }
        });

    }

    private void redirect(String name){
        if(name == "LOGIN"){
            Intent intent = new Intent(LoginIntro.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        if(name == "MAIN"){
            Intent intent = new Intent(LoginIntro.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}