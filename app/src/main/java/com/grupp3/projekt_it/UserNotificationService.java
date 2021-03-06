package com.grupp3.projekt_it;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by Daniel on 2015-05-06.
 */
public class UserNotificationService  extends IntentService {
    String TAG = "com.grupp3.projekt_it";

    public UserNotificationService() {
        super("UserNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "UserNotificationService");
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean notifi = getPrefs.getBoolean("notification_on", true);

        if (notifi == true ) {

            Bundle bundle = intent.getExtras();
            if(bundle == null) {
                return;
            }
            //String id = bundle.getI
            String json =  bundle.getString("jsonUserNotification");
            GardenUtil gardenUtil = new GardenUtil();
            //UserNotification userNotification = gardenUtil.loadUserNotification(id, getBaseContext());
            Gson gson = new Gson();
            UserNotification userNotification = gson.fromJson(json, UserNotification.class);
            if(userNotification == null){
                Log.i(TAG, "fuck up located");
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setAutoCancel(true);
            builder.setContentTitle(userNotification.getTitle());
            builder.setContentText(userNotification.getText());
            builder.setSmallIcon(R.mipmap.app_icon_launch);

            Intent intent1 = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            Notification notification = builder.build();

            boolean vibrate = getPrefs.getBoolean("notification_vibration", true);
            if (vibrate == true) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }

            boolean sound = getPrefs.getBoolean("notification_sound", true);
            if (sound == true) {
                notification.defaults |= Notification.DEFAULT_SOUND;
            }

            notification.defaults |= Notification.DEFAULT_LIGHTS;
            NotificationManager manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            manager.notify(5, notification);

            GardenUtil gardenUtil1 = new GardenUtil();
            gardenUtil.deleteUserNotification(userNotification.getId(), getApplicationContext());
        }
    }
}
