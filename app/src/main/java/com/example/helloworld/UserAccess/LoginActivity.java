package com.example.helloworld.UserAccess;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.helloworld.MainActivity;
import com.example.helloworld.OkHttpUtils;
import com.example.helloworld.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private Button loginBtn;
    private Button registerBtn;
    private EditText emailEdit;
    private EditText pwdEdit;
    private ImageButton openpwd;
    private Boolean flag = true;
    private String baseUrl = "http://192.168.31.100:5000/";
    private String email;
    private String password;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("IEMS5722");
        SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
        email = sp.getString("email", null);
        password = sp.getString("password", null);
        Log.i("log", email+password);
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            login(email, password);
        }
        init();
    }

    private void init(){
        emailEdit = findViewById(R.id.edit_email);
        emailEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String email = emailEdit.getText().toString();
                    Log.i("log", "Email:" + email);
                    if (isEmail(email)) {
                        Log.i("log", "Email checking passed");
                        flag = true;
                    } else {
                        Toast.makeText(LoginActivity.this, "Please input valid email address!", Toast.LENGTH_SHORT).show();
                        flag = false;
                    }
                    emailEdit.clearFocus();
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(emailEdit.getWindowToken(), 0);
                }
                return false;
            }
        });
        pwdEdit = findViewById(R.id.edit_password);
        pwdEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String s = v.getText().toString();
                    Log.i("log", " v: ****** v :" + s.length());
                    if (s.length() >= 6) {
                        Log.i("Log", " ****** s :" + s.length());
                        flag = true;
                        pwdEdit.clearFocus();
                        InputMethodManager imm =
                                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(pwdEdit.getWindowToken(), 0);
                    } else {
                        Toast.makeText(LoginActivity.this, "Password has at least 6 digits！", Toast.LENGTH_SHORT).show();
                        flag = false;
                    }
                }
                return false;
            }
        });
        loginBtn = findViewById(R.id.btn_login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin();
            }
        });
        registerBtn = findViewById(R.id.btn_register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    public static boolean isEmail(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }

    public void onLogin(){
        if (!flag){
            return;
        }
        email = emailEdit.getText().toString();
        password = pwdEdit.getText().toString();
        login(email, password);
    }

    public void login(String email, String password){
        final String url = "auth/login";
        final HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
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
                            JSONObject resultJsonObject = jsonObject.getJSONObject("data");
                            String email = resultJsonObject.getString("email");
                            String name = resultJsonObject.getString("name");
                            loginSuccess(email, name);
                        } else if (resultCode.equals("ERROR")) {
                            Toast.makeText(LoginActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public void loginSuccess(String email, String name){
        Intent intent = new Intent(this, MainActivity.class);
        saveLoginStatus(email, password);
        intent.putExtra("email", email);
        intent.putExtra("name", name);
        startActivity(intent);
        LoginActivity.this.finish();
    }

    public void register(){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void saveLoginStatus(String email, String password){
        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("email", email).putString("password", password).apply();
    }
}
