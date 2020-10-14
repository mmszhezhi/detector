package net.xuanyutech.vision.training.ui.notifications;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import net.xuanyutech.vision.R;
import net.xuanyutech.vision.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TrainingProcessAdapter extends RecyclerView.Adapter<TrainingProcessAdapter.Viewholder> {

    private JSONArray datals;
    public static int id=0;
    private Context context;
    public interface OnItemClickListener {
        void Cancel(View view, String name,int pos) throws JSONException;
        void Delete(View view,String name,int pos) throws JSONException;

    }
    private OnItemClickListener listener;
    public int getId(){
        return id;
    }

    @NonNull
    @Override
    public TrainingProcessAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.training_process_item, parent, false);
        Viewholder holder = new Viewholder(view);
        return holder;

    }

    static class Viewholder extends RecyclerView.ViewHolder {

        TextView name;
        TextView start_time;
        ProgressBar bar;
        MaterialButton button;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.process_name);
            start_time = itemView.findViewById(R.id.start_time);
            bar = itemView.findViewById(R.id.process);
            button = itemView.findViewById(R.id.cancel_button);
        }
    }

    public TrainingProcessAdapter(Context context, JSONArray als, OnItemClickListener listener){
        this.datals = als;
        this.context = context;
        this.listener = listener;

    }


    public void removeModel(int pos) {
        datals.remove(pos);
    }
    public void cancel_process(int pos) throws JSONException {
        JSONObject obj = datals.getJSONObject(pos);
        obj.put("cancel",true);
        datals.put(pos,obj);
        System.out.println(obj.toString() + "#############");
    }





    @Override
    public void onBindViewHolder(@NonNull TrainingProcessAdapter.Viewholder holder, int position) {
        try {
            JSONObject data = (JSONObject) datals.get(position);
            holder.start_time.setText(data.getString("create_time"));
            holder.name.setText(data.getString("name"));
            holder.bar.setProgress(data.getInt("percent"));
            if(data.getInt("percent") == 100){
                holder.button.setVisibility(View.INVISIBLE);
            }
            if(data.getBoolean("cancel") == true){
                holder.button.setText(R.string.cancel_button_canceled);
                holder.button.setOnClickListener(view -> {
                    try {
                        listener.Delete(view,data.getString("name"),position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }else {
                holder.button.setOnClickListener(view -> {
                    try {
                        listener.Cancel(view,data.getString("name"),position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return datals.length();
    }
}
