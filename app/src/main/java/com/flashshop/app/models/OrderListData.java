package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderListData {
    @SerializedName("orders") public List<Order> orders;
}

