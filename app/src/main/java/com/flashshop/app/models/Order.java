package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Order {
    @SerializedName("id") public int id;
    @SerializedName("order_number") public String orderNumber;
    @SerializedName("total") public double total;
    @SerializedName("payment_method") public String paymentMethod;
    @SerializedName("payment_status") public String paymentStatus;
    @SerializedName("order_status") public String orderStatus;
    @SerializedName("created_at") public String createdAt;
    @SerializedName("items") public List<OrderItem> items;
}

