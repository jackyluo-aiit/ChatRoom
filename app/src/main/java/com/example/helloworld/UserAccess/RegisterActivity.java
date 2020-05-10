package com.example.helloworld.UserAccess;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.Map;
import java.util.Set;


public class RegisterActivity extends AppCompatActivity {
    private Button confirmBtn;
    private Button cancelBtn;
    private EditText emailEdit;
    private EditText enPwdEdit;
    private EditText conPwdEdit;
    private EditText nameEdit;
    private Boolean flag = true;
    private String baseUrl = "http://192.168.31.100:5000/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("IEMS5722");
        init();
    }

    private void init() {
        nameEdit = findViewById(R.id.edit_name);
        nameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (TextUtils.isEmpty(nameEdit.getText().toString())){
                        Toast.makeText(RegisterActivity.this, "Name can't be null!", Toast.LENGTH_SHORT).show();
                        flag = false;
                    }
                    else{
                        flag = true;
                    }
                    nameEdit.clearFocus();
                    InputMethodManager im =
                            (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(nameEdit.getWindowToken(), 0);
                }
                return false;
            }
        });
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
                        Toast.makeText(RegisterActivity.this, "Please input valid email address!", Toast.LENGTH_SHORT).show();
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
        enPwdEdit = findViewById(R.id.edit_setpassword);
        enPwdEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String s = v.getText().toString();
                    Log.i("log", " v: ****** v :" + s.length());
                    if (s.length() >= 6) {
                        Log.i("Log", " ****** s :" + s.length());
                        flag = true;
                        enPwdEdit.clearFocus();
                        InputMethodManager imm =
                                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(enPwdEdit.getWindowToken(), 0);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Password has at least 6 digits！", Toast.LENGTH_SHORT).show();
                        flag = false;
                    }
                }
                return false;
            }
        });
        conPwdEdit = findViewById(R.id.edit_resetpassword);
        conPwdEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!enPwdEdit.getText().toString().trim().equals(conPwdEdit.getText().toString().trim())) {
                        Toast.makeText(RegisterActivity.this, "Password doesn't match!", Toast.LENGTH_SHORT).show();
                        flag = false;
                    }else{
                        flag = true;
                    }
                    conPwdEdit.clearFocus();
                    InputMethodManager im =
                            (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(conPwdEdit.getWindowToken(), 0);
                }
                return false;
            }
        });
        confirmBtn = findViewById(R.id.btn_yes);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConfirmBtn();
            }
        });
        cancelBtn = findViewById(R.id.btn_cancle);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCancelBtn();
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

    public void setConfirmBtn() {
        if (!flag){
            return;
        }
        final String url = "auth/register";
        String name = nameEdit.getText().toString();
        String email = emailEdit.getText().toString();
        String password = enPwdEdit.getText().toString();
        String mark = "captcha";
        final HashMap<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        params.put("password", password);
        params.put("mark", mark);
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
                            validateCaptcha(emailEdit.getText().toString());
                        } else if (resultCode.equals("ERROR")) {
                            Toast.makeText(RegisterActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public void setCancelBtn() {
    }

    public void validateCaptcha(String email){
        Intent intent = new Intent(this, CaptchaActivity.class);
        saveLoginStatus(email, enPwdEdit.getText().toString());
        startActivity(intent);
        RegisterActivity.this.finish();
    }

    private void saveLoginStatus(String email, String password){
        SharedPreferences sp = getSharedPreferences("registerInfo", Context.MODE_PRIVATE);
        sp.edit().putString("email", email).putString("password", password).apply();
    }
}
