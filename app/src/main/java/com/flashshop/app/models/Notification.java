package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Notification {
    @SerializedName("id") public int id;
    @SerializedName("title") public String title;
    @SerializedName("message") public String message;
    @SerializedName("type") public String type;
    @SerializedName("is_read") public boolean isRead;
    @SerializedName("created_at") public String createdAt;
}

