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
    Button btn_setValue, btn_startAuto, btn_autoBackHome;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);
        getView();

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://doan-4abdf-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Device");

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        Boolean auto = preferences.getBoolean("auto", true);
        tb_auto.setChecked(auto);

        tb_auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("auto", tb_auto.isChecked());
                editor.commit();
            }
        });

        tb_auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    databaseReference.child("auto").setValue(b);
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Boolean auto =(Boolean) snapshot.child("auto").getValue();
                            String humi = snapshot.child("humi").getValue().toString();
                            String temp = snapshot.child("temp").getValue().toString();
                            String tempAuto = tv_valueTemp.getText().toString();
                            String humiAuto = tv_valueHumid.getText().toString();
//                            if(Integer.parseInt(humi) >= Integer.parseInt(humiAuto)){
//                                String message = "Pump is on";
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(AutoActivity.this, "My notification");
//                                builder.setSmallIcon(R.drawable.ic_message);
//                                builder.setContentTitle("New Notification");
//                                builder.setContentText(message);
//                                builder.setAutoCancel(true);
//
//                                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(AutoActivity.this);
//                                notificationManagerCompat.notify(1, builder.build());
//                            }

                            tv_statusAuto.setText(auto.toString());
                            if(auto == true){
                                if(Integer.parseInt(humi) >= Integer.parseInt(humiAuto)){
                                    databaseReference.child("pump").setValue(true);
                                }else if(Integer.parseInt(temp) >= Integer.parseInt(tempAuto)){
                                    databaseReference.child("pump").setValue(true);
                                }else {
                                    databaseReference.child("pump").setValue(false);
                                }
                                tv_statusAuto.setText("Auto is On");
                            }else {
                                tv_statusAuto.setText("Auto is Off");
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
        btn_startAuto = findViewById(R.id.btn_startAuto);
        btn_autoBackHome = findViewById(R.id.btn_autoBackHome);
    }

    public void backHome(){
        Intent intent = new Intent(AutoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}