package com.flashshop.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SearchHistoryHelper {

    private static final String PREF = "flashshop_search_history";
    private static final String KEY = "queries";
    private static final int MAX = 10;

    private SearchHistoryHelper() {}

    public static void add(Context ctx, String query) {
        if (query == null || query.trim().length() < 2) return;
        String q = query.trim();
        SharedPreferences p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        Set<String> set = new LinkedHashSet<>();
        set.add(q);
        set.addAll(p.getStringSet(KEY, new LinkedHashSet<>()));
        List<String> list = new ArrayList<>(set);
        if (list.size() > MAX) {
            list = list.subList(0, MAX);
        }
        p.edit().putStringSet(KEY, new LinkedHashSet<>(list)).apply();
    }

    public static List<String> get(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return new ArrayList<>(p.getStringSet(KEY, new LinkedHashSet<>()));
    }

    public static void remove(Context ctx, String query) {
        if (query == null || query.trim().isEmpty()) return;
        String q = query.trim();
        SharedPreferences p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        Set<String> cur = p.getStringSet(KEY, new LinkedHashSet<>());
        LinkedHashSet<String> next = new LinkedHashSet<>(cur);
        next.remove(q);
        p.edit().putStringSet(KEY, next).apply();
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().remove(KEY).apply();
    }
}
