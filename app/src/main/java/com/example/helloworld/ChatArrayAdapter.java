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

    public ChatArrayAdapter(Context context, int textViewId){
        super(context, textViewId);
        this.context = context;
    }

    @Override
    public void add(@Nullable Msg object) {
        super.add(object);
        chatMessageList.add(object);
    }

    public int getCount(){
        return chatMessageList.size();
    }

    public Msg getMessage(int id){
        return chatMessageList.get(id);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        Msg msg = getMessage(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        row = inflater.inflate(R.layout.message_send, parent, false);
        chatText = (TextView) row.findViewById(R.id.message_body_send);
        chatTime = row.findViewById(R.id.send_message_time);
        chatText.setText(msg.getMessage());
        chatTime.setText(msg.getDate());
        return row;
    }
}
