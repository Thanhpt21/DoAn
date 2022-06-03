package com.example.doan;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AutoActivity extends AppCompatActivity implements ExampleDialog.ExampleDialogListener {
    SwitchCompat tb_auto;
    TextView tv_valueTemp, tv_valueHumid, tv_statusAuto;
    Button btn_setValue, btn_autoBackHome;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseHandler firebaseHandler;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);
        getView();

        db = DBHelper.getInstance(Constants.PACKAGE_NAME,Constants.DATABASE_NAME);
        firebaseHandler = FirebaseHandler.getInstance();

        String h = db.getByName(Constants.HUMIDITY);
        String t = db.getByName(Constants.TEMPERATURE);
        tv_valueTemp.setText(t);
        tv_valueHumid.setText(h);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://doan-4abdf-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Device");

        btn_setValue.setOnClickListener(view->{
            openDialog();
        });

        String autoMode = db.getByName(Constants.AUTO_MODE);
        if(autoMode.equals("1"))
            tb_auto.setChecked(true);


        tb_auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    db.update(Constants.AUTO_MODE,"1");

                }else {
                    db.update(Constants.AUTO_MODE,"0");
                    db.update(Constants.AUTO_RUNNING,"0");
                    firebaseHandler.updateField("pump",false);
                }
            }

        });

        btn_autoBackHome.setOnClickListener(view -> {
            backHome();
        });
    }

    public void openDialog(){
        ExampleDialog exampleDialog = new ExampleDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");

    }

    @Override
    public void applyTexts(String temp, String humid) {
       tv_valueTemp.setText(temp);
        tv_valueHumid.setText(humid);

        db.update(Constants.HUMIDITY,humid);
        db.update(Constants.TEMPERATURE,temp);
    }

    public void getView(){
        tb_auto = findViewById(R.id.tb_auto);
        tv_valueTemp = findViewById(R.id.tv_valueTemp);
        tv_valueHumid = findViewById(R.id.tv_valueHumid);
        tv_statusAuto = findViewById(R.id.tv_statusAuto);
        btn_setValue = findViewById(R.id.btn_setValue);
        btn_autoBackHome = findViewById(R.id.btn_autoBackHome);
    }

    public void backHome(){
        Intent intent = new Intent(AutoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}