package net.xuanyutech.vision.training.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.xuanyutech.vision.ErrorAdapter;
import net.xuanyutech.vision.R;
import net.xuanyutech.vision.Utils;
import net.xuanyutech.vision.models.Model;
import net.xuanyutech.vision.models.ModelActivity;
import net.xuanyutech.vision.models.ModelAdapter;
import net.xuanyutech.vision.settings.PreferenceUtils;
import net.xuanyutech.vision.training.ui.home.AlbumAdapter;
import net.xuanyutech.vision.training.ui.home.HomeFragment;
import net.xuanyutech.vision.training.ui.home.ImagesAdapter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment implements TrainingProcessAdapter.OnItemClickListener,ModelAdapter.OnItemClickListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private RecyclerView models = null;
    private PageViewModel pageViewModel;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
        queryProcess();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.training_process_main, container, false);
        models = root.findViewById(R.id.list_process);
        models.setLayoutManager(new LinearLayoutManager(getContext()));


        SwipeRefreshLayout ly = root.findViewById(R.id.swiplayout1);
        ly.setProgressViewEndTarget(true, 100);
        ly.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ly.setRefreshing(false);
            }
        });

//        root.findViewById(R.id.c)
//        final TextView textView = root.findViewById(R.id.section_label);
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                switcher(s);
            }
        });
        return root;
    }
    public void switcher(String tab){
//        System.out.println("switch to " + String.valueOf(tab));
        if(tab.equals("1")){
            queryProcess();
        }else {
            queryModelList();
        }
    }


    private JSONArray remove100percent(JSONArray ja) throws JSONException {
        for(int i=0;i<ja.length();i++){
            JSONObject x = ja.getJSONObject(i);
            int p = x.getInt("percent");
            if(p==100){
                ja.remove(i);
                i--;
            }
        }
        System.out.println(ja);
        return ja;
    }

    private List<Model> getFinished(List<Model> ja) throws JSONException {
        for(int i=0;i<ja.size();i++){
            Model x = ja.get(i);
            if(x.percent!=100){
                ja.remove(i);
                i--;
            }
        }
        return ja;
    }


    private void queryProcess() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Utils.makeUrl(getContext(), "/models")).get()
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
                    JSONArray ja = new JSONArray(raw);
                    getActivity().runOnUiThread(() -> {
                        try {
                            models.setAdapter(new TrainingProcessAdapter(getContext(), remove100percent(ja),PlaceholderFragment.this));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void cancelProcess(View view,String name) {
        OkHttpClient client = new OkHttpClient();
//        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("name", name)
//                .build();
        Request request = new Request.Builder()
                .url(Utils.makeUrl(getContext(), "/training/" +name)).delete()
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
                    JSONObject jo = new JSONObject(raw);
                    if(jo.getInt("code")==0){
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), R.string.operation_success, Toast.LENGTH_SHORT).show();
                        });
                    }else {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void Cancel(View view, String name,int pos) throws JSONException {
        cancelProcess(view,name);
        ((TrainingProcessAdapter)models.getAdapter()).cancel_process(pos);
        models.getAdapter().notifyItemChanged(pos);
    }

    @Override
    public void Delete(View view, String name,int pos) throws JSONException {
        deleteModel(view,name);
        ((TrainingProcessAdapter)models.getAdapter()).removeModel(pos);
        models.getAdapter().notifyItemRemoved(pos);
    }

    private void queryModelList() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Utils.makeUrl(getContext(), "/models")).get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                models.setAdapter(new ErrorAdapter(getString(R.string.offline), view -> queryModelList()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Type listType = new TypeToken<List<Model>>() {
                }.getType();
                Gson gson = new Gson();
                List<Model> data = gson.fromJson(response.body().string(), listType);
                getActivity().runOnUiThread(() -> {
                    if (data.size() == 0) {
                        ErrorAdapter errorAdapter = new ErrorAdapter(getString(R.string.empty_list), null);
                        errorAdapter.setImage(R.drawable.ic_baseline_inbox_24);
                        models.setAdapter(errorAdapter);
                    } else {
                        try {
                            models.setAdapter(new ModelAdapter(getFinished(data),PlaceholderFragment.this));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onItemClick(View view, String name) {

    }

    @Override
    public void onExpand(View view, int position) {
        models.getAdapter().notifyItemChanged(position);
    }

    @Override
    public void onCollapse(View view, int position) {
        models.getAdapter().notifyItemChanged(position);
    }

    @Override
    public void onItemDelete(View view, String name, int pos) {
        deleteModel(view,name);
        ((ModelAdapter)models.getAdapter()).removeModel(pos);
        models.getAdapter().notifyItemRemoved(pos);
    }


    private void deleteModel(View view,String name) {
        OkHttpClient client = new OkHttpClient();
//        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("name", name)
//                .build();
        Request request = new Request.Builder()
                .url(Utils.makeUrl(getContext(), "/models/" +name)).delete()
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
                    JSONObject jo = new JSONObject(raw);
                    if(jo.getInt("code")==0){
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), R.string.operation_success, Toast.LENGTH_SHORT).show();
                        });
                    }else {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), R.string.operation_failed, Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {

        super.onResume();
    }
}