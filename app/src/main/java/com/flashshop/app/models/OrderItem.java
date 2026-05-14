package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

public class OrderItem {
    @SerializedName("product_name") public String productName;
    @SerializedName("product_image") public String productImage;
    @SerializedName("variation") public String variation;
    @SerializedName("quantity") public int quantity;
    @SerializedName("unit_price") public double unitPrice;
    @SerializedName("total_price") public double totalPrice;
}

