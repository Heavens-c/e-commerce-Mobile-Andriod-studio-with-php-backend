package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdminOrdersData {
    @SerializedName("orders") public List<AdminOrderRow> orders;
}
