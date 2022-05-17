package com.example.doan;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;

import android.os.Build;
import android.os.Bundle;


import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.CompoundButton;

import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ekn.gruzer.gaugelibrary.ArcGauge;
import com.ekn.gruzer.gaugelibrary.Range;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    TextView tv_Verify;
    Button btn_Verify;
    ArcGauge arcGauge1, arcGauge2;
    SwitchCompat switchCompat;
    BottomNavigationView bottom_nav;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getView();

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance("https://doan-4abdf-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Device");

        bottom_nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_home:
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.action_auto:
                        Intent intent1 = new Intent(MainActivity.this, AutoActivity.class);
                        startActivity(intent1);
                        finish();
                        break;
                    case R.id.action_schedule:
                        Intent intent2 = new Intent(MainActivity.this, ScheduleActivity.class);
                        startActivity(intent2);
                        finish();
                        break;
                    case R.id.action_personal:
                        Intent intent3 = new Intent(MainActivity.this, PersonalActivity.class);
                        startActivity(intent3);
                        finish();
                        break;
                    case R.id.action_logout:
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                        break;
                }
                return true;
            }
        });

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    databaseReference.child("pump").setValue(b);
                }else {
                    databaseReference.child("pump").setValue(b);
                }
            }
        });
        temp();
        humid();
        getData();


    }

    public void temp(){
        Range range = new Range();
        range.setColor(Color.parseColor("#E3E500"));
        range.setFrom(0.0);
        range.setTo(100.0);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String temp = snapshot.child("temp").getValue().toString();
                Log.w(TAG, String.valueOf(snapshot));
                arcGauge1.addRange(range);
                arcGauge1.setMinValue(0.0);
                arcGauge1.setMaxValue(100.0);
                arcGauge1.setValue(Double.parseDouble(temp));
                arcGauge1.setUseRangeBGColor(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
    public void humid(){
        Range range = new Range();
        range.setColor(Color.parseColor("#9BED3B"));
        range.setFrom(0.0);
        range.setTo(100.0);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String humi = snapshot.child("humi").getValue().toString();

                Log.w(TAG, String.valueOf(snapshot));
                arcGauge2.addRange(range);
                arcGauge2.setMinValue(0.0);
                arcGauge2.setMaxValue(100.0);
                arcGauge2.setValue(Double.parseDouble(humi));
                arcGauge2.setUseRangeBGColor(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    public void getData(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean auto = (Boolean) snapshot.child("auto").getValue();
                Boolean time = (Boolean) snapshot.child("time").getValue();
                Boolean pump =(Boolean) snapshot.child("pump").getValue();

                if(auto == true) {
                    switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            switchCompat.setChecked(false);
                        }
                    });
                }


                if(pump == true){
                    switchCompat.setChecked(true);
                }else {
                    switchCompat.setChecked(false);
                }
                Log.w(TAG, String.valueOf(snapshot));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getView(){
        btn_Verify = findViewById(R.id.btn_Verify);
        tv_Verify = findViewById(R.id.tv_Verify);
        arcGauge1 = findViewById(R.id.arcGauge1);
        arcGauge2 = findViewById(R.id.arcGauge2);
        switchCompat = findViewById(R.id.switchCompat);
        bottom_nav = findViewById(R.id.bottom_nav);
    }


}