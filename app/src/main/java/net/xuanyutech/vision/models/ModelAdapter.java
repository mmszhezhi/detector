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

package net.xuanyutech.vision.models;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import net.xuanyutech.vision.R;
import net.xuanyutech.vision.search.ImageDownloadTask;
import net.xuanyutech.vision.settings.PreferenceUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Presents the list of model items from cloud model search. */
public class ModelAdapter extends Adapter<ModelAdapter.ModelViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, String name);
        void onExpand(View view, int position);
        void onCollapse(View view, int position);
        void onItemDelete(View view,String name, int pos);
    }

    class ModelItem {
        Model model;
        boolean expand;
    }

    static class ModelViewHolder extends RecyclerView.ViewHolder {

        static ModelViewHolder create(ViewGroup parent) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.model_item, parent, false);
            return new ModelViewHolder(view);
        }

        private final MaterialCardView rootView;
        private final ImageView imageView;
        private final TextView titleView;
        private final TextView timeView;
        private final TextView accuracyView;
        private final TextView descView;
        private final TextView longDescView;
        private final ChipGroup classesView;
        private final ImageButton expandBtn;
        private final View detailView;
        private final MaterialButton deleteView;

        private ModelViewHolder(View view) {
            super(view);
            rootView = (MaterialCardView) view;
            imageView = view.findViewById(R.id.model_image);
            titleView = view.findViewById(R.id.model_title);
            timeView = view.findViewById(R.id.model_create_time);
            accuracyView = view.findViewById(R.id.model_accuracy);
            descView = view.findViewById(R.id.model_description);
            longDescView = view.findViewById(R.id.model_long_description);
            classesView = view.findViewById(R.id.model_classes);
            detailView = view.findViewById(R.id.model_detail);
            expandBtn = view.findViewById(R.id.model_item_btn_expand);
            deleteView = view.findViewById(R.id.delete_button);
        }

        void bindModel(ModelItem model, OnItemClickListener listener,Boolean isVisible) {
            ((LinearLayout)deleteView.getParent()).setVisibility(isVisible?View.VISIBLE:View.GONE);
            imageView.setImageDrawable(null);
            if (!TextUtils.isEmpty(model.model.imageUrl)) {
                new ImageDownloadTask(imageView, 1200).execute(model.model.imageUrl);
            } else {
                imageView.setVisibility(View.GONE);
            }
            titleView.setText(model.model.name);
            try {
                Date createTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS").parse(model.model.createTime);
                if (createTime != null) {
                    timeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createTime));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                timeView.setText("");
            }
            accuracyView.setText(String.valueOf(model.model.accuracy));
            descView.setText(model.model.brief);
            longDescView.setText(model.model.detail);
            classesView.removeAllViews();
            for (String cls : model.model.classes.split(",")) {
                Chip chip = new Chip(classesView.getContext());
                chip.setText(cls);
                classesView.addView(chip);
            }
            deleteView.setOnClickListener(view -> listener.onItemDelete(view,titleView.getText().toString(),getAdapterPosition()));
            rootView.setOnClickListener(view -> listener.onItemClick(view, titleView.getText().toString()));
            rootView.setChecked(model.model.name.equals(PreferenceUtils.getUsingModel(rootView.getContext())));
            if (model.expand)
                expandBtn.setImageResource(R.drawable.ic_baseline_expand_more_24);
            else
                expandBtn.setImageResource(R.drawable.ic_baseline_expand_less_24);
            expandBtn.setRotation(0);
            expandBtn.animate().rotation(model.expand ? 180 : -180).setDuration(300).start();
            detailView.setVisibility(model.expand ? View.VISIBLE : View.GONE);
            expandBtn.setOnClickListener(btn -> {
                model.expand = !model.expand;
                if (model.expand) {
                    listener.onExpand(detailView, getAdapterPosition());
                } else {
                    listener.onCollapse(detailView, getAdapterPosition());
                }
            });
        }
    }

    private final List<ModelItem> modelList;
    private OnItemClickListener listener;
    private Boolean isVisible;

    public ModelAdapter(List<Model> modelList, OnItemClickListener listener){
        this(modelList, listener, true);
    }

    public ModelAdapter(List<Model> modelList, OnItemClickListener listener,Boolean isVisible) {
        this.modelList = new ArrayList<>();
        for (Model model : modelList) {
            ModelItem item = new ModelItem();
            item.model = model;
            this.modelList.add(item);
        }
        this.isVisible = isVisible;
        this.listener = listener;
    }

    public void removeModel(int pos) {
        modelList.remove(pos);
    }

    @Override
    @NonNull
    public ModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ModelViewHolder.create(parent);
    }
    @Override
    public void onBindViewHolder(@NonNull ModelViewHolder holder, int position) {
        holder.bindModel(modelList.get(position), listener,isVisible);
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
