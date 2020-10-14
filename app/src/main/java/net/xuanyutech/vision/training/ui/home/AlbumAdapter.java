package net.xuanyutech.vision.training.ui.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import net.xuanyutech.vision.R;
import net.xuanyutech.vision.Utils;
import net.xuanyutech.vision.models.ModelAdapter;
import net.xuanyutech.vision.search.ImageDownloadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewholder> {

    private JSONArray als;
    public static int id=0;
    private Context context;
    private View.OnClickListener listener;
    private onItemClick itemListener;

    public int getId(){
        return id;
    }

    @NonNull
    @Override
    public AlbumAdapter.AlbumViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.albums_view, parent, false);
        AlbumViewholder holder = new AlbumViewholder(view);
        view.setOnClickListener(listener);
        return holder;

    }

    public interface onItemClick{
        void  onDeleteAlbum(JSONObject name, int pos);
    }

    static class AlbumViewholder extends RecyclerView.ViewHolder {
        ImageView imgview;
        TextView name;
        TextView num;
        MaterialCardView rootview;
        public AlbumViewholder(@NonNull View itemView) {
            super(itemView);
            rootview = (MaterialCardView) itemView;
            imgview = itemView.findViewById(R.id.preview_image);
            name = itemView.findViewById(R.id.albums_title);
            num = itemView.findViewById(R.id.albums_subtitle);
        }
    }

    public AlbumAdapter(Context context, JSONArray als, View.OnClickListener listener,onItemClick itemListener){
        this.als = als;
        this.context = context;
        this.listener = listener;
        this.itemListener = itemListener;

    }

    public static Bitmap getHttpBitmap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setConnectTimeout(0);
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }



    public void delete_album(int pos){
        this.als.remove(pos);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapter.AlbumViewholder holder, int position) {
        try {
            JSONObject album = (JSONObject) als.get(position);
            String url = Utils.makeUrl(context,"/static/thumbnails/"+album.getString("name") +".jpg");
            holder.rootview.setOnLongClickListener(view -> {
                itemListener.onDeleteAlbum(album,position);
                return true;
            });
//            new ImageDownloadTask(holder.imgview, 1000).execute(url);
            Picasso.get()
                    .load(url).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24)
                    .into(holder.imgview);
            holder.name.setText(album.getString("name"));
            holder.num.setText(album.getString("count"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return als.length();
    }
}
