package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("cart_id") public int cartId;
    @SerializedName("product_id") public int productId;
    @SerializedName("name") public String name;
    @SerializedName("price") public double price;
    @SerializedName("original_price") public Double originalPrice;
    @SerializedName("image_url") public String imageUrl;
    @SerializedName("quantity") public int quantity;
    @SerializedName("variation") public String variation;
    @SerializedName("stock") public int stock;
    @SerializedName("brand") public String brand;
    @SerializedName("line_total") public double lineTotal;

    public String getFormattedPrice() { return String.format("$%.2f", price); }
    public String getFormattedLineTotal() { return String.format("$%.2f", lineTotal); }
}
