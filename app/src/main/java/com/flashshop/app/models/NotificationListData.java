package com.flashshop.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificationListData {
    @SerializedName("notifications") public List<Notification> notifications;
}

