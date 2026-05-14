package com.flashshop.app.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.flashshop.app.R;

import java.util.ArrayList;
import java.util.List;

/** Two-column dashboard metrics (orders, revenue, products, customers). */
public class AdminDashboardStatAdapter extends RecyclerView.Adapter<AdminDashboardStatAdapter.VH> {

    public static final class Stat {
        public final int iconRes;
        @ColorInt
        public final int accentColor;
        public final String value;
        public final String label;

        public Stat(int iconRes, @ColorInt int accentColor, String value, String label) {
            this.iconRes = iconRes;
            this.accentColor = accentColor;
            this.value = value;
            this.label = label;
        }
    }

    private final List<Stat> stats = new ArrayList<>();

    public void setStats(List<Stat> next) {
        stats.clear();
        if (next != null) stats.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_stat_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Stat s = stats.get(position);
        h.tvValue.setText(s.value);
        h.tvLabel.setText(s.label);
        h.ivIcon.setImageResource(s.iconRes);
        h.ivIcon.setColorFilter(s.accentColor);

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        int bg = android.graphics.Color.argb(42,
                android.graphics.Color.red(s.accentColor),
                android.graphics.Color.green(s.accentColor),
                android.graphics.Color.blue(s.accentColor));
        gd.setColor(bg);
        
        // Use setBackground directly since minSdk is 24 (setBackground is available from API 16+)
        h.flIcon.setBackground(gd);

        h.vAccentBar.setBackgroundColor(s.accentColor);
    }

    @Override
    public int getItemCount() {
        return stats.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final FrameLayout flIcon;
        final ImageView ivIcon;
        final View vAccentBar;
        final TextView tvValue;
        final TextView tvLabel;

        VH(@NonNull View itemView) {
            super(itemView);
            flIcon = itemView.findViewById(R.id.fl_stat_icon);
            ivIcon = itemView.findViewById(R.id.iv_stat_icon);
            vAccentBar = itemView.findViewById(R.id.v_stat_accent_bar);
            tvValue = itemView.findViewById(R.id.tv_stat_value);
            tvLabel = itemView.findViewById(R.id.tv_stat_label);
        }
    }
}
