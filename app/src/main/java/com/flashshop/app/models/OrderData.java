package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

public class OrderData {
    @SerializedName("order_id") public int orderId;
    @SerializedName("order_number") public String orderNumber;
    @SerializedName("total") public double total;
    @SerializedName("payment_method") public String paymentMethod;
    @SerializedName("status") public String status;
}

