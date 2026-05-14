package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CartData {
    @SerializedName("items") public List<CartItem> items;
    @SerializedName("item_count") public int itemCount;
    @SerializedName("subtotal") public double subtotal;
}

