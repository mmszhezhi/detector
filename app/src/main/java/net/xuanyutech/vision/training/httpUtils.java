package net.xuanyutech.vision.training;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class httpUtils {
    public static void get_JsonArray(String url){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url).get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String raw = response.body().string();
                try {
                    JSONArray result = new JSONArray(raw);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
