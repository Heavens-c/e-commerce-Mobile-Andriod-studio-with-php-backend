package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

public class Banner {
    @SerializedName("id") public int id;
    @SerializedName("title") public String title;
    @SerializedName("image_url") public String imageUrl;
    @SerializedName("link_type") public String linkType;
    @SerializedName("link_value") public String linkValue;
}
