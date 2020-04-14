package com.example.helloworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChatArrayAdapter extends ArrayAdapter<Msg> {
    private TextView chatText;
    private List<Msg> chatMessageList = new ArrayList<Msg>();
    private Context context;
    private TextView chatTime;
    private TextView user;

    public ChatArrayAdapter(Context context, int textViewId){
        super(context, textViewId);
        this.context = context;
    }

    @Override
    public void add(@Nullable Msg object) {
        chatMessageList.add(object);
    }

    public void addHistory(Msg object){
        chatMessageList.add(0, object);
    }

    public int getCount(){
        return chatMessageList.size();
    }

    public void deleteItem(){
        chatMessageList.clear();
    }

    public Msg getMessage(int id){
        return chatMessageList.get(id);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        Msg msg = getMessage(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String flag = msg.getFlag();
        if(flag.equals("send")) {
            row = inflater.inflate(R.layout.message_send, parent, false);
        }
        else{
            row = inflater.inflate(R.layout.message_received, parent, false);
        }
        chatText = (TextView) row.findViewById(R.id.message_body);
        chatTime = row.findViewById(R.id.message_time);
        user = row.findViewById(R.id.user);
        chatText.setText(msg.getMessage());
        chatTime.setText(msg.getDate());
        user.setText(msg.getName());
        return row;
    }
}
