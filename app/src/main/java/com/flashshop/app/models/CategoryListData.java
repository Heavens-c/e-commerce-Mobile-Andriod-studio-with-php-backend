package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CategoryListData {
    @SerializedName("categories") public List<Category> categories;
}

