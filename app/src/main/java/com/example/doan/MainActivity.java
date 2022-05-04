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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.Calendar;
import java.util.Date;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ExampleDialog.ExampleDialogListener {
    TextView tv_Verify,d, tv_timer1, tv_timer2, tv_time, tv_humi, tv_temp;
    Button btn_Verify, btn_Personal, btn_showHide, btn_editAuto;
    ArcGauge arcGauge1, arcGauge2;
    ToggleButton tb_auto, tb_time;
    SwitchCompat switchCompat;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    int t1Hour, t1Minute, t2Hour, t2Minute;
    Calendar calendar;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getView();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance("https://doan-4abdf-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Device");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        btn_showHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Boolean auto =(Boolean) snapshot.child("auto").getValue();
                        String humi = snapshot.child("humi").getValue().toString();
                        String temp = snapshot.child("temp").getValue().toString();
                        String tempAuto = tv_temp.getText().toString();
                        String humiAuto = tv_humi.getText().toString();


                        d.setText(auto.toString());
                        if(auto == true){
                            if(Integer.parseInt(humi) >= Integer.parseInt(humiAuto)){
                                databaseReference.child("pump").setValue(true);
                            }else if(Integer.parseInt(temp) >= Integer.parseInt(tempAuto)){
                                databaseReference.child("pump").setValue(true);
                            }else {
                                databaseReference.child("pump").setValue(false);
                            }
                            d.setText("Auto is On");
                        }else {
                            d.setText("Auto is Off");
                        }
                        Log.w(TAG, String.valueOf(snapshot));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        tb_auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    databaseReference.child("auto").setValue(b);

                }else {
                    databaseReference.child("auto").setValue(b);
                }
            }
        });
        btn_editAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        tb_time.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    databaseReference.child("time").setValue(b);
                    tv_timer1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TimePickerDialog timePickerDialog = new TimePickerDialog(
                                    MainActivity.this,
                                    new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                            switchCompat.setChecked(false);
                                            t1Hour = i;
                                            t1Minute = i1;
                                            calendar.set(Calendar.HOUR_OF_DAY,t1Hour);
                                            calendar.set(Calendar.MINUTE,t1Minute);
                                            calendar.set(Calendar.SECOND,0);
                                            calendar.set(Calendar.MILLISECOND,0);
                                            Date dateSchedule = calendar.getTime();
                                            int hours = t1Hour;
                                            int minutes = t1Minute;
                                            String str_hours = String.valueOf(hours);
                                            String str_minutes = String.valueOf(minutes);
                                            if(minutes<10){
                                                str_minutes ="0";
                                                str_minutes = str_minutes.concat(String.valueOf(minutes));
                                            }else {
                                                str_minutes = String.valueOf(minutes);
                                            }

                                            String stringAlarmTime;
                                            if(hours >= 12){
                                                stringAlarmTime = String.valueOf(hours).concat(":").concat(str_minutes).concat(" PM");
                                            }else {
                                                stringAlarmTime = String.valueOf(hours).concat(":").concat(str_minutes).concat(" AM");
                                            }

                                            tv_timer1.setText(stringAlarmTime);

                                            long period = 24 * 60 * 60 * 1000;
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    databaseReference.child("pump").setValue(true);
                                                    String message = "Pump is on";
                                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My notification");
                                                    builder.setSmallIcon(R.drawable.ic_message);
                                                    builder.setContentTitle("New Notification");
                                                    builder.setContentText(message);
                                                    builder.setAutoCancel(true);

                                                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);
                                                    notificationManagerCompat.notify(1, builder.build());
                                                }
                                            };
                                            Timer timer = new Timer();
                                            timer.schedule(task, dateSchedule, period);


                                        }
                                    }, 24, 0 , true
                            );

                            timePickerDialog.updateTime(t1Hour, t1Minute);
                            timePickerDialog.show();
                        }
                    });

                    final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
                    calendar = Calendar.getInstance();
                    Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                    alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                    tv_timer2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TimePickerDialog timePickerDialog = new TimePickerDialog(
                                    MainActivity.this,
                                    new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                            t2Hour = i;
                                            t2Minute = i1;
                                            calendar.set(Calendar.HOUR_OF_DAY,t2Hour);
                                            calendar.set(Calendar.MINUTE,t2Minute);
                                            calendar.set(Calendar.SECOND,0);
                                            calendar.set(Calendar.MILLISECOND,0);
                                            Date dateSchedule = calendar.getTime();
                                            int hours = t2Hour;
                                            int minutes = t2Minute;
                                            String str_hours = String.valueOf(hours);
                                            String str_minutes = String.valueOf(minutes);
                                            if(minutes<10){
                                                str_minutes ="0";
                                                str_minutes = str_minutes.concat(String.valueOf(minutes));
                                            }else {
                                                str_minutes = String.valueOf(minutes);
                                            }

                                            String stringAlarmTime;
                                            if(hours >= 12){
                                                stringAlarmTime = String.valueOf(hours).concat(":").concat(str_minutes).concat(" PM");
                                            }else {
                                                stringAlarmTime = String.valueOf(hours).concat(":").concat(str_minutes).concat(" AM");
                                            }
                                            tv_timer2.setText(stringAlarmTime);
                                            pendingIntent = PendingIntent.getBroadcast(
                                                    MainActivity.this,
                                                    0,
                                                    intent,
                                                    PendingIntent.FLAG_UPDATE_CURRENT
                                            );
                                            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                                            long period = 24 * 60 * 60 * 1000;
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    databaseReference.child("pump").setValue(false);
                                                    String message = "Pump is off";
                                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My notification");
                                                    builder.setSmallIcon(R.drawable.ic_message);
                                                    builder.setContentTitle("New Notification");
                                                    builder.setContentText(message);
                                                    builder.setAutoCancel(true);

                                                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);
                                                    notificationManagerCompat.notify(1, builder.build());
                                                }
                                            };
                                            Timer timer = new Timer();
                                            timer.schedule(task, dateSchedule, period);
                                        }
                                    },24,0, true
                            );
                            timePickerDialog.updateTime(t2Hour, t2Minute);
                            timePickerDialog.show();
                        }
                    });
                    tv_time.setText("Auto time is on");
                }else {
                    databaseReference.child("time").setValue(b);
                    tv_time.setText("Auto time is off");
                    tv_timer1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tv_time.setText("No handle when off");
                            tv_timer1.setText("StartTime");
                        }
                    });
                    tv_timer2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tv_time.setText("No handle when off");
                            tv_timer2.setText("EndTime");
                        }
                    });
                }

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

        if(!user.isEmailVerified()){
            tv_Verify.setVisibility(View.VISIBLE);
            btn_Verify.setVisibility(View.VISIBLE);
            btn_Verify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(view.getContext(), "Verification Email has been sent", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Email not send " + e.getMessage());
                        }
                    });
                }
            });
        }

        btn_Personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirect();
            }
        });


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
                Boolean time = (Boolean) snapshot.child("time").getValue();
                Boolean auto = (Boolean) snapshot.child("auto").getValue();
                Boolean pump =(Boolean) snapshot.child("pump").getValue();
                if(pump == true){
                    switchCompat.setChecked(true);
                }else {
                    switchCompat.setChecked(false);
                }
                Log.w(TAG, String.valueOf(snapshot));

                if(time==true){
                    tb_time.setChecked(true);
                }else {
                    tb_time.setChecked(false);
                }

                if(auto==true){
                    tb_auto.setChecked(true);
                }else {
                    tb_auto.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void openDialog(){
        ExampleDialog exampleDialog = new ExampleDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");

    }

    @Override
    public void applyTexts(String temp, String humi) {
        tv_temp.setText(temp);
        tv_humi.setText(humi);
    }

    public void getView(){
        btn_Verify = findViewById(R.id.btn_Verify);
        tv_Verify = findViewById(R.id.tv_Verify);
        btn_Personal = findViewById(R.id.btn_Personal);
        arcGauge1 = findViewById(R.id.arcGauge1);
        arcGauge2 = findViewById(R.id.arcGauge2);
        btn_showHide = findViewById(R.id.btn_showHide);
        btn_editAuto = findViewById(R.id.btn_editAuto);
        d = findViewById(R.id.d);
        switchCompat = findViewById(R.id.switchCompat);
        tv_timer1 = findViewById(R.id.tv_timer1);
        tv_timer2 = findViewById(R.id.tv_timer2);
        tv_time = findViewById(R.id.tv_time);
        tv_humi = findViewById(R.id.tv_humi);
        tv_temp = findViewById(R.id.tv_temp);
        tb_auto =  findViewById(R.id.tb_auto);
        tb_time = findViewById(R.id.tb_time);
    }

    public void Logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    public void redirect(){
        Intent intent = new Intent(MainActivity.this, PersonalActivity.class);
        startActivity(intent);
        finish();
    }


}