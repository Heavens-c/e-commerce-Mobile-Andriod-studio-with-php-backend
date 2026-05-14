package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("id") public int id;
    @SerializedName("user_id") public int userId;
    @SerializedName("full_name") public String fullName;
    @SerializedName("avatar_url") public String avatarUrl;
    @SerializedName("rating") public int rating;
    @SerializedName("comment") public String comment;
    @SerializedName("created_at") public String createdAt;
}
