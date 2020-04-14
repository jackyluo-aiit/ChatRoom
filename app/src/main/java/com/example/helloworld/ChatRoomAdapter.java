package com.example.helloworld;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomAdapter extends BaseAdapter {
    private ArrayList<Map<String, Object>> list = new ArrayList<>();
    private Context context;
    private TextView chatRoomName;

    public ChatRoomAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(HashMap<String, Object> map){
        list.add(map);
    }

    public void deleteItem(){
        this.list.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_item, null);
        }
        chatRoomName = convertView.findViewById(R.id.chatRoomName);
        chatRoomName.setText((list.get(position).get("name").toString()));
        return convertView;

    }


}
