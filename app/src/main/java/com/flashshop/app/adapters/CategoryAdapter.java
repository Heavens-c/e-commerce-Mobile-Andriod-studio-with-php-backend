package com.flashshop.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.flashshop.app.R;
import com.flashshop.app.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    public interface OnCategoryClickListener {
        void onCategoryClick(@NonNull Category category);
    }

    private List<Category> categories;
    @Nullable
    private OnCategoryClickListener clickListener;

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    public void setOnCategoryClickListener(@Nullable OnCategoryClickListener listener) {
        this.clickListener = listener;
    }

    public void updateData(List<Category> data) {
        this.categories = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Category c = categories.get(pos);
        h.tvName.setText(c.name);
        h.tvIcon.setVisibility(View.GONE);
        h.ivPhoto.setVisibility(View.VISIBLE);
        if (c.imageUrl != null && !c.imageUrl.isEmpty()) {
            Glide.with(h.itemView.getContext()).load(c.imageUrl).centerCrop().into(h.ivPhoto);
        } else {
            h.ivPhoto.setImageResource(R.drawable.ic_flash_logo);
        }
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCategoryClick(c);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories == null ? 0 : categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvIcon;
        final ImageView ivPhoto;

        ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_cat_name);
            tvIcon = v.findViewById(R.id.tv_cat_icon);
            ivPhoto = v.findViewById(R.id.iv_cat_image);
        }
    }
}
