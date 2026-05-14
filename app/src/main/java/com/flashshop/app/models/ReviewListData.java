package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewListData {
    @SerializedName("reviews") public List<Review> reviews;
}

