package com.flashshop.app.activities;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flashshop.app.R;
import com.flashshop.app.api.ApiClient;
import com.flashshop.app.api.ApiService;
import com.flashshop.app.models.ApiResponse;
import com.flashshop.app.models.Category;
import com.flashshop.app.models.CategoryListData;
import com.flashshop.app.models.Product;
import com.flashshop.app.models.UploadImageData;
import com.flashshop.app.utils.AuthUiHelper;
import com.flashshop.app.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductEditorActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "product";

    private SessionManager session;
    private ApiService api;
    private Product editing;
    private List<Category> categories = new ArrayList<>();
    private Spinner spinner;
    private TextInputEditText etImage;
    private ProgressBar progress;
    private MaterialButton btnUpload;
    private ActivityResultLauncher<String> pickImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) uploadPickedImage(uri);
        });

        setContentView(R.layout.activity_admin_product_editor);

        session = new SessionManager(this);
        api = ApiClient.getService();
        if (!session.isLoggedIn() || !session.isAdminAccount()) {
            AuthUiHelper.clearSessionAndOpenLogin(this);
            return;
        }

        if (Build.VERSION.SDK_INT >= 33) {
            editing = getIntent().getSerializableExtra(EXTRA_PRODUCT, Product.class);
        } else {
            editing = (Product) getIntent().getSerializableExtra(EXTRA_PRODUCT);
        }

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());
        tb.setTitle(editing == null ? getString(R.string.admin_add_product_title) : getString(R.string.admin_edit_product));

        spinner = findViewById(R.id.spinner_category);
        TextInputEditText etName = findViewById(R.id.et_name);
        TextInputEditText etDesc = findViewById(R.id.et_desc);
        TextInputEditText etPrice = findViewById(R.id.et_price);
        TextInputEditText etStock = findViewById(R.id.et_stock);
        etImage = findViewById(R.id.et_image_url);
        TextInputEditText etTags = findViewById(R.id.et_tags);
        SwitchMaterial swActive = findViewById(R.id.switch_active);
        progress = findViewById(R.id.progress);
        btnUpload = findViewById(R.id.btn_upload_image);

        if (editing != null) {
            etName.setText(editing.name);
            etDesc.setText(editing.description);
            etPrice.setText(String.valueOf(editing.price));
            etStock.setText(String.valueOf(editing.stock));
            etImage.setText(editing.imageUrl);
            if (editing.tags != null) etTags.setText(editing.tags);
            swActive.setChecked(editing.isActive);
        } else {
            swActive.setChecked(true);
        }

        btnUpload.setOnClickListener(v -> pickImage.launch("image/*"));

        loadCategories(() -> {
            if (categories.isEmpty()) {
                Toast.makeText(this, R.string.admin_no_categories, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            List<String> names = new ArrayList<>();
            for (Category c : categories) names.add(c.name);
            ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
            spinner.setAdapter(ad);
            if (editing != null) {
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).id == guessCategoryId(editing)) {
                        spinner.setSelection(i);
                        break;
                    }
                }
            }
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            int catPos = spinner.getSelectedItemPosition();
            if (catPos < 0 || catPos >= categories.size()) {
                Toast.makeText(this, R.string.admin_pick_category, Toast.LENGTH_SHORT).show();
                return;
            }
            int catId = categories.get(catPos).id;
            String name = text(etName);
            String desc = text(etDesc);
            String img = text(etImage);
            if (name.isEmpty() || desc.isEmpty() || img.isEmpty()) {
                Toast.makeText(this, R.string.admin_fill_required, Toast.LENGTH_SHORT).show();
                return;
            }
            double price;
            int stock;
            try {
                price = Double.parseDouble(text(etPrice));
                stock = Integer.parseInt(text(etStock));
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.admin_invalid_numbers, Toast.LENGTH_SHORT).show();
                return;
            }
            if (price <= 0 || stock < 0) {
                Toast.makeText(this, R.string.admin_invalid_numbers, Toast.LENGTH_SHORT).show();
                return;
            }

            progress.setVisibility(View.VISIBLE);
            if (editing == null) {
                Map<String, Object> body = new HashMap<>();
                body.put("category_id", catId);
                body.put("name", name);
                body.put("description", desc);
                body.put("price", price);
                body.put("stock", stock);
                body.put("image_url", img);
                String tags = text(etTags);
                if (!tags.isEmpty()) body.put("tags", tags);
                api.addProduct(session.getToken(), body).enqueue(done(progress));
            } else {
                Map<String, Object> body = new HashMap<>();
                body.put("id", editing.id);
                body.put("category_id", catId);
                body.put("name", name);
                body.put("description", desc);
                body.put("price", price);
                body.put("stock", stock);
                body.put("image_url", img);
                body.put("tags", text(etTags));
                body.put("is_active", swActive.isChecked() ? 1 : 0);
                api.updateProduct(session.getToken(), body).enqueue(done(progress));
            }
        });
    }

    private void uploadPickedImage(Uri uri) {
        File temp;
        try {
            temp = copyUriToCache(uri);
        } catch (IOException e) {
            Toast.makeText(this, R.string.admin_upload_read_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        String mime = getContentResolver().getType(uri);
        if (mime == null || !mime.startsWith("image/")) {
            mime = "image/jpeg";
        }
        MediaType mediaType = MediaType.parse(mime);
        RequestBody fileBody = RequestBody.create(temp, mediaType);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image", temp.getName(), fileBody);

        progress.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);
        api.adminUploadImage(session.getToken(), part).enqueue(new Callback<ApiResponse<UploadImageData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UploadImageData>> call,
                                   @NonNull Response<ApiResponse<UploadImageData>> response) {
                progress.setVisibility(View.GONE);
                btnUpload.setEnabled(true);
                if (!temp.delete()) {
                    // ignore
                }
                if (response.code() == 401) {
                    AuthUiHelper.clearSessionAndOpenLogin(AdminProductEditorActivity.this);
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null && response.body().getData().url != null) {
                    etImage.setText(response.body().getData().url);
                    Toast.makeText(AdminProductEditorActivity.this, R.string.admin_upload_ok, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminProductEditorActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UploadImageData>> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                btnUpload.setEnabled(true);
                if (!temp.delete()) {
                    // ignore
                }
                Toast.makeText(AdminProductEditorActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File copyUriToCache(Uri uri) throws IOException {
        String ext = ".jpg";
        String mime = getContentResolver().getType(uri);
        if (mime != null) {
            if (mime.contains("png")) ext = ".png";
            else if (mime.contains("webp")) ext = ".webp";
            else if (mime.contains("gif")) ext = ".gif";
        }
        File out = File.createTempFile("upload_", ext, getCacheDir());
        try (InputStream in = getContentResolver().openInputStream(uri);
             FileOutputStream fos = new FileOutputStream(out)) {
            if (in == null) throw new IOException("openInputStream null");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                fos.write(buf, 0, n);
            }
        }
        return out;
    }

    private int guessCategoryId(Product p) {
        return p.categoryId;
    }

    private String text(TextInputEditText e) {
        return e.getText() != null ? e.getText().toString().trim() : "";
    }

    private void loadCategories(Runnable after) {
        api.getCategories().enqueue(new Callback<ApiResponse<CategoryListData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CategoryListData>> call,
                                   @NonNull Response<ApiResponse<CategoryListData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null && response.body().getData().categories != null) {
                    categories = response.body().getData().categories;
                }
                after.run();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CategoryListData>> call, @NonNull Throwable t) {
                after.run();
            }
        });
    }

    private Callback<ApiResponse<Object>> done(ProgressBar progressBar) {
        return new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.code() == 401) {
                    AuthUiHelper.clearSessionAndOpenLogin(AdminProductEditorActivity.this);
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminProductEditorActivity.this, R.string.admin_saved, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminProductEditorActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminProductEditorActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        };
    }
}
