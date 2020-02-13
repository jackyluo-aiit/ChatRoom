package com.example.helloworld;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Msg {
    private String message;
    private String date;

    public Msg(String message){
        this.message = message;
        SimpleDateFormat date = new SimpleDateFormat("HH:mm");
        this.date = date.format(new Date());
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return message + "\n"+date;
    }
}
