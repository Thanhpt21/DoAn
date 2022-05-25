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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AutoActivity extends AppCompatActivity implements ExampleDialog.ExampleDialogListener {
    ToggleButton tb_auto;
    TextView tv_valueTemp, tv_valueHumid, tv_statusAuto;
    Button btn_setValue, btn_autoBackHome;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    public static String SHARED_PREF3 = "shared3", SHARED_PREF4 = "shared4";
    public static String TEXT3 = "text3", TEXT4 = "text4";
    public String text3, text4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);
        getView();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://doan-4abdf-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Device");
        getData();

        tb_auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    databaseReference.child("auto").setValue(b);
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Boolean auto =(Boolean) snapshot.child("auto").getValue();
                            String humid = snapshot.child("humi").getValue().toString();
                            String temp = snapshot.child("temp").getValue().toString();

                            btn_setValue.setOnClickListener(view -> {
                                openDialog();
                                tb_auto.setChecked(false);
                            });


                            SharedPreferences sharedPreferences1 = getSharedPreferences(SHARED_PREF3, MODE_PRIVATE);
                            text3 = sharedPreferences1.getString(TEXT3,"");
                            tv_valueTemp.setText(text3);

                            SharedPreferences sharedPreferences2 = getSharedPreferences(SHARED_PREF4, MODE_PRIVATE);
                            text4 = sharedPreferences2.getString(TEXT4,"");
                            tv_valueHumid.setText(text4);

                            String tempAuto = tv_valueTemp.getText().toString();
                            String humidAuto = tv_valueHumid.getText().toString();


                            tv_statusAuto.setText(auto.toString());
                            if(auto == true){
                                if(Float.parseFloat(humid) >= Float.parseFloat(humidAuto) || Float.parseFloat(temp) >= Float.parseFloat(tempAuto) ){
                                    databaseReference.child("pump").setValue(true);
                                }else {
                                    databaseReference.child("pump").setValue(false);
                                }
                                tv_statusAuto.setText("Đang bật");
                            }else {
                                tv_statusAuto.setText("Đã tắt");
                            }
                            Log.w(TAG, String.valueOf(snapshot));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }else {
                    databaseReference.child("auto").setValue(b);
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
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF3, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEXT3, tv_valueTemp.getText().toString());
        editor.apply();

        SharedPreferences sharedPreferences1 = getSharedPreferences(SHARED_PREF4, MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor1.putString(TEXT4, tv_valueHumid.getText().toString());
        editor1.apply();

    }

    public void getView(){
        tb_auto = findViewById(R.id.tb_auto);
        tv_valueTemp = findViewById(R.id.tv_valueTemp);
        tv_valueHumid = findViewById(R.id.tv_valueHumid);
        tv_statusAuto = findViewById(R.id.tv_statusAuto);
        btn_setValue = findViewById(R.id.btn_setValue);
        btn_autoBackHome = findViewById(R.id.btn_autoBackHome);
    }

    public void getData(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean auto = (Boolean) snapshot.child("auto").getValue();
                Boolean time = (Boolean) snapshot.child("time").getValue();

                if (time == true) {
                    tb_auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            tv_statusAuto.setText("Hẹn giờ đang bật");
                            tb_auto.setChecked(false);

                        }
                    });
                }else {
                    if (auto == true) {
                        tb_auto.setChecked(true);
                        tv_statusAuto.setText("Đang bật");
                        String humid = snapshot.child("humi").getValue().toString();
                        String temp = snapshot.child("temp").getValue().toString();
                        String tempAuto = tv_valueTemp.getText().toString();
                        String humidAuto = tv_valueHumid.getText().toString();
                        if(Float.parseFloat(humid) >= Float.parseFloat(humidAuto) || Float.parseFloat(temp) >= Float.parseFloat(tempAuto) ){
                            String message = "Máy bơm đang bật do chỉ số nằm trên mức quy định";
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(AutoActivity.this, "My notification");
                            builder.setSmallIcon(R.drawable.ic_message);
                            builder.setContentTitle("Thông báo mới");
                            builder.setContentText(message);
                            builder.setAutoCancel(true);

                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(AutoActivity.this);
                            notificationManagerCompat.notify(1, builder.build());
                        }else {
                            String message = "Máy bơm tắt do chỉ số nằm dưới mức quy định";
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(AutoActivity.this, "My notification");
                            builder.setSmallIcon(R.drawable.ic_message);
                            builder.setContentTitle("Thông báo mới");
                            builder.setContentText(message);
                            builder.setAutoCancel(true);

                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(AutoActivity.this);
                            notificationManagerCompat.notify(1, builder.build());
                        }
                    } else {
                        tb_auto.setChecked(false);
                        tv_statusAuto.setText("Đã tắt");
                    }
                }



            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    public void backHome(){
        Intent intent = new Intent(AutoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}