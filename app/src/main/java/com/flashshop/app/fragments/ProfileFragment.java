package com.flashshop.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.flashshop.app.R;
import com.flashshop.app.activities.CartActivity;
import com.flashshop.app.activities.LoginActivity;
import com.flashshop.app.activities.OrderHistoryActivity;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.models.ApiResponse;
import com.flashshop.app.models.User;
import com.flashshop.app.utils.SessionManager;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private SessionManager session;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());
        User user = session.getUser();

        if (user != null) {
            ((TextView) view.findViewById(R.id.tv_name)).setText(user.fullName);
            ((TextView) view.findViewById(R.id.tv_membership)).setText(user.membership != null ? user.membership.substring(0, 1).toUpperCase() + user.membership.substring(1) + " Member" : "Standard Member");
            ((TextView) view.findViewById(R.id.tv_coins)).setText(String.valueOf(user.coins));
            ((TextView) view.findViewById(R.id.tv_points)).setText(String.valueOf(user.points));

            if (user.avatarUrl != null) {
                Glide.with(this).load(user.avatarUrl).placeholder(R.drawable.ic_person).into((CircleImageView) view.findViewById(R.id.iv_avatar));
            }
        }

        view.findViewById(R.id.btn_cart).setOnClickListener(v -> startActivity(new Intent(getActivity(), CartActivity.class)));
        view.findViewById(R.id.btn_order_history).setOnClickListener(v -> startActivity(new Intent(getActivity(), OrderHistoryActivity.class)));
        view.findViewById(R.id.btn_wishlist).setOnClickListener(v -> Toast.makeText(getContext(), "Wishlist", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.logout))
                    .setMessage(getString(R.string.logout_confirm))
                    .setPositiveButton(getString(R.string.confirm), (d, w) -> {
                        ApiClient.getService().logout(session.getToken()).enqueue(new Callback<ApiResponse<Object>>() {
                            @Override public void onResponse(Call<ApiResponse<Object>> c, Response<ApiResponse<Object>> r) {}
                            @Override public void onFailure(Call<ApiResponse<Object>> c, Throwable t) {}
                        });
                        session.logout();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        requireActivity().finishAffinity();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });
    }
}
