package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Stack;

public class ChatRoomActivity extends AppCompatActivity {
    private Socket socket;
    private ListView listView;
    private EditText editText;
    private ChatArrayAdapter chatArrayAdapter;
    private ImageButton sendBtn;
    private String result;
    private String chatRoomId;
    private String chatRoomName;
    private Integer current_page;
    private Integer total_pages;
    private Boolean loadMore = false;
    private FetchChats fetchChats;
    private FetchHistorys fetchHistorys;
    private String baseUrl = "http://192.168.31.26:5000/api/a3/";
    private String baseWsUrl = "http://192.168.31.26:8001/";
    private JSONObject json = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //return to the main page

        Intent data = getIntent();
        chatRoomId = data.getStringExtra("id");
        chatRoomName = data.getStringExtra("name");

        try {
            Log.i("log", "Websocket connecting");
            socket = IO.socket(baseWsUrl);
            socket.on(Socket.EVENT_CONNECT, onConnectSuccess);
            socket.on("status", onJoinLeave);
            socket.on("room_message", onMessageRecieve);
            socket.connect();
            json.put("chatroom_id", chatRoomId);
            socket.emit("join", json);
        } catch (URISyntaxException | JSONException e) {
            e.printStackTrace();
        }

        setTitle(chatRoomName);
        current_page = 1;
        fetchChats = new FetchChats();
        fetchChats.execute(current_page, Integer.valueOf(chatRoomId));
//        refresh(current_page, 0);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                OkHttpUtils okHttpUtils = new OkHttpUtils();
//                HashMap<String, Object> params = new HashMap<>();
//                params.put("chatroom_id", chatRoomId);
//                params.put("page", "1");
//                result = okHttpUtils.get("http://18.220.14.97/api/a2/get_messages", params);
//                if (!result.equals("error")) {
//                    handler.sendMessage(handler.obtainMessage(1, result));
//                }else if (result.equals("error")){
//                    handler.sendMessage(handler.obtainMessage(404));
//                }
//            }
//        }).start();

//        refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
        listView = (ListView) findViewById(R.id.messageListView);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.message_send);
        listView.setAdapter(chatArrayAdapter);
//        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
//            @Override
//            public void onRefresh() {
//                current_page+=1;
//                if(current_page<=total_pages){
//                    refresh(current_page);
//                }
//                refreshableView.finishRefreshing();
//            }
//        }, 0);

        editText = (EditText) findViewById(R.id.editText);
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendMessage();
                }
                return false;
            }
        });

        sendBtn = (ImageButton) findViewById(R.id.imageButtonSend);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

//        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);  //automatically scroll the listview to show the latest one.

