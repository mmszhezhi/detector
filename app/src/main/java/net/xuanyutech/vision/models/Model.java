package net.xuanyutech.vision.models;

import com.google.gson.annotations.SerializedName;

public class Model {
    String name;
    @SerializedName("image_url")
    String imageUrl;
    @SerializedName("create_time")
    String createTime;
    @SerializedName("finish_time")
    String finishTime;
    String classes;
    int status;
    String brief;
    String detail;
    double accuracy;
    public int percent;
    boolean cancel;
}
