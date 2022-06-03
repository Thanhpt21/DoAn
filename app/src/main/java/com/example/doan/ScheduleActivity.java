package com.example.doan;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.AlertDialog;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduleActivity extends AppCompatActivity {
    Button btn_scheduleBackHome;
    SwitchCompat tb_schedule;
    TextView tv_timer1,tv_timer2,tv_time;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;


    int startHour, startMinute, endHour, endMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        getView();

        DBHelper db = DBHelper.getInstance(Constants.PACKAGE_NAME, Constants.DATABASE_NAME);
        FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://doan-4abdf-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Device");


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        if(!db.getByName(Constants.SCHEDULE_START).equals("")){
            tv_timer1.setText(db.getByName(Constants.SCHEDULE_START));
            LocalTime time = LocalTime.parse(db.getByName(Constants.SCHEDULE_START));
            startHour = time.getHour();
            startMinute = time.getMinute();
        }

        if(!db.getByName(Constants.SCHEDULE_END).equals("")){
            tv_timer2.setText(db.getByName(Constants.SCHEDULE_END));
            LocalTime time = LocalTime.parse(db.getByName(Constants.SCHEDULE_END));
            endHour = time.getHour();
            endMinute = time.getMinute();
        }

        tv_timer1.setOnClickListener(view->{
            TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    startHour = hourOfDay;
                    startMinute = minute;
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute, 0);
                    tv_timer1.setText(time);
                    db.update(Constants.SCHEDULE_START, time);
                }
            };

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener,
                    startHour, startMinute,true);
            timePickerDialog.setTitle("Chọn thời gian bắt đầu");
            timePickerDialog.show();
        });

        tv_timer2.setOnClickListener(view->{
            TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    endHour = hourOfDay;
                    endMinute = minute;
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute, 0);
                    tv_timer2.setText(time);
                    db.update(Constants.SCHEDULE_END, time);

                }
            };


            TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener,
                    endHour, endMinute,true);
            timePickerDialog.setTitle("Chọn thời gian kết thúc");
            timePickerDialog.show();
        });

        if(db.getByName(Constants.SCHEDULE_MODE).equals("1")){
            tb_schedule.setChecked(true);
        }

        tb_schedule.setOnClickListener(view->{
            if(((SwitchCompat) view).isChecked()){
                db.update(Constants.SCHEDULE_MODE,"1");
            }
            else{
                db.update(Constants.SCHEDULE_MODE,"0");
                db.update(Constants.SCHEDULE_RUNNING,"0");
                firebaseHandler.updateField("pump",false);

            }
        });


        btn_scheduleBackHome.setOnClickListener(view -> {
            backHome();
        });
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