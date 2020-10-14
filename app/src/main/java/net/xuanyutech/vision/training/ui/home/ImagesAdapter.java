package net.xuanyutech.vision.training.ui.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import net.xuanyutech.vision.R;
import net.xuanyutech.vision.Utils;
import net.xuanyutech.vision.search.ImageDownloadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.Viewholder> {


    public static int id=1;
    public interface OnItemClickListener {
        void onItemClick(View view, JSONObject name) throws JSONException;
        void onItemLongClick(View view,JSONObject name,int pos)throws JSONException;
    }
    private JSONArray jsonarray;
    private Context context;
    private View.OnClickListener listener;
    private OnItemClickListener itemListener;
    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.img_item, parent, false);
        view.setOnClickListener(listener);
        return new Viewholder(view);
    }

    static class Viewholder extends RecyclerView.ViewHolder {
        ImageView imgview;
        TextView name;
        View holder;
        LinearLayout rootview;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            rootview = (LinearLayout) itemView;
            imgview = itemView.findViewById(R.id.img);
            holder=itemView;
        }
    }

    public ImagesAdapter(Context context,JSONArray jsonarray,OnItemClickListener itemListener){
        this.context = context;
        this.jsonarray = jsonarray;
        this.itemListener = itemListener;
    }

    public class CropSquareTransformation implements Transformation {
        ImageView imageView;
        public CropSquareTransformation(ImageView w) {
            imageView=w;
        }

        @Override public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int width = source.getWidth();
            int height = source.getHeight();
            int maxWidth = ((LinearLayout)imageView.getParent()).getWidth();
            int maxHeight = ((LinearLayout)imageView.getParent()).getWidth();
            float scale = (maxHeight * height )/width;

            Bitmap t = Bitmap.createScaledBitmap(source,maxWidth,Integer.valueOf((int) (scale)),false);
//            Bitmap result = Bitmap.createBitmap(t,0,t.getHeight()/2-maxWidth/2,maxWidth,maxWidth);
            Bitmap bmBackground = Bitmap.createBitmap(maxWidth,maxHeight, Bitmap.Config.ARGB_8888);
            bmBackground.eraseColor(Color.BLACK);
            int top_shift = 0;
            int start_shift = 0;
            int square = Math.min(width,height);
            if(width>height){
                int e = source.getHeight();
                start_shift = (source.getWidth() - e )/2;

            }
            else {
                int e = source.getWidth();
                top_shift = (source.getHeight() - e )/2;
            }
            Bitmap temp = Bitmap.createBitmap(source,start_shift,top_shift,square,square);
            Bitmap result = Bitmap.createScaledBitmap(temp,maxWidth,maxWidth,false);
            int shift = (maxHeight - t.getHeight())/2;
            new Canvas(bmBackground).drawBitmap(t, 0,shift , new Paint());
            if (result != source) {
                source.recycle();
            }
            return result;
        }
        @Override public String key() { return "square()"; }
    }


    public void delete_img(int pos){
        this.jsonarray.remove(pos);
    }
    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        try {
            final JSONObject album = (JSONObject) jsonarray.get(position);
            String url = Utils.makeUrl(context,album.getString("url"));

//            holder.rootview.setOnLongClickListener(view -> {
//                itemListener.onItemLongClick(view,album);
//            });
//
            holder.imgview.setOnLongClickListener(view -> {
                try {
                    itemListener.onItemLongClick(view,album,position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            });
            holder.imgview.setOnClickListener(view -> {
                try {
                    itemListener.onItemClick(view,album);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            Picasso.get()
                    .load(url)
                    .transform(new CropSquareTransformation(holder.imgview))
                    .into(holder.imgview);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public int getItemCount() {
        return jsonarray.length();
    }
}
