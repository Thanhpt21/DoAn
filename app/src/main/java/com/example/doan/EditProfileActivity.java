package com.example.doan;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText et_namePf, et_emailPf, et_phonePf;
    Button btn_Save, btn_BackEdit;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getView();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore =FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();


        Intent data = getIntent();
        String name = data.getStringExtra("name");
        String email = data.getStringExtra("email");
        String phone = data.getStringExtra("phone");

        et_namePf.setText(name);
        et_emailPf.setText(email);
        et_phonePf.setText(phone);


        //Log.d(TAG, "onCreate" + name + " " + email + " " + phone);

        btn_BackEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    public void getView(){
        et_namePf = findViewById(R.id.et_namePf);
        et_emailPf = findViewById(R.id.et_emailPf);
        et_phonePf = findViewById(R.id.et_phonePf);
        btn_Save = findViewById(R.id.btn_Save);
        btn_BackEdit = findViewById(R.id.btn_BackEdit);
    }

    public void save(){
        if(et_namePf.getText().toString().isEmpty() || et_emailPf.getText().toString().isEmpty() || et_phonePf.getText().toString().isEmpty()){
            Toast.makeText(EditProfileActivity.this, "Have field are empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = et_emailPf.getText().toString();
        String name = et_namePf.getText().toString();
        String phone = et_phonePf.getText().toString();
        user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
                Map<String, Object> edit = new HashMap<>();
                edit.put("email", email);
                edit.put("name", name);
                edit.put("phone", phone);
                documentReference.update(edit).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(EditProfileActivity.this,"Profile updated", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), PersonalActivity.class));
                        finish();
                    }
                });

                Toast.makeText(EditProfileActivity.this,"Email is changed", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public  void back(){
        Intent intent = new Intent(EditProfileActivity.this, PersonalActivity.class);
        startActivity(intent);
        finish();
    }
}