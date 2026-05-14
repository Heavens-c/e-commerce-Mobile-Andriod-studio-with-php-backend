package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WishlistData {
    @SerializedName("items") public List<Product> items;
    @SerializedName("wishlisted") public Boolean wishlisted;
}

