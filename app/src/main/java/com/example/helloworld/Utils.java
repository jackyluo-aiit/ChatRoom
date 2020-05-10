package com.example.helloworld;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
    public static Msg transferMsg(JSONObject result) {
        String message = null;
        String name = null;
        String id = null;
        String message_time = null;
        try {
            message = result.getString("message");
            name = result.getString("name");
            id = result.getString("user_id");
            message_time = result.getString("message_time");
            SimpleDateFormat utcSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            utcSdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = utcSdf.parse(message_time);

            SimpleDateFormat bjSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            bjSdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            message_time = bjSdf.format(date);

            System.out.println("message: " + message + ", name: " + name + ", user_id: " + id + ", message_time: " + message_time);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        if(id.equals("1155129475")){
            return new Msg(message, message_time, name, "send");
        }
        else {
            return new Msg(message, message_time, name, "received");
        }
    }

    public static void createNotification(Context context, Class c, String title, String content, boolean sound, boolean vibrate, boolean lights,
                                          String chatRoomId, String chatRoomName){
        Intent intent = new Intent(context, c);
        intent.putExtra("id", chatRoomId);
        intent.putExtra("name", chatRoomName);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setFullScreenIntent(pendingIntent, false);
        builder.setAutoCancel(true);
        int defaults = 0;
        if (sound){
            defaults|= Notification.DEFAULT_SOUND;
        }
        if (vibrate){
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (lights){
            defaults |= Notification.DEFAULT_SOUND;
        }
        builder.setDefaults(defaults);
        builder.setOngoing(true);
        if (Build.VERSION.SDK_INT >= 16){
            notification = builder.build();
        }
        else{
            notification = builder.getNotification();
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, notification);
    }

    public static void clear(Context context, String name){
        SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}
