package com.example.doan;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.LocalTime;

public class AutoService extends Service {
    double humidity, temperature;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        DBHelper db = DBHelper.getInstance(Constants.PACKAGE_NAME, Constants.DATABASE_NAME);
        FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();
        createNotificationChannel();

        Intent notifyIntent = new Intent(this, com.example.doan.Notification.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_IMMUTABLE);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notify = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle("IOT")
                .setSound(alarmSound)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        firebaseHandler.onDbChange(model -> {
            humidity = model.getHumi();
            temperature = model.getTemp();
            Log.d("Temp", String.valueOf(temperature));
        });

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
                            db.update(Constants.AUTO_RUNNING, "1");
                            notify.setContentText("Chức năng auto đang chạy");
                            notificationManager.notify(1, notify.build());
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
                            notify.setContentText("Chức năng lên lịch đang chạy");
                            notificationManager.notify(1, notify.build());
                        }
                    }
                    else {
                        if (scheduleRunning.equals("1")) {
                            firebaseHandler.updateField("pump", false);
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = Constants.NOTIFY_CHANNEL;
            String description = Constants.NOTIFY_CHANNEL;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}