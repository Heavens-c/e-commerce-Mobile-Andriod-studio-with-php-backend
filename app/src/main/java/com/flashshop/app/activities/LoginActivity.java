package com.flashshop.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.flashshop.app.R;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.api.ApiService;
import com.flashshop.app.models.LoginResponse;
import com.flashshop.app.utils.SessionManager;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "FlashShopAPI";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getService();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);

        TextView tvCreateAccount = findViewById(R.id.tv_create_account);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);

        btnLogin.setOnClickListener(v -> doLogin());

        tvCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent i = new Intent(this, ForgotPasswordActivity.class);
            i.putExtra(ForgotPasswordActivity.EXTRA_EMAIL, etEmail.getText().toString().trim());
            startActivity(i);
        });

        findViewById(R.id.tv_reset_code).setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class)));
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) { etEmail.setError("Email required"); return; }
        if (password.isEmpty()) { etPassword.setError("Password required"); return; }

        setLoading(true);

        apiService.login(email, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);

                LoginResponse body = response.body();
                Log.d(TAG, "login.php raw code=" + response.code() + " body=" + new Gson().toJson(body));

                if (response.code() == 401 || (body != null && "error".equals(body.status))) {
                    String msg = body != null && body.message != null ? body.message : "Invalid credentials";
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    Toast.makeText(LoginActivity.this, getString(R.string.error_invalid_response), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (body.token == null || body.token.isEmpty()) {
                    Toast.makeText(LoginActivity.this, R.string.auth_session_failed, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (body.admin != null) {
                    sessionManager.saveAdminSession(body.token, body.admin);
                    startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                    finishAffinity();
                    return;
                }

                if (body.user != null) {
                    sessionManager.saveUserSession(body.token, body.user);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finishAffinity();
                    return;
                }

                Toast.makeText(LoginActivity.this, getString(R.string.error_invalid_response), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "login network / parse failure", t);
                Toast.makeText(LoginActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? getString(R.string.loading) : getString(R.string.login_btn));
    }
}
