package com.example.helloworld;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpUtils {
    private OkHttpClient client =  new OkHttpClient();
    private MainActivity mainActivity;

    public void asyncGet(String url){
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(!response.isSuccessful()) throw new IOException("Unexpected code "+response);
                ResponseBody responseBody = response.body();
                String content = responseBody.toString();
            }
        });
    }

    public String get(String url){
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        String content = null;
        try {
            Log.i("log", "Loading...");
            response = client.newCall(request).execute();
            if(!response.isSuccessful()) throw new IOException("Unexpected code "+response);
            ResponseBody responseBody = response.body();
            content = responseBody.string();
            System.out.println(content);
        } catch (IOException e) {
            Log.e("log", "Retrying...");
            return "error";
        }
        return content;
    }

    public String get(String url, HashMap<String, Object> params){
        Request.Builder reqBuild = new Request.Builder();
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        Set<Map.Entry<String, Object>> entries = params.entrySet();
        for (Map.Entry<String, Object> entry:entries){
            builder.addQueryParameter(entry.getKey(), entry.getValue().toString());
        }
        reqBuild.url(builder.build());
        Request request = reqBuild.build();
        Response response = null;
        String content = null;
        System.out.println("Fetching..."+params.get("page"));
        try {
            response = client.newCall(request).execute();
            if(!response.isSuccessful()) throw new IOException("Unexpected code "+response);
            ResponseBody responseBody = response.body();
            content = responseBody.string();
            System.out.println(content);
        } catch (IOException e) {
            Log.e("log", "Retrying..."+params.get("page"));
            return "error";
        }
        return content;
    }

    public String post(String url, HashMap<String, String> params){
        String content;
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()){
            builder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        Request request = new Request.Builder()
                .url(builder.build())
                .method("post", new FormBody.Builder().build())
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if(!response.isSuccessful()) throw new IOException("Unexpected code "+response);
            ResponseBody responseBody = response.body();
            content = responseBody.string();
            System.out.println(content);
        } catch (IOException e) {
            Log.e("log", "Retry sending...");
            return "error";
        }
        return content;
    }

}
