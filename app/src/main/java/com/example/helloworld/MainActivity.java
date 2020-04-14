package com.example.helloworld;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private String result;
    private ListView listView;
    private ChatRoomAdapter chatRoomAdapter;
    private String baseUrl = "http://192.168.31.26:5000/api/a3/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("IEMS5722");
        listView = findViewById(R.id.chatRoomList);
        chatRoomAdapter=new ChatRoomAdapter(getApplicationContext());
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                OkHttpUtils okHttpUtils = new OkHttpUtils();
//                result = okHttpUtils.get("http://18.220.14.97/api/a2/get_chatrooms");
//                if(!result.equals("error")){
//                    handler.sendMessage(handler.obtainMessage(1, result));
//                }else if(result.equals("error")){
//                    handler.sendMessage(handler.obtainMessage(404));
//                }
//            }
//        }).start();
        refresh();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, Objects> item = (HashMap<String, Objects>) chatRoomAdapter.getItem(i);
//                Toast.makeText(MainActivity.this, "Click " + item.get("name"), Toast.LENGTH_SHORT).show();
                Object id = item.get("id");
                Object name = item.get("name");
                assert name != null;
                openChatRoom(view, id.toString(), name.toString());
            }
        });
    }

    public void parseJson(String content, ChatRoomAdapter chatRoomAdapter){
        /*
        {"data":[{"id":2,"name":"General Chatroom"},{"id":3,"name":"Chatroom 2"},{"id":4,"name":"Catroom 3"}],"status":"OK"}
         */
        if(chatRoomAdapter.getCount()!=0){
            chatRoomAdapter.deleteItem();
        }
        if(content != null){
            try {
                JSONObject jsonObject = new JSONObject(content);
                String resultCode = jsonObject.getString("status");
                if(resultCode.equals("OK")){
                    JSONArray resultJsonArray = jsonObject.getJSONArray("data");
                    int len = resultJsonArray.length();
                    for (int i = 0; i<len; i++){
                        HashMap<String, Object> map = new HashMap<>();
                        JSONObject resultObject = resultJsonArray.getJSONObject(i);
                        Integer id = resultObject.getInt("id");
                        String name = resultObject.getString("name");
                        System.out.println("id: "+id+", name: "+name);
                        map.put("id", id);
                        map.put("name", name);
                        chatRoomAdapter.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    parseJson((String) msg.obj, chatRoomAdapter);
                    listView.setAdapter(chatRoomAdapter);
                    break;
            }


        }
    };

    public void refresh(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtils okHttpUtils = new OkHttpUtils();
                do {
                    result = okHttpUtils.get(baseUrl+"get_chatrooms");
                } while (result.equals("error"));
                handler.sendMessage(handler.obtainMessage(1, result));
//                if(!result.equals("error")){
//                    handler.sendMessage(handler.obtainMessage(1, result));
//                }else if(result.equals("error")){
//                    handler.sendMessage(handler.obtainMessage(404));
//                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                return true;
            case R.id.refreshBtn:
                refresh();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openChatRoom(View view, String chatRoomId, String chatRoomName){
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("id", chatRoomId);
        intent.putExtra("name", chatRoomName);
        startActivity(intent);
    }
}
