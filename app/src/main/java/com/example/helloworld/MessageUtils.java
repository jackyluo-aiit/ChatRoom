package com.example.helloworld;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MessageUtils {
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
}
