package com.flashshop.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flashshop.app.R;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.api.ApiService;
import com.flashshop.app.models.ApiResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    public static final String EXTRA_TOKEN = "token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        TextInputEditText etToken = findViewById(R.id.et_token);
        TextInputEditText etPass = findViewById(R.id.et_password);
        TextInputEditText etPass2 = findViewById(R.id.et_password_confirm);
        MaterialButton btnSubmit = findViewById(R.id.btn_submit);
        MaterialButton btnBack = findViewById(R.id.btn_back);
        ProgressBar progress = findViewById(R.id.progress);

        String pre = getIntent().getStringExtra(EXTRA_TOKEN);
        if (pre != null && !pre.isEmpty()) {
            etToken.setText(pre.trim());
        }

        btnBack.setOnClickListener(v -> finish());

        ApiService api = ApiClient.getService();
        btnSubmit.setOnClickListener(v -> {
            String token = text(etToken).replaceAll("[^a-fA-F0-9]", "");
            String p1 = text(etPass);
            String p2 = text(etPass2);
            if (token.length() < 32) {
                etToken.setError(getString(R.string.reset_token_invalid));
                return;
            }
            if (p1.length() < 6) {
                etPass.setError(getString(R.string.reset_password_short));
                return;
            }
            if (!p1.equals(p2)) {
                etPass2.setError(getString(R.string.reset_password_mismatch));
                return;
            }
            progress.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);
            Map<String, String> body = new HashMap<>();
            body.put("token", token);
            body.put("password", p1);
            api.resetPasswordApi(body).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                    progress.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ResetPasswordActivity.this, R.string.reset_password_success, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                    progress.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(ResetPasswordActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private static String text(TextInputEditText e) {
        return e.getText() != null ? e.getText().toString().trim() : "";
    }
}
