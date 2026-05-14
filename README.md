# FlashShop — Android E-Commerce + PHP API

Monorepo layout: Android app (`app/`), PHP API (`backend/`), MySQL dump (`database/`).

---

## Project structure

### Android (Java + XML)

- **Activities**: Splash, Login, Register, **ForgotPassword**, **ResetPassword** (token from email), **AdminDashboard**, **AdminProductList** / **AdminProductEditor** (CRUD + image upload), **AdminReports** (WebView charts), Main, **CategoryProducts**, ProductDetail, Cart, Checkout, OrderHistory, **Search** (debounced search, suggestions, recent history), **OrderList** (admin orders)
- **Fragments**: Home, Profile  
- **Networking**: Retrofit + Gson, `BuildConfig.BASE_URL`  
- **Login**: `login.php` uses **form-urlencoded** (`email`, `password`). Response JSON: `status`, `token`, and either `user` (customer) or `admin` (admin app — opens **AdminDashboard** after success).  
- **Session**: `SharedPreferences` via `SessionManager` (tracks `account_type`: `user` vs `admin`)  
- **Material Design 3** theme  

### PHP backend (`backend/api/`)

- JSON responses (`Content-Type: application/json`).  
- **Users** authenticate with `authenticateUser()` (Bearer token, `users` table).  
- **Admins** authenticate with `authenticateAdmin()` (Bearer token, `admin` table).  
- PDO prepared statements on mutating endpoints.  

### MySQL (`database/flashshop_db.sql`)

- **12 tables**: `admin`, `users`, `categories`, `products`, `cart`, `wishlist`, `orders`, `order_items`, `reviews`, `banners`, `notifications`, `password_resets`  
- The bundled script **`DROP DATABASE IF EXISTS flashshop_db`** then creates the schema and sample data — **backup before import** if you have real data.

---

## Setup

### 1. Database

```bash
mysql -u root -p < database/flashshop_db.sql
```

Or import `database/flashshop_db.sql` in phpMyAdmin.  
If your host disallows `DROP DATABASE`, edit the file to remove those lines and use an empty schema / manual drop instead.

Ensure `backend/config/db.php` matches your server:

```php
define('DB_HOST', 'localhost');
define('DB_NAME', 'flashshop_db');
define('DB_USER', 'your_mysql_user');
define('DB_PASS', 'your_mysql_password');
define('BASE_URL', 'http://YOUR_PUBLIC_HOST'); // no trailing path; used for upload URLs
```

### 2. Deploy PHP

Upload so the API is reachable as:

```text
http://YOUR_PUBLIC_IP_OR_DOMAIN/flashshop/api/
```

Example files: `login.php`, `get_products.php`, `admin_orders.php`, etc.

### 3. Android Studio

1. **File → Open** the repo root **`android app`** (the folder that contains `app/` and `settings.gradle` / Gradle wrapper — not only the inner `app` module if your IDE expects the project root).
2. Set **`app/build.gradle`** → `BASE_URL` to match your deployed API (same path suffix `/flashshop/api/` if you keep that convention):

```groovy
buildConfigField "String", "BASE_URL", "\"http://YOUR_PUBLIC_IP/flashshop/api/\""
```

Use a host/IP the **phone or emulator can reach** (WLAN IP for a real device, or your VPS public IP). Avoid `localhost` on a physical phone.

3. Sync Gradle and Run.

Cleartext HTTP is enabled in the manifest for dev; prefer HTTPS in production.

**Search screen**: empty query shows **recent searches** (stored locally); tap to search again, long-press to remove one entry, **Clear** wipes history. Suggestions load from `search_suggestions.php` while typing.

---

## Test credentials (sample seed)

Password for **all rows below** matches the bcrypt in the SQL file: plaintext **`password`** (not `admin123` / `password123`).

| Role    | Email               | Password  |
|---------|---------------------|-----------|
| Admin   | admin@flashshop.com | password  |
| Customer| alex@example.com    | password  |
| Customer| jane@example.com    | password  |
| Customer| juan@example.com    | password  |

To change passwords, generate a new hash in PHP: `password_hash('YourPass', PASSWORD_BCRYPT)` and `UPDATE admin` / `UPDATE users`.

---

## Admin app flow

1. Log in with `admin@flashshop.com` + password above.  
2. **AdminDashboard** → **Orders** opens **OrderListActivity** (all orders, status spinner).  
3. **Products** (quick tile or bottom nav) → **AdminProductListActivity**: tap a product to edit; **long-press** to deactivate (calls `delete_product.php`, sets `is_active = 0`). FAB adds a new product.  
4. **AdminProductEditorActivity**: save JSON to `add_product.php` / `update_product.php`; use **Upload image from device** to `POST admin_upload_image.php` (multipart field `image`) — the returned URL is pasted into the image URL field.  
5. **Reports** → **AdminReportsActivity**: loads `admin_analytics.php` and renders Chart.js in a WebView.  
6. Status values in the app: `pending`, `processing`, `completed`, `cancelled`. The API maps **`completed`** to DB `order_status` **`delivered`** (see `update_order_status.php`).

---

## Forgot password & in-app reset

- **Forgot password**: `ForgotPasswordActivity` → `POST forgot_password.php` (email). Backend should use PHPMailer + SMTP (`backend/config/smtp_local.php` from `smtp.example.php`), store tokens in **`password_resets`**, and email a link to **`reset_password.php`** (browser form). Set `RESET_LINK_BASE` / `reset_link_base` in config so links are reachable from the phone.  
- **Reset without mail client on device**: **Login** and **Forgot password** screens link to **ResetPasswordActivity**, which calls `POST reset_password_api.php` with JSON `token`, `password`. Ensure `password_resets` exists and (if your PHP updates them) `users` / `admin` have any columns your `reset_password_api.php` expects.  
- Run **`database/migrations/001_flashshop_extensions.sql`** (or equivalent) once if tables/columns are missing — do not re-run conflicting `ALTER`s on production without checking.

---

## API endpoints (overview)

| Method | Endpoint | Auth | Description |
|--------|----------|------|--------------|
| POST | `login.php` | No | User or admin login; JSON `status`, `token`, `user` \| `admin` |
| POST | `register.php` | No | Register |
| POST | `forgot_password.php` | No | Forgot password (email; PHPMailer) |
| POST | `reset_password_api.php` | No | JSON `token`, `password` (mobile reset) |
| POST | `logout.php` | Bearer | Clears token on user or admin |
| GET | `get_products.php` | No | Product listing (search, category, pagination) |
| GET | `search_suggestions.php` | No | GET `q`, `limit` — name/category/tags suggestions |
| GET | `get_categories.php` | No | Categories |
| GET | `get_banners.php` | No | Banners |
| POST | `add_to_cart.php` | User | Add to cart |
| GET | `get_cart.php` | User | Cart |
| POST | `update_cart.php` | User | Update cart |
| POST/GET | `wishlist.php` | User | Wishlist |
| POST | `place_order.php` | User | Place order |
| GET | `get_orders.php` | User | Current user’s orders |
| GET | `reviews.php` | Mixed | Reviews |
| POST | `reviews.php` | User | Add review |
| GET | `profile.php` | User | Profile |
| PUT | `profile.php` | User | Update profile |
| GET | `get_notifications.php` | User | Notifications |
| GET | `admin_stats.php` | **Admin** | Aggregated counts: orders, revenue, products, customers |
| GET | `admin_orders.php` | **Admin** | All orders (`user_name`, `total_price`, `status`, …) |
| POST | `update_order_status.php` | **Admin** | JSON `id`, `status` |
| POST | `add_product.php` | **Admin** | Create product (JSON or multipart + `image`) |
| POST | `update_product.php` | **Admin** | Update product fields |
| POST | `delete_product.php` | **Admin** | Soft deactivate product (`is_active = 0`) |
| GET | `admin_list_products.php` | **Admin** | All products (incl. inactive), pagination |
| POST | `admin_upload_image.php` | **Admin** | Multipart `image` → JSON `data.url` |
| GET | `admin_analytics.php` | **Admin** | Summary, orders by status, top products, monthly revenue |

**Web (non-API)**: `backend/reset_password.php` — HTML form for reset link from email; may need `FLASHSHOP_SKIP_HTTP_HEADERS` in `db.php` so it does not force JSON-only headers.

---

## Tech stack

| Layer    | Details |
|----------|---------|
| Android  | Java 11, minSdk 24, targetSdk 34, compileSdk 36 |
| Networking | Retrofit 3.x, Gson, OkHttp logging |
| Images   | Glide 5.x |
| Backend  | PHP 8+, PDO, MySQL 8 / MariaDB 10.5+ |
| Auth     | Bearer token (SHA-256), stored server-side with expiry |

---

## Debugging tips

- Logcat tag **`FlashShopAPI`** logs parsed login bodies; OkHttp logs full HTTP bodies in debug builds.  
- If login fails: confirm **`BASE_URL`**, HTTPS vs HTTP, and that `login.php` returns **only JSON** (no PHP warnings HTML).  
- If admin lists fail: ensure the admin row exists and you use the **admin** token from `login.php` (not a customer token).
