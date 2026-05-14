package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdminAnalyticsData {
    @SerializedName("summary") public Summary summary;
    @SerializedName("orders_by_status") public List<StatusSlice> ordersByStatus;
    @SerializedName("top_products") public List<TopProduct> topProducts;
    @SerializedName("monthly") public List<MonthlyRow> monthly;

    public static class Summary {
        @SerializedName("total_orders") public int totalOrders;
        @SerializedName("total_revenue") public double totalRevenue;
        @SerializedName("total_users") public int totalUsers;
        @SerializedName("active_products") public int activeProducts;
    }

    public static class StatusSlice {
        @SerializedName("label") public String label;
        @SerializedName("value") public int value;
    }

    public static class TopProduct {
        @SerializedName("name") public String name;
        @SerializedName("units_sold") public int unitsSold;
    }

    public static class MonthlyRow {
        @SerializedName("month") public String month;
        @SerializedName("orders") public int orders;
        @SerializedName("revenue") public double revenue;
    }
}