//        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
//            @Override
//            public void onChanged() {
//                super.onChanged();
//                listView.setSelection(chatArrayAdapter.getCount() - 1); //let the latest message to be put on the top (bottom) of the listview
//            }
//        });
        fetchHistorys = new FetchHistorys();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            boolean isTopRow = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (listView.getFirstVisiblePosition() == 0 && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && !fetchHistorys.getStatus().toString().equals("RUNNING")) {
                    if (fetchHistorys.getStatus().toString().equals("FINISHED")) {
                        fetchHistorys.cancel(true);
                        fetchHistorys = new FetchHistorys();
                    }
                    loadMore = false;
                    current_page += 1;
                    if (current_page <= total_pages) {
                        fetchHistorys.execute(current_page, Integer.valueOf(chatRoomId));
                    } else {
                        Toast.makeText(ChatRoomActivity.this, "Reach the end", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("log", "Websocket connected");
                }
            });
        }
    };

    private Emitter.Listener onJoinLeave = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject result = (JSONObject) args[0];
            try {
                final String status = result.getString("status");
                final String room = result.getString("room");
                final StringBuilder sb = new StringBuilder(status.equals("in") ? "Enter room :" : "Leave room :");
                sb.append(room.equals(chatRoomId) ? chatRoomName : "error");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("log", sb.toString());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onMessageRecieve = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject result = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject message = result.getJSONObject("message");
                        Msg recieve = MessageUtils.transferMsg(message);
                        Log.i("log", "Recieve: " + recieve);
                        if (recieve.getFlag().equals("send")) {
                            return;
                        }
                        chatArrayAdapter.add(recieve);
                        chatArrayAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fetchChats.cancel(true);
        fetchHistorys.cancel(true);
        if (socket != null) {
            try {
                json.put("chatroom_id", chatRoomId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("leave", json);
            socket.disconnect();
            socket.off();
        }
    }

    //    public void refresh(final Integer current_page, final Integer flag) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                OkHttpUtils okHttpUtils = new OkHttpUtils();
//                HashMap<String, Object> params = new HashMap<>();
//                params.put("chatroom_id", 2);
//                params.put("page", current_page);
//                do {
//                    result = okHttpUtils.get("http://18.220.14.97/api/a2/get_messages", params);
//                } while (result.equals("error"));
//
//                if (flag == 0) handler.sendMessage(handler.obtainMessage(0, result));
//                if (flag == 1) handler.sendMessage(handler.obtainMessage(1, result));
////                }else if(result.equals("error")){
////                    handler.sendMessage(handler.obtainMessage(404));
////                }
//            }
//        }).start();
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chatroom, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.refreshBtn:
                current_page = 1;
                if (!fetchChats.getStatus().toString().equals("RUNNING")) {
                    if (fetchChats.getStatus().toString().equals("FINISHED")) {
                        fetchChats.cancel(true);
                        fetchChats = new FetchChats();
                        fetchChats.execute(current_page, Integer.valueOf(chatRoomId));
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }


    public boolean sendMessage() {
        String message = editText.getText().toString();
        if (message.length() > 200) {
            Toast.makeText(ChatRoomActivity.this, "Too long sequence", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (!message.equals("")) {
            Msg msg = new Msg(message, "send");
            chatArrayAdapter.add(msg);
            SendMessage sendMessage = new SendMessage();
            sendMessage.execute(msg);
        }
        editText.setText("");

        chatArrayAdapter.notifyDataSetChanged();
        listView.setSelection(chatArrayAdapter.getCount()-1);
//        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
//            @Override
//            public void onChanged() {
//                super.onChanged();
//                listView.setSelection(chatArrayAdapter.getCount() - 1); //let the latest message to be put on the top (bottom) of the listview
//            }
//        });
        return true;
    }

    private class SendMessage extends AsyncTask<Msg, Integer, String> {

        @Override
        protected String doInBackground(Msg... msgs) {
            String url = "send_message";
            HashMap<String, String> params = new HashMap<>();
            params.put("chatroom_id", chatRoomId);
            params.put("user_id", msgs[0].getId());
            params.put("name", msgs[0].getName());
            params.put("message", msgs[0].getMessage());
            OkHttpUtils okHttpUtils = new OkHttpUtils();
            do {
                result = okHttpUtils.post(baseUrl + url, params);
            } while (result.equals("error"));
            Log.i("log", "Send message: " + msgs[0].getMessage());
            return result;
        }
    }

//    public void receiveMessage(Stack<JSONObject> received, Integer flag) {
//        if(flag == 0) chatArrayAdapter.deleteItem();
//        while (!received.isEmpty()) {
//            JSONObject resultObject = null;
//            try {
//                resultObject = received.pop();
//                String message = resultObject.getString("message");
//                String name = resultObject.getString("name");
//                String message_time = resultObject.getString("message_time");
//                chatArrayAdapter.add(new Msg(message, message_time, name, "received"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    public Stack<JSONObject> parseJson(String content) {
//        /*
//        {"data":[{"id":2,"name":"General Chatroom"},{"id":3,"name":"Chatroom 2"},{"id":4,"name":"Catroom 3"}],"status":"OK"}
//         */
//        Stack<JSONObject> list = new Stack<>();
//        if (content != null) {
//            try {
//                JSONObject jsonObject = new JSONObject(content);
//                String resultCode = jsonObject.getString("status");
//                if (resultCode.equals("OK")) {
//                    JSONObject data = jsonObject.getJSONObject("data");
//                    current_page = data.getInt("current_page");
//                    total_pages = data.getInt("total_pages");
//                    JSONArray messages = data.getJSONArray("messages");
//                    int len = messages.length();
//
//                    for (int i = 0; i < len; i++) {
//                        JSONObject resultObject = messages.getJSONObject(i);
//                        String message = resultObject.getString("message");
//                        String name = resultObject.getString("name");
//                        String message_time = resultObject.getString("message_time");
//                        System.out.println("message: " + message + ", name: " + name + " message_time: " + message_time);
//                        list.push(resultObject);
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return list;
//    }


//    @SuppressLint("HandlerLeak")
//    public Handler handler = new Handler() {
//
//
//        @Override
//        public void handleMessage(Message msg) {
//            String content;
//            switch (msg.what) {
//                case 0:
//                    content = (String) msg.obj;
//                    receiveMessage(parseJson(content), 0);
//                    break;
//                case 1:
//                    content = (String) msg.obj;
//                    receiveMessage(parseJson(content), 1);
//                    break;
////                case 404:
////                    refresh(current_page, 404);
//            }


//        }
//    };

    private class FetchChats extends AsyncTask<Integer, Integer, String> {
        private String url = "get_messages";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... current_page) {
            if (!isCancelled()) {
                if (current_page[0] == 1) chatArrayAdapter.deleteItem();
                OkHttpUtils okHttpUtils = new OkHttpUtils();
                HashMap<String, Object> params = new HashMap<>();
                params.put("chatroom_id", current_page[1]);
                params.put("page", current_page[0]);
                do {
                    result = okHttpUtils.get(baseUrl + url, params);
                } while (result.equals("error"));
                return result;
            }
            return null;
        }

//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            Toast.makeText(ChatRoomActivity.this, "Please wait", Toast.LENGTH_SHORT).show();
//        }

        @Override
        protected void onPostExecute(String content) {
            Stack<JSONObject> list = new Stack<>();
            if (content != null) {
                try {
                    JSONObject jsonObject = new JSONObject(content);
                    String resultCode = jsonObject.getString("status");
                    if (resultCode.equals("OK")) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        current_page = data.getInt("current_page");
                        total_pages = data.getInt("total_pages");
                        JSONArray messages = data.getJSONArray("messages");
                        int len = messages.length();

                        for (int i = 0; i < len; i++) {
                            JSONObject resultObject = messages.getJSONObject(i);
                            list.push(resultObject);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            while (!list.isEmpty()) {
                JSONObject resultObject = null;
                resultObject = list.pop();
                chatArrayAdapter.add(MessageUtils.transferMsg(resultObject));
            }
            chatArrayAdapter.notifyDataSetChanged();
            loadMore = true;
        }
    }

    private class FetchHistorys extends AsyncTask<Integer, Integer, String> {
        private String url = "get_messages";

        @Override
        protected String doInBackground(Integer... integers) {
            if (!isCancelled()) {
                OkHttpUtils okHttpUtils = new OkHttpUtils();
                HashMap<String, Object> params = new HashMap<>();
                params.put("chatroom_id", integers[1]);
                params.put("page", integers[0]);
                do {
                    result = okHttpUtils.get(baseUrl + url, params);
                } while (result.equals("error"));
                return result;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String content) {
            if (content != null) {
                try {
                    JSONObject jsonObject = new JSONObject(content);
                    String resultCode = jsonObject.getString("status");
                    if (resultCode.equals("OK")) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        current_page = data.getInt("current_page");
                        total_pages = data.getInt("total_pages");
                        JSONArray messages = data.getJSONArray("messages");
                        int len = messages.length();

                        for (int i = 0; i < len; i++) {
                            JSONObject resultObject = messages.getJSONObject(i);
                            chatArrayAdapter.addHistory(MessageUtils.transferMsg(resultObject));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            chatArrayAdapter.notifyDataSetChanged();
            loadMore = true;
        }
    }
}


