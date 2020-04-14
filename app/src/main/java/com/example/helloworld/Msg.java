package com.example.helloworld;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Msg {
    private String message;
    private String date;
    private String name;
    private String flag;
    private String id;

    public Msg(String message, String flag) {
        this.message = message;
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        date.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        this.date = date.format(new Date());
        this.name = "User";
        this.flag = flag;
        this.id = "1155129475";
    }

    public Msg(String message, String date, String name, String flag) {
        this.message = message;
        this.date = date;
        this.name = name;
        this.flag = flag;
    }

    public String getMessage() {
        return message;
    }

    public String getFlag() {
        return flag;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Msg{" +
                "message='" + message + '\'' +
                ", date='" + date + '\'' +
                ", name='" + name + '\'' +
                ", flag='" + flag + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
