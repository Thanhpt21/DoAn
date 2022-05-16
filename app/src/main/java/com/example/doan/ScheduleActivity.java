package com.example.doan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduleActivity extends AppCompatActivity {
    Button btn_scheduleBackHome;
    ToggleButton tb_schedule;
    TextView tv_timer1,tv_timer2,tv_time;
    public static String SHARED_PREF1 = "shared1", SHARED_PREF2 = "shared2";
    public static String TEXT1 = "text1", TEXT2 = "text2";
    public String text1, text2;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    int t1Hour, t1Minute, t2Hour, t2Minute;
    Calendar calendar;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        getView();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://doan-4abdf-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Device");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        Boolean time = preferences.getBoolean("time", true);
        tb_schedule.setChecked(time);

        tb_schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("time", tb_schedule.isChecked());
                editor.commit();
            }
        });

        tb_schedule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    databaseReference.child("time").setValue(b);
                    tv_timer1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TimePickerDialog timePickerDialog = new TimePickerDialog(
                                    ScheduleActivity.this,
                                    new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker timePicker, int i, int i1) {
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
                                            Log.d("abc", stringAlarmTime);

                                            tv_timer1.setText(stringAlarmTime);
                                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF1, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString(TEXT1, tv_timer1.getText().toString());
                                            editor.apply();



                                            long period = 24 * 60 * 60 * 1000;
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    databaseReference.child("pump").setValue(true);
                                                    String message = "Pump is on";
                                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ScheduleActivity.this, "My notification");
                                                    builder.setSmallIcon(R.drawable.ic_message);
                                                    builder.setContentTitle("New Notification");
                                                    builder.setContentText(message);
                                                    builder.setAutoCancel(true);

                                                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ScheduleActivity.this);
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
                    Intent intent = new Intent(ScheduleActivity.this, AlarmReceiver.class);
                    alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

                    tv_timer2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TimePickerDialog timePickerDialog = new TimePickerDialog(
                                    ScheduleActivity.this,
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
                                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF2, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString(TEXT2, tv_timer2.getText().toString());
                                            editor.apply();
                                            pendingIntent = PendingIntent.getBroadcast(
                                                    ScheduleActivity.this,
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
                                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(ScheduleActivity.this, "My notification");
                                                    builder.setSmallIcon(R.drawable.ic_message);
                                                    builder.setContentTitle("New Notification");
                                                    builder.setContentText(message);
                                                    builder.setAutoCancel(true);

                                                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ScheduleActivity.this);
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
        update();

        btn_scheduleBackHome.setOnClickListener(view -> {
            backHome();
        });
    }

    public void update(){
        SharedPreferences sharedPreferences1 = getSharedPreferences(SHARED_PREF1, MODE_PRIVATE);
        text1 = sharedPreferences1.getString(TEXT1,"");
        tv_timer1.setText(text1);
        SharedPreferences sharedPreferences2 = getSharedPreferences(SHARED_PREF2, MODE_PRIVATE);
        text2 = sharedPreferences2.getString(TEXT2,"");
        tv_timer2.setText(text2);
    }


    public void getView(){
        btn_scheduleBackHome = findViewById(R.id.btn_scheduleBackHome);
        tb_schedule = findViewById(R.id.tb_schedule);
        tv_timer1 = findViewById(R.id.tv_timer1);
        tv_timer2 = findViewById(R.id.tv_timer2);
        tv_time = findViewById(R.id.tv_time);
    }

    public void backHome(){
        Intent intent = new Intent(ScheduleActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}