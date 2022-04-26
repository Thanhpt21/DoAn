package com.example.doan;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ekn.gruzer.gaugelibrary.ArcGauge;
import com.ekn.gruzer.gaugelibrary.Range;
import com.example.doan.model.Device;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    EditText et_SetMinute, et_Pump;
    TextView tv_Verify, tv_Countdown,a,b,c,d;
    Button btn_Verify, btn_Personal, btn_start_pause, btn_resetTime, btn_SetTime, btn_readData, btn_updatePump;
    SwitchCompat switchCompat;
    ArcGauge arcGauge1, arcGauge2;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long startTimeInMillis;
    private long timeLeftInMillis;
    private long endTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getView();
        firebaseAuth = FirebaseAuth.getInstance();


        btn_readData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference = FirebaseDatabase.getInstance("https://doan-4abdf-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Device");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String humi = snapshot.child("humi").getValue().toString();
                        String temp = snapshot.child("temp").getValue().toString();
                        Boolean pump =(Boolean) snapshot.child("pump").getValue();
                        Boolean alive =(Boolean) snapshot.child("alive").getValue();
                        a.setText(humi);
                        b.setText(temp);
                        c.setText(pump.toString());
                        d.setText(alive.toString());
                        Log.w(TAG, String.valueOf(snapshot));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btn_updatePump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });



        FirebaseUser user = firebaseAuth.getCurrentUser();

        temp();
        humid();

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

        SharedPreferences sharedPreferences = getSharedPreferences("save", MODE_PRIVATE);
        switchCompat.setChecked(sharedPreferences.getBoolean("pump", false));


        btn_start_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timerRunning){
                    pauseTimer();
                }else {
                    startTimer();
                }
            }
        });

        btn_resetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });

        btn_SetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String minutes = et_SetMinute.getText().toString();
                if(minutes.length() == 0 ){
                    Toast.makeText(MainActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                long millisInput = Long.parseLong(minutes)*60000;
                if(millisInput == 0){
                    Toast.makeText(MainActivity.this,"Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                et_SetMinute.setText("");
            }
        });



    }



    public void temp(){
        Range range = new Range();
        range.setColor(Color.parseColor("#E3E500"));
        range.setFrom(0.0);
        range.setTo(100.0);

        arcGauge1.addRange(range);
        arcGauge1.setMinValue(0.0);
        arcGauge1.setMaxValue(100.0);
        arcGauge1.setValue(45.0);
        arcGauge1.setUseRangeBGColor(true);
    }

    public void humid(){
        Range range = new Range();
        range.setColor(Color.parseColor("#9BED3B"));
        range.setFrom(0.0);
        range.setTo(100.0);

        arcGauge2.addRange(range);
        arcGauge2.setMinValue(0.0);
        arcGauge2.setMaxValue(100.0);
        arcGauge2.setValue(34.0);
        arcGauge2.setUseRangeBGColor(true);
    }

    public void setTime(long milliseconds){
        startTimeInMillis = milliseconds;
        resetTimer();
    }

    public void startTimer(){
        endTime = System.currentTimeMillis() + timeLeftInMillis;
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftInMillis = l;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                updateWatchInterface();
            }
        }.start();

        timerRunning = true;
        updateWatchInterface();
    }

    public void pauseTimer(){
        countDownTimer.cancel();
        timerRunning = false;
        updateWatchInterface();
    }

    public void resetTimer(){
        timeLeftInMillis = startTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
        closeKeyboard();
    }

    public void updateCountDownText(){
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if(hours > 0){
            timeLeftFormatted = String.format(Locale.getDefault(),"%d:%02d:%02d", hours, minutes, seconds);
        }else {
            timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
        }
        tv_Countdown.setText(timeLeftFormatted);
    }

    public void updateWatchInterface(){
        if(timerRunning){
            et_SetMinute.setVisibility(View.INVISIBLE);
            btn_SetTime.setVisibility(View.INVISIBLE);
            btn_resetTime.setVisibility(View.INVISIBLE);
            btn_start_pause.setText("Pause");
        }else {
            et_SetMinute.setVisibility(View.VISIBLE);
            btn_SetTime.setVisibility(View.VISIBLE);
            btn_start_pause.setText("Start");
            if(timeLeftInMillis < 1000){
                btn_start_pause.setVisibility(View.INVISIBLE);
            } else {
                btn_start_pause.setVisibility(View.VISIBLE);
            }

            if(timeLeftInMillis < startTimeInMillis){
                btn_resetTime.setVisibility(View.VISIBLE);
            }else {
                btn_resetTime.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("startTimeInMillis", startTimeInMillis);
        editor.putLong("millisLeft", timeLeftInMillis);
        editor.putBoolean("timerRunning", timerRunning);
        editor.putLong("endTime", endTime);

        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        startTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        timeLeftInMillis = prefs.getLong("millisLeft", startTimeInMillis);
        timerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDownText();
        updateWatchInterface();

        if(timerRunning){
            endTime = prefs.getLong("endTime", 0);
            timeLeftInMillis=  endTime - System.currentTimeMillis();
            if(timeLeftInMillis < 0){
                timeLeftInMillis = 0;
                timerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            }else {
                startTimer();
            }
        }

    }

    public void getView(){
        btn_Verify = findViewById(R.id.btn_Verify);
        tv_Verify = findViewById(R.id.tv_Verify);
        btn_Personal = findViewById(R.id.btn_Personal);
        switchCompat = findViewById(R.id.switchCompat);
        tv_Countdown = findViewById(R.id.tv_Countdown);
        btn_start_pause = findViewById(R.id.btn_start_pause);
        btn_resetTime = findViewById(R.id.btn_resetTime);
        btn_SetTime = findViewById(R.id.btn_SetTime);
        et_SetMinute = findViewById(R.id.et_SetMinute);
        arcGauge1 = findViewById(R.id.arcGauge1);
        arcGauge2 = findViewById(R.id.arcGauge2);
        btn_readData = findViewById(R.id.btn_readData);
        btn_updatePump = findViewById(R.id.btn_updatePump);
        a = findViewById(R.id.a);
        b = findViewById(R.id.b);
        c = findViewById(R.id.c);
        d = findViewById(R.id.d);
        et_Pump = findViewById(R.id.et_Pump);
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