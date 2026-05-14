package com.flashshop.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        TextInputEditText etEmail = findViewById(R.id.et_email);
        ProgressBar progress = findViewById(R.id.progress);
        String pre = getIntent().getStringExtra(EXTRA_EMAIL);
        if (!TextUtils.isEmpty(pre)) {
            etEmail.setText(pre);
        }

        findViewById(R.id.btn_back_login).setOnClickListener(v -> finish());

        findViewById(R.id.btn_have_reset_code).setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class)));

        MaterialButton send = findViewById(R.id.btn_send);
        ApiService api = ApiClient.getService();
        send.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError(getString(R.string.forgot_invalid_email));
                return;
            }
            progress.setVisibility(View.VISIBLE);
            send.setEnabled(false);
            Map<String, String> body = new HashMap<>();
            body.put("email", email);
            api.forgotPassword(body).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                    progress.setVisibility(View.GONE);
                    send.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ForgotPasswordActivity.this, R.string.forgot_success, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String msg = resolveForgotErrorMessage(response);
                        Toast.makeText(ForgotPasswordActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                    progress.setVisibility(View.GONE);
                    send.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String resolveForgotErrorMessage(Response<ApiResponse<Object>> response) {
        ApiResponse<Object> body = response.body();
        if (body != null && body.getMessage() != null && !body.getMessage().trim().isEmpty()) {
            return body.getMessage().trim();
        }
        ResponseBody err = response.errorBody();
        if (err != null) {
            try {
                String raw = err.string().trim();
                if (!raw.isEmpty()) {
                    return raw.length() > 200 ? raw.substring(0, 200) + "..." : raw;
                }
            } catch (IOException ignored) {
                // fall through
            }
        }
        return getString(R.string.error_generic) + " (" + response.code() + ")";
    }
}
