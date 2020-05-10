package com.example.helloworld.UserAccess;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.helloworld.OkHttpUtils;
import com.example.helloworld.R;
import com.example.helloworld.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class CaptchaActivity extends AppCompatActivity {
    private Button confirmBtn;
    private EditText captchaEdit;
    private String baseUrl = "http://192.168.31.100:5000/";
    private String email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captcha);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("IEMS5722");
        init();
        SharedPreferences sp = getSharedPreferences("registerInfo", Context.MODE_PRIVATE);
        email = sp.getString("email", null);

    }

    private void init(){
        confirmBtn = findViewById(R.id.btn_yes);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateCaptcha();
            }
        });

        captchaEdit = findViewById(R.id.edit_captcha);
        captchaEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    captchaEdit.clearFocus();
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(captchaEdit.getWindowToken(), 0);
                }
                return false;
            }
        });
    }

    public void validateCaptcha(){
        final String url = "auth/validate_captcha";
        String captcha = captchaEdit.getText().toString();
        final HashMap<String, String> params = new HashMap<>();
        params.put("captcha", captcha);
        params.put("email", email);
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtils okHttpUtils = new OkHttpUtils();
                String result;
                do {
                    result = okHttpUtils.post(baseUrl + url, params);
                } while (result.equals("error"));
                handler.sendMessage(handler.obtainMessage(1, result));
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String content = (String) msg.obj;
                if (content != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(content);
                        String resultCode = jsonObject.getString("status");
                        if (resultCode.equals("OK")) {
                            SharedPreferences sp = getSharedPreferences("registerInfo", Context.MODE_PRIVATE);
                            String password = sp.getString("password", null);
                            login(email, password);
                        } else if (resultCode.equals("ERROR")) {
                            Toast.makeText(CaptchaActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    };

    public void login(String email, String password){
        Intent intent = new Intent(this, LoginActivity.class);
        saveLoginStatus(email, password);
        startActivity(intent);
        Utils.clear(CaptchaActivity.this, "registerInfo");
        CaptchaActivity.this.finish();
    }

    private void saveLoginStatus(String email, String password){
        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        sp.edit().putString("email", email).putString("password", password).apply();
    }
}
