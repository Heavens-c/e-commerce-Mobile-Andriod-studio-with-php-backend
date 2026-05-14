package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

public class AdminStatsData {
    @SerializedName("total_orders") public int totalOrders;
    @SerializedName("total_sales") public double totalSales;
    @SerializedName("total_products") public int totalProducts;
    @SerializedName("total_customers") public int totalCustomers;
}
