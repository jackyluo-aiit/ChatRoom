package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class ChatRoomActivity extends AppCompatActivity {
    private ListView listView;
    private EditText editText;
    private ChatArrayAdapter chatArrayAdapter;
    private ImageButton sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        setTitle("Chatroom");

        listView = (ListView) findViewById(R.id.messageListView);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.message_send);
        listView.setAdapter(chatArrayAdapter);

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
        sendBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);  //automatically scroll the listview to show the latest one.

        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1); //let the latest message to be put on the top (bottom) of the listview
            }
        });
    }


    public boolean sendMessage(){
        String message = editText.getText().toString();
        if(!message.equals("")) {
            chatArrayAdapter.add(new Msg(message));
        }
        editText.setText("");
        return true;
    }
}
