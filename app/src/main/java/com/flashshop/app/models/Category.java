package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("id") public int id;
    @SerializedName("name") public String name;
    @SerializedName("icon") public String icon;
    @SerializedName("color") public String color;
    @SerializedName("image_url") public String imageUrl;
}
