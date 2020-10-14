package net.xuanyutech.vision.models;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.xuanyutech.vision.ErrorAdapter;
import net.xuanyutech.vision.R;
import net.xuanyutech.vision.Utils;
import net.xuanyutech.vision.settings.PreferenceUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ModelActivity extends AppCompatActivity implements Callback, SwipeRefreshLayout.OnRefreshListener, ModelAdapter.OnItemClickListener {

    private RecyclerView modelListView;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.model_list_view);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        refreshLayout = findViewById(R.id.model_list_refresh_view);
        refreshLayout.setOnRefreshListener(this);
        modelListView = findViewById(R.id.model_list_view);
        modelListView.setLayoutManager(new LinearLayoutManager(this));
        modelListView.setAdapter(new ModelAdapter(new ArrayList<>(), this));
        queryModelList();
    }

    private void queryModelList() {
        refreshLayout.setRefreshing(true);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Utils.makeUrl(this, "/models")).get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        runOnUiThread(() -> {
            refreshLayout.setRefreshing(false);
            modelListView.setAdapter(new ErrorAdapter(getString(R.string.offline), view -> queryModelList()));
        });
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        Type listType = new TypeToken<List<Model>>() {
        }.getType();
        Gson gson = new Gson();
        List<Model> models = gson.fromJson(response.body().string(), listType);
        runOnUiThread(() -> {
            if (models.size() == 0) {
                ErrorAdapter errorAdapter = new ErrorAdapter(getString(R.string.empty_list), null);
                errorAdapter.setImage(R.drawable.ic_baseline_inbox_24);
                modelListView.setAdapter(errorAdapter);
            } else {
                modelListView.setAdapter(new ModelAdapter(models, this));
            }
            refreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onRefresh() {
        queryModelList();
    }

    @Override
    public void onItemClick(View view, String name) {
        PreferenceUtils.saveStringPreference(this, R.string.pref_key_pref_model, name);
        finish();
    }

    @Override
    public void onExpand(View view, int position) {
        modelListView.getAdapter().notifyItemChanged(position);
    }

    @Override
    public void onCollapse(View view, int position) {
        modelListView.getAdapter().notifyItemChanged(position);
    }



    @Override
    public void onItemDelete(View view, String name, int pos) {

    }
}
