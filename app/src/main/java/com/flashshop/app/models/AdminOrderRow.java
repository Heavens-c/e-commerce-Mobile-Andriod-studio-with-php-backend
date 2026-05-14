package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

public class AdminOrderRow {
    @SerializedName("id") public int id;
    @SerializedName("user_name") public String userName;
    @SerializedName("total_price") public double totalPrice;
    @SerializedName("status") public String status;
    @SerializedName("created_at") public String createdAt;
}
