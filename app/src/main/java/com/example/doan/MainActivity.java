package com.example.doan;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;


import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;


import android.util.Log;
import android.view.MenuItem;


import android.widget.Button;
import android.widget.CompoundButton;


import android.widget.TextView;


import com.ekn.gruzer.gaugelibrary.ArcGauge;
import com.ekn.gruzer.gaugelibrary.Range;


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

        Intent autoServiceIntent = new Intent(this, AutoService.class);
        if(!foregroundServiceRunning()) {
            startForegroundService(autoServiceIntent);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance("https://doan-4abdf-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Device");

        bottom_nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
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

        FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();
        firebaseHandler.onDbChange(model -> {
            System.out.println("Pump " + model.getPump());
            switchCompat.setChecked(model.getPump());
        });
        DBHelper db = DBHelper.getInstance(Constants.PACKAGE_NAME, Constants.DATABASE_NAME);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    firebaseHandler.updateField("pump", true);
                } else {
                    String autoRunning = db.getByName(Constants.AUTO_RUNNING);
                    String scheduleRunning = db.getByName(Constants.SCHEDULE_RUNNING);

                    if (autoRunning.equals("1")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Chế độ auto đang được chạy, bạn muốn tắt?")
                                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        db.update(Constants.AUTO_MODE, "0");
                                        db.update(Constants.AUTO_RUNNING, "0");
                                        firebaseHandler.updateField("pump", false);
                                    }
                                })
                                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        switchCompat.setChecked(true);
                                    }
                                }).create().show();
                    } else if (scheduleRunning.equals("1")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Chế độ schedule đang được chạy, bạn muốn tắt?")
                                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        db.update(Constants.SCHEDULE_MODE, "0");
                                        db.update(Constants.SCHEDULE_RUNNING, "0");
                                        firebaseHandler.updateField("pump", false);
                                    }
                                })
                                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        switchCompat.setChecked(true);
                                    }
                                }).create().show();
                    } else {
                        firebaseHandler.updateField("pump", false);
                    }
                }
            }
        });
        temp();
        humid();
        getData();


    }

    public void temp() {
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

    public boolean foregroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (AutoService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void humid() {
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

    public void getData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean alive = (Boolean) snapshot.child("alive").getValue();
                Boolean pump = (Boolean) snapshot.child("pump").getValue();

                if (alive == true) {
                    databaseReference.child("alive").setValue(false);
                }

                if (pump == true) {
                    switchCompat.setChecked(true);
                } else {
                    switchCompat.setChecked(false);
                }
                Log.w(TAG, String.valueOf(snapshot));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getView() {
        btn_Verify = findViewById(R.id.btn_Verify);
        tv_Verify = findViewById(R.id.tv_Verify);
        arcGauge1 = findViewById(R.id.arcGauge1);
        arcGauge2 = findViewById(R.id.arcGauge2);
        switchCompat = findViewById(R.id.switchCompat);
        bottom_nav = findViewById(R.id.bottom_nav);
    }


}