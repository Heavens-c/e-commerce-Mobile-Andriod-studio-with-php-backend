package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    @SerializedName("id") public int id;
    @SerializedName("category_id") public int categoryId;
    @SerializedName("name") public String name;
    @SerializedName("description") public String description;
    @SerializedName("price") public double price;
    @SerializedName("original_price") public Double originalPrice;
    @SerializedName("discount_percent") public int discountPercent;
    @SerializedName("image_url") public String imageUrl;
    @SerializedName("images") public List<String> images;
    @SerializedName("stock") public int stock;
    @SerializedName("sold_count") public int soldCount;
    @SerializedName("rating") public float rating;
    @SerializedName("review_count") public int reviewCount;
    @SerializedName("brand") public String brand;
    @SerializedName("tags") public String tags;
    @SerializedName("variations") public List<String> variations;
    @SerializedName("is_featured") public boolean isFeatured;
    @SerializedName("is_flash_sale") public boolean isFlashSale;
    @SerializedName("is_active") public boolean isActive;
    @SerializedName("category_name") public String categoryName;

    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }

    public String getFormattedOriginalPrice() {
        return originalPrice != null ? String.format("$%.2f", originalPrice) : "";
    }

    public String getFormattedSoldCount() {
        if (soldCount >= 1000) return String.format("%.1fk sold", soldCount / 1000.0);
        return soldCount + " sold";
    }
}
