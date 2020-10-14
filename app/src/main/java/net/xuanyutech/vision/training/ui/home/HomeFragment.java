package net.xuanyutech.vision.training.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.xuanyutech.vision.R;
import net.xuanyutech.vision.Utils;
import net.xuanyutech.vision.annotation.DrawActivity;
import net.xuanyutech.vision.models.ModelAdapter;
import net.xuanyutech.vision.training.ModelInfoActivity;
import net.xuanyutech.vision.training.TrainingActivity;
import net.xuanyutech.vision.training.httpUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment implements Callback, View.OnClickListener ,ImagesAdapter.OnItemClickListener,AlbumAdapter.onItemClick{

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private HomeViewModel homeViewModel;
    private RecyclerView album;
    private String class_name;
    private JSONArray albums;
    public static final int PICK_IMAGE = 2;
    public static final int LABEL_IMG = 3;
    private int delete_image_pos = -1;
    private int delete_album_pos = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        album = root.findViewById(R.id.list_album);
        SwipeRefreshLayout ly = root.findViewById(R.id.swiplayout);
        ly.setProgressViewEndTarget(true, 100);
        ly.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(album.getAdapter().getClass() == AlbumAdapter.class){
                    queryAlbums();
                }else {
                    queryImages(class_name);
                }
                ly.setRefreshing(false);
            }
        });
        album.setLayoutManager(new GridLayoutManager(getContext(),3));
        queryAlbums();
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.cat_bottomsheet_scrollable_content);
        View bottomSheetInternal = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheetInternal).setPeekHeight(400);
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        TextView t1 = bottomSheetDialog.findViewById(R.id.take_picture);
        TextView t2 = bottomSheetDialog.findViewById(R.id.select_picture);
        TextView t3 = bottomSheetDialog.findViewById(R.id.new_model);
        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        t2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_picture();
            }
        });
        t3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ModelInfoActivity.class);
                try {
                    intent.putExtra("classes",jsonarray2stringarray(albums));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });
        fab.setOnClickListener(v -> bottomSheetDialog.show());
        return root;
    }

    public String[] jsonarray2stringarray(JSONArray ja) throws JSONException {
        String[] result = new String[ja.length()];
        for(int i=0;i<ja.length();i++){
            result[i] = ja.getJSONObject(i).getString("name");
        }
        return result;
    }

    public void queryAlbums() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Utils.makeUrl(getContext(), "/data")).get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(this);
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {

    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        String raw = response.body().string();
        try {
            albums = new JSONArray(raw);
            getActivity().runOnUiThread(() -> {
                album.setAdapter(new AlbumAdapter(getContext(), albums, HomeFragment.this,HomeFragment.this));
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        System.out.println("##################" + view.getId());
        TextView textView = view.findViewById(R.id.albums_title);
        String name = (String) textView.getText();
        class_name = name;
        queryImages(name);
        if (((TrainingActivity)getActivity()).getSupportActionBar() != null) {
            ((TrainingActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void refresh(){
        if(album.getAdapter().getClass() == AlbumAdapter.class){
            queryAlbums();
        }else {
            queryImages(class_name);
        }
    }

    private void queryImages(String name) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Utils.makeUrl(getContext(), "/data/"+name)).get()
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
                        album.setAdapter(new ImagesAdapter(getContext(), ja,HomeFragment.this));
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public int[] jsonarray2intarray(JSONArray array) throws JSONException {
        int[] result = new int[array.length()];
        for(int i =0;i<array.length();i++){
            result[i] = array.getInt(i);
        }
        return result;
    }

    public String[] get_classes() throws JSONException {
        String[] result = new String[albums.length()];
        for(int i =0;i<albums.length();i++){
            result[i] = albums.getJSONObject(i).getString("name");
        }
        return result;
    }

    @Override
    public void onItemClick(View view, JSONObject img) throws JSONException {

        Intent intent = new Intent(getContext(), DrawActivity.class);
        intent.putExtra("classes", get_classes());
        intent.putExtra("class", class_name);
        intent.putExtra("box", jsonarray2intarray(img.getJSONArray("box")));
        intent.putExtra("id",img.getString("id"));
        intent.putExtra("uri", Utils.makeUrl(getContext(),img.getString("url")));
        startActivityForResult(intent,LABEL_IMG);
    }


    private void deleteConfirm(JSONObject obj){

        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.tip_title);
        builder.setMessage(R.string.operation_comfirm); //设置内容
        builder.setIcon(R.drawable.ic_baseline_delete_forever_24);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                Toast.makeText(getContext(), "确认" + which, Toast.LENGTH_SHORT).show();
                try {
                    if(obj.getString("name")!=null){}
                    DeleteAlbum(obj.getString("name"));
                } catch (JSONException e) {
                    try {
                        DeleteImages(obj);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onItemLongClick(View view, JSONObject obj,int pos) throws JSONException {
        delete_image_pos = pos;
        deleteConfirm(obj);
    }

    private void DeleteImages(JSONObject obj) throws JSONException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Utils.makeUrl(getContext(), "/data/"+class_name+"/" + obj.getString("id"))).delete()
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
                    JSONObject ja = new JSONObject(raw);
                    if(ja.getInt("code")==0){
                        getActivity().runOnUiThread(() -> {
                            ((ImagesAdapter)album.getAdapter()).delete_img(delete_image_pos);
                            album.getAdapter().notifyItemRemoved(delete_image_pos);
                        });

                    }else {
                        Toast.makeText(getContext(), R.string.operation_failed, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    Uri photoURI;
    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/tmp.jpg");;
            // Continue only if the File was successfully created
            photoURI = FileProvider.getUriForFile(getContext(),
                    getActivity().getApplicationContext().getPackageName()+".fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void select_picture(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }


    public void enter(String uri){
        Intent intent = new Intent(getContext(), DrawActivity.class);
        try {
            intent.putExtra("classes", get_classes());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intent.putExtra("uri",uri);
        startActivityForResult(intent,LABEL_IMG);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String uri = null;
        if(resultCode !=  getActivity().RESULT_OK){
            return;
        }
        switch (requestCode){
            case REQUEST_IMAGE_CAPTURE:
                uri = photoURI.toString();
                enter(uri);
                break;
            case PICK_IMAGE:
                uri = data.getData().toString();
                enter(uri);
                break;
            case LABEL_IMG:
//                refresh();
                if(data==null){
                    break;
                }
                String name = data.getStringExtra("class");
                if(name!=null && name.equals(class_name)){
                    queryImages(name);
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void DeleteAlbum(String name) throws JSONException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Utils.makeUrl(getContext(), "/data/"+name)).delete()
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
                    JSONObject ja = new JSONObject(raw);
                    if(ja.getInt("code")==0){
                        ((AlbumAdapter)album.getAdapter()).delete_album(delete_album_pos);
                        getActivity().runOnUiThread(() -> {
                            album.getAdapter().notifyItemRemoved(delete_album_pos);
                        });
                    }else {
                        Toast.makeText(getContext(), R.string.operation_failed, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDeleteAlbum(JSONObject name,int pos) {
        delete_album_pos = pos;
        deleteConfirm(name);
    }
}