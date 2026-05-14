package com.flashshop.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.flashshop.app.R;
import com.flashshop.app.models.Banner;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
    private List<Banner> banners;

    public BannerAdapter(List<Banner> banners) { this.banners = banners; }

    public void updateData(List<Banner> data) { this.banners = data; notifyDataSetChanged(); }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Banner b = banners.get(pos);
        Glide.with(h.itemView.getContext()).load(b.imageUrl).centerCrop()
                .transform(new RoundedCorners(24)).into(h.ivBanner);
    }

    @Override public int getItemCount() { return banners.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        ViewHolder(@NonNull View v) { super(v); ivBanner = v.findViewById(R.id.iv_banner); }
    }
}
