package com.example.doan;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.LocalTime;
import java.util.Calendar;

public class AutoService extends Service {
    double humidity, temperature;

    Calendar calendar;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DBHelper db = DBHelper.getInstance(Constants.PACKAGE_NAME, Constants.DATABASE_NAME);
        FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();

        firebaseHandler.onDbChange(model -> {
            humidity = model.getHumi();
            temperature = model.getTemp();
        });

        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        calendar = Calendar.getInstance();
        Intent i = new Intent(AutoService.this, AlarmReceiver.class);
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        new Thread(() -> {
            while (true) {


                String autoMode = db.getByName(Constants.AUTO_MODE);
                String autoRunning = db.getByName(Constants.AUTO_RUNNING);
                String scheduleMode = db.getByName(Constants.SCHEDULE_MODE);
                String scheduleRunning = db.getByName(Constants.SCHEDULE_RUNNING);

                Log.d("Schedule", "Mode: " + scheduleMode);
                Log.d("Schedule", "Run: " + scheduleRunning);
                Log.d("Auto", "Mode: " + autoMode);
                Log.d("Auto", "Run: " + autoRunning);

                // nếu auto đang chạy thì schedule không được chạy và ngược lại
                if (autoMode.equals("1") && !scheduleRunning.equals("1")) {
                    double humiThreshold = Double.parseDouble(db.getByName(Constants.HUMIDITY));
                    double tempThreshold = Double.parseDouble(db.getByName(Constants.TEMPERATURE));

                    // nếu điều kiện vượt ngưỡng thì auto đc bật pump
                    // điều kiện khác ngưỡng và auto đang chạy thì tắt pump
                    if (humidity < humiThreshold || temperature > tempThreshold) {
                        if (autoRunning.equals("0")) {
                            firebaseHandler.updateField("pump", true);
                            Log.d("Pump", "set pump");
                            db.update(Constants.AUTO_RUNNING, "1");
                        }
                    } else {
                        if (autoRunning.equals("1")) {
                            firebaseHandler.updateField("pump", false);
                            db.update(Constants.AUTO_RUNNING, "0");
                        }
                    }
                } else if (scheduleMode.equals("1") && !autoRunning.equals("1")) {
                    LocalTime timeStart = LocalTime.parse(db.getByName(Constants.SCHEDULE_START));
                    LocalTime timeEnd = LocalTime.parse(db.getByName(Constants.SCHEDULE_END));
                    LocalTime timeNow = LocalTime.now();

                    if (timeNow.isAfter(timeStart) && timeNow.isBefore(timeEnd)) {
                        if(scheduleRunning.equals("0")) {
                            firebaseHandler.updateField("pump", true);
                            db.update(Constants.SCHEDULE_RUNNING, "1");
                        }
                    }
                    else {
                        if (scheduleRunning.equals("1")) {
                            firebaseHandler.updateField("pump", false);
                            pendingIntent = PendingIntent.getBroadcast(
                                    AutoService.this,
                                    0,
                                    i,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            db.update(Constants.SCHEDULE_RUNNING, "0");
                        }
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.d("TAG", "dmdmdmdmd");
                }

            }
        }).start();

        startForegroundAutoService();

        return super.onStartCommand(intent, flags, startId);
    }


    private void startForegroundAutoService() {
        String CHANNEL_ID = "AUTO SERVICE";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentText("Monitoring your plant")
                .setContentTitle("Smart Plant Monitoring")
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        startForeground(1001, notification.build());
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}