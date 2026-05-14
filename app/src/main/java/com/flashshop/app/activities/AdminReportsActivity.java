package com.flashshop.app.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flashshop.app.R;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.api.ApiService;
import com.flashshop.app.models.AdminAnalyticsData;
import com.flashshop.app.models.ApiResponse;
import com.flashshop.app.utils.AuthUiHelper;
import com.flashshop.app.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * In-app analytics using Chart.js (loaded from CDN) inside a WebView.
 */
public class AdminReportsActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn() || !session.isAdminAccount()) {
            AuthUiHelper.clearSessionAndOpenLogin(this);
            return;
        }

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        WebView wv = findViewById(R.id.webview);
        ProgressBar progress = findViewById(R.id.progress);
        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        wv.setWebViewClient(new WebViewClient());

        ApiClient.getService().getAdminAnalytics(session.getToken()).enqueue(new Callback<ApiResponse<AdminAnalyticsData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AdminAnalyticsData>> call,
                                   @NonNull Response<ApiResponse<AdminAnalyticsData>> response) {
                progress.setVisibility(View.GONE);
                if (response.code() == 401) {
                    AuthUiHelper.clearSessionAndOpenLogin(AdminReportsActivity.this);
                    return;
                }
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()
                        || response.body().getData() == null) {
                    Toast.makeText(AdminReportsActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                    return;
                }
                String json = new Gson().toJson(response.body().getData());
                String html = buildHtml(json);
                wv.loadDataWithBaseURL("https://cdn.jsdelivr.net/", html, "text/html", "UTF-8", null);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AdminAnalyticsData>> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(AdminReportsActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static String buildHtml(String jsonPayload) {
        String safe = jsonPayload.replace("<", "\\u003c");
        return "<!DOCTYPE html><html><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>"
                + "<style>body{font-family:sans-serif;padding:16px;background:#faf7ff;color:#1d1b20;}h2{color:#6750a4;}canvas{max-width:100%;margin-bottom:24px;}</style>"
                + "</head><body>"
                + "<h2>FlashShop analytics</h2>"
                + "<div id='sum'></div>"
                + "<h3>Orders by status</h3><canvas id='pie'></canvas>"
                + "<h3>Last 6 months (revenue)</h3><canvas id='bar'></canvas>"
                + "<h3>Top products (units sold)</h3><canvas id='top'></canvas>"
                + "<script>const DATA=" + safe + ";"
                + "const s=DATA.summary||{};"
                + "document.getElementById('sum').innerHTML='<p>Orders: '+(s.total_orders||0)+'</p>"
                + "<p>Revenue: $'+Number(s.total_revenue||0).toFixed(2)+'</p>"
                + "<p>Users: '+(s.total_users||0)+'</p>"
                + "<p>Active products: '+(s.active_products||0)+'</p>';"
                + "const obs=(DATA.orders_by_status||[]);const labels=obs.map(x=>x.label||'Unknown');const vals=obs.map(x=>parseInt(x.value,10)||0);"
                + "if(labels.length){new Chart(document.getElementById('pie'),{type:'doughnut',data:{labels:labels,datasets:[{data:vals,backgroundColor:['#FF9800','#2196F3','#4CAF50','#9C27B0','#607D8B','#E91E63','#795548']}]} ,options:{plugins:{legend:{position:'bottom'}}}});}"
                + "const mon=(DATA.monthly||[]);const m=mon.map(x=>x.month);const rev=mon.map(x=>parseFloat(x.revenue)||0);"
                + "if(m.length){new Chart(document.getElementById('bar'),{type:'bar',data:{labels:m,datasets:[{label:'Revenue',data:rev,backgroundColor:'#6750a4'}]},options:{scales:{y:{beginAtZero:true}}}}});}"
                + "const tops=(DATA.top_products||[]);const tn=tops.map(x=>x.name||'Item');const tu=tops.map(x=>parseInt(x.units_sold,10)||0);"
                + "if(tn.length){new Chart(document.getElementById('top'),{type:'bar',data:{labels:tn,datasets:[{label:'Units',data:tu,backgroundColor:'#00695c'}]},options:{indexAxis:'y',scales:{x:{beginAtZero:true}}}}});}"
                + "</script></body></html>";
    }
}
