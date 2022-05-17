package com.example.doan;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
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
                    String message = "Chế độ tự động đang bật";
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(AutoActivity.this, "My notification");
                    builder.setSmallIcon(R.drawable.ic_message);
                    builder.setContentTitle("Thông báo mới");
                    builder.setContentText(message);
                    builder.setAutoCancel(true);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(AutoActivity.this);
                    notificationManagerCompat.notify(1, builder.build());
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Boolean auto =(Boolean) snapshot.child("auto").getValue();
                            String humid = snapshot.child("humi").getValue().toString();
                            String temp = snapshot.child("temp").getValue().toString();
                            String tempAuto = tv_valueTemp.getText().toString();
                            String humidAuto = tv_valueHumid.getText().toString();


                            tv_statusAuto.setText(auto.toString());
                            if(auto == true){
                                if(Integer.parseInt(humid) >= Integer.parseInt(humidAuto)){
                                    databaseReference.child("pump").setValue(true);
                                }else if(Integer.parseInt(temp) >= Integer.parseInt(tempAuto)){
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
                    String message = "Chế độ tự động đã tắt";
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(AutoActivity.this, "My notification");
                    builder.setSmallIcon(R.drawable.ic_message);
                    builder.setContentTitle("Thông báo mới");
                    builder.setContentText(message);
                    builder.setAutoCancel(true);

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(AutoActivity.this);
                    notificationManagerCompat.notify(2, builder.build());
                }
            }

        });

        btn_setValue.setOnClickListener(view -> {
            openDialog();
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
                            tv_statusAuto.setText("Đã tắt");
                            String message = "Không thể xữ lý do chế độ hẹn giờ đang được bật!";
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(AutoActivity.this, "My notification");
                            builder.setSmallIcon(R.drawable.ic_message);
                            builder.setContentTitle("Thông báo mới !");
                            builder.setContentText(message);
                            builder.setAutoCancel(true);

                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(AutoActivity.this);
                            notificationManagerCompat.notify(1, builder.build());
                            tb_auto.setChecked(false);
                        }
                    });
                }else {
                    if (auto == true) {
                        String humid = snapshot.child("humi").getValue().toString();
                        String temp = snapshot.child("temp").getValue().toString();
                        String tempAuto = tv_valueTemp.getText().toString();
                        String humidAuto = tv_valueHumid.getText().toString();
                        if(Integer.parseInt(humid) >= Integer.parseInt(humidAuto) || Integer.parseInt(temp) >= Integer.parseInt(tempAuto) ){
                            String message = "Máy bơm đang bật do chỉ số nằm trên mức quy định";
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(AutoActivity.this, "My notification");
                            builder.setSmallIcon(R.drawable.ic_message);
                            builder.setContentTitle("Thông báo mới");
                            builder.setContentText(message);
                            builder.setAutoCancel(true);

                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(AutoActivity.this);
                            notificationManagerCompat.notify(2, builder.build());
                        }else {
                            String message = "Máy bơm tắt do chỉ số nằm dưới mức quy định";
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(AutoActivity.this, "My notification");
                            builder.setSmallIcon(R.drawable.ic_message);
                            builder.setContentTitle("Thông báo mới");
                            builder.setContentText(message);
                            builder.setAutoCancel(true);

                            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(AutoActivity.this);
                            notificationManagerCompat.notify(2, builder.build());
                        }
                        tb_auto.setChecked(true);
                        tv_statusAuto.setText("Đang bật");
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