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

package net.xuanyutech.vision;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

public class ErrorAdapter extends Adapter<ErrorAdapter.ErrorViewHolder> {

    static class ErrorViewHolder extends RecyclerView.ViewHolder {

        static ErrorViewHolder create(ViewGroup parent) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.error_page, parent, false);
            return new ErrorViewHolder(view);
        }

        private final ImageView imageView;
        private final TextView msgView;
        private final TextView retryView;

        private ErrorViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.error_image);
            msgView = view.findViewById(R.id.err_msg);
            retryView = view.findViewById(R.id.retry_txt);
        }

        void bind(int position, String txt, @DrawableRes int image, View.OnClickListener listener) {
            msgView.setText(txt);
            imageView.setImageResource(image);
            if (listener != null) {
                retryView.setVisibility(View.VISIBLE);
                retryView.setOnClickListener(listener);
            } else {
                retryView.setVisibility(View.GONE);
            }
        }
    }

    private View.OnClickListener listener;
    private String msg;
    private @DrawableRes int image;

    public ErrorAdapter(String text, View.OnClickListener listener) {
        this.msg = text;
        this.listener = listener;
        this.image = R.drawable.ic_baseline_cloud_off_24;
    }

    public void setImage(int image) {
        this.image = image;
    }

    @Override
    @NonNull
    public ErrorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ErrorViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ErrorViewHolder holder, int position) {
        holder.bind(position, msg, this.image, listener);
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
