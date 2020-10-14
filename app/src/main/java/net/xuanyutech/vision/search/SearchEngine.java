/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xuanyutech.vision.search;

import android.content.Context;

import net.xuanyutech.vision.Utils;
import net.xuanyutech.vision.objectdetection.DetectedObject;
import net.xuanyutech.vision.settings.PreferenceUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** A fake search engine to help simulate the complete work flow. */
public class SearchEngine {

  private static final String TAG = "SearchEngine";
  private Context context;

  public interface SearchResultListener {
    void onSearchCompleted(DetectedObject object, List<Product> productList, String errMsg);
  }

  public SearchEngine(Context context) {
      this.context = context;
  }

  public void search(DetectedObject object, SearchResultListener listener) {
      OkHttpClient client = new OkHttpClient();
      RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
              .addFormDataPart("file", "img.jpg", RequestBody.create(object.getImageData()))
              .addFormDataPart("model", PreferenceUtils.getUsingModel(context))
              .build();
      Request request = new Request.Builder()
              .url(Utils.makeUrl(context, "/detection"))
              .post(requestBody)
              .build();
      Call call = client.newCall(request);
      List<Product> productList = new ArrayList<>();
      call.enqueue(new Callback() {
          @Override
          public void onFailure(@NotNull Call call, @NotNull IOException e) {
              e.printStackTrace();
              listener.onSearchCompleted(object, null, e.getLocalizedMessage());
          }

          @Override
          public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
              try {
                  JSONObject resp = new JSONObject(response.body().string());
                  if (resp.getInt("code") != 0) {
                      listener.onSearchCompleted(object, productList, resp.getString("msg"));
                      return;
                  }
                  JSONArray Jarray = resp.getJSONArray("classes");
                  JSONArray scores = resp.getJSONArray("scores");
                  for (int i = 0; i < Jarray.length(); i++) {
                      String obj = Jarray.getString(i);
                      productList.add(
                              new Product(Utils.makeUrl(context,  "/static/images/" + obj + ".jpg"), obj, String.valueOf(scores.getDouble(i))));
                  }
                  listener.onSearchCompleted(object, productList, "");
              } catch (JSONException ex) {
                  ex.printStackTrace();
                  listener.onSearchCompleted(object, productList, "");
              }
          }
      });
  }
}
