package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

public class SearchSuggestion {
    @SerializedName("id") public int id;
    @SerializedName("name") public String name;
    @SerializedName("image_url") public String imageUrl;
    @SerializedName("category_name") public String categoryName;
}
