package com.flashshop.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;

import com.flashshop.app.R;
import com.flashshop.app.models.AdminOrderRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Admin order rows with status Spinner (immediate API update from {@link Listener}). */
public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.VH> {

    public interface Listener {
        void onAdminOrderStatusSelected(AdminOrderRow row, String newStatus);
    }

    private final List<AdminOrderRow> orders = new ArrayList<>();
    private final String[] statusValues;
    private final Listener listener;

    public AdminOrderAdapter(Context context, Listener listener) {
        this.statusValues = context.getResources().getStringArray(R.array.admin_order_statuses);
        this.listener = listener;
    }

    public void setOrders(List<AdminOrderRow> next) {
        orders.clear();
        if (next != null) orders.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminOrderRow row = orders.get(position);
        h.tvId.setText(String.format(Locale.US, "#%d", row.id));
        h.tvUser.setText(row.userName);
        h.tvTotal.setText(String.format(Locale.US, "$%.2f", row.totalPrice));
        h.tvCreated.setText(row.createdAt != null ? row.createdAt : "");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                h.itemView.getContext(),
                R.array.admin_order_statuses,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        h.spinner.setAdapter(adapter);

        int idx = indexOfStatus(row.status);
        h.suppressSpinnerCallback = true;
        h.spinner.setSelection(idx, false);
        h.spinner.post(() -> h.suppressSpinnerCallback = false);

        h.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (h.suppressSpinnerCallback) return;
                int adapterPos = h.getBindingAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;
                AdminOrderRow current = orders.get(adapterPos);
                String next = statusValues[pos];
                if (next.equalsIgnoreCase(current.status)) return;
                listener.onAdminOrderStatusSelected(current, next);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }
        });
    }

    private int indexOfStatus(String dbOrUiStatus) {
        if (dbOrUiStatus == null) return 0;
        for (int i = 0; i < statusValues.length; i++) {
            if (statusValues[i].equalsIgnoreCase(dbOrUiStatus)) return i;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        final TextView tvId, tvUser, tvTotal, tvCreated;
        final AppCompatSpinner spinner;
        boolean suppressSpinnerCallback;

        VH(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_order_id);
            tvUser = itemView.findViewById(R.id.tv_user_name);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
            tvCreated = itemView.findViewById(R.id.tv_created);
            spinner = itemView.findViewById(R.id.spinner_status);
        }
    }
}
