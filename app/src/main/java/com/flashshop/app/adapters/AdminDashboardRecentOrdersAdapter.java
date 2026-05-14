package com.flashshop.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.flashshop.app.R;
import com.flashshop.app.models.AdminOrderRow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/** Compact rows for dashboard “recent orders”. */
public class AdminDashboardRecentOrdersAdapter extends RecyclerView.Adapter<AdminDashboardRecentOrdersAdapter.VH> {

    private final List<AdminOrderRow> rows = new ArrayList<>();
    private final SimpleDateFormat isoParser;
    private final SimpleDateFormat displayFormat;

    public AdminDashboardRecentOrdersAdapter() {
        isoParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        isoParser.setTimeZone(TimeZone.getDefault());
        displayFormat = new SimpleDateFormat("MMM d · h:mm a", Locale.getDefault());
    }

    public void setRows(List<AdminOrderRow> next) {
        rows.clear();
        if (next != null) rows.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_recent_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminOrderRow o = rows.get(position);
        h.tvId.setText(String.format(Locale.US, "#ORD-%05d", o.id));
        h.tvCustomer.setText(o.userName != null ? o.userName : "—");
        h.tvPrice.setText(String.format(Locale.US, "$%.2f", o.totalPrice));
        h.tvDate.setText(formatTimestamp(o.createdAt));

        bindStatusBadge(h.tvStatus, o.status);
    }

    private String formatTimestamp(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        try {
            Date d = isoParser.parse(raw.substring(0, Math.min(raw.length(), 19)));
            if (d != null) return displayFormat.format(d);
        } catch (ParseException ignored) { /* fallback */ }
        return raw.length() >= 16 ? raw.substring(0, 16) : raw;
    }

    private void bindStatusBadge(TextView tv, String statusRaw) {
        String status = statusRaw != null ? statusRaw.toLowerCase(Locale.US) : "pending";
        @DrawableRes int bg;
        int textColorRes;
        final String label;

        switch (status) {
            case "processing":
                bg = R.drawable.bg_badge_processing;
                textColorRes = R.color.badge_text_processing;
                label = "Processing";
                break;
            case "completed":
                bg = R.drawable.bg_badge_completed;
                textColorRes = R.color.badge_text_completed;
                label = "Completed";
                break;
            case "cancelled":
                bg = R.drawable.bg_badge_cancelled;
                textColorRes = R.color.badge_text_cancelled;
                label = "Cancelled";
                break;
            case "pending":
                bg = R.drawable.bg_badge_pending;
                textColorRes = R.color.badge_text_pending;
                label = "Pending";
                break;
            default:
                bg = R.drawable.bg_badge_pending;
                textColorRes = R.color.badge_text_pending;
                label = capitalize(statusRaw != null ? statusRaw : "Pending");
                break;
        }

        tv.setText(label);
        tv.setBackgroundResource(bg);
        tv.setTextColor(ContextCompat.getColor(tv.getContext(), textColorRes));
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase(Locale.US) + s.substring(1).toLowerCase(Locale.US);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final TextView tvId, tvStatus, tvCustomer, tvPrice, tvDate;

        VH(@NonNull android.view.View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_recent_order_id);
            tvStatus = itemView.findViewById(R.id.tv_recent_status);
            tvCustomer = itemView.findViewById(R.id.tv_recent_customer);
            tvPrice = itemView.findViewById(R.id.tv_recent_price);
            tvDate = itemView.findViewById(R.id.tv_recent_date);
        }
    }
}
