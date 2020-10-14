package net.xuanyutech.vision.training.ui.notifications;

import android.view.View;

import net.xuanyutech.vision.models.Model;
import net.xuanyutech.vision.models.ModelAdapter;

import java.util.List;

public class TrainedModelAdapter extends ModelAdapter {
    public TrainedModelAdapter(List<Model> modelList, OnItemClickListener listener) {
        super(modelList, listener);
    }

    interface OnButtomClickListener{
        void OnDeleteButtomClicked(View view, String name);
    }


}
