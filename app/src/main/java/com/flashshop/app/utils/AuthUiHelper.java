package com.flashshop.app.utils;

import android.app.Activity;
import android.content.Intent;

import com.flashshop.app.activities.LoginActivity;

public final class AuthUiHelper {

    private AuthUiHelper() {}

    public static void clearSessionAndOpenLogin(Activity activity) {
        new SessionManager(activity).logout();
        Intent i = new Intent(activity, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(i);
        activity.finish();
    }
}
