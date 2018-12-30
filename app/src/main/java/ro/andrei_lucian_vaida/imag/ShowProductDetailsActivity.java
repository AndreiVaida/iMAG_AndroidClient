package ro.andrei_lucian_vaida.imag;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.IMagDatabase;
import domain.Product;
import domain.TaskToDoWhenOnline;

public class ShowProductDetailsActivity extends AppCompatActivity {
    private static final String DATABASE_NAME = "iMag_db";
    private dao.IMagDatabase IMagDatabase;
    private boolean weAreOnline;
    private Integer productId;
    private TextView productNameView;
    private TextView productPriceView;
    private TextView productDetailsView;
    private TextView addToWishlistTextView;
    private ImageView productImageView;
    private final String productUrl = "/product";
    private final String wishlistUrl = "/user/wishlist";
    private RequestQueue queue;
    private Integer userId;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_product_details);
        productNameView = findViewById(R.id.productNameView);
        productPriceView = findViewById(R.id.productPriceView);
        productDetailsView = findViewById(R.id.productDetailsView);
        productImageView = findViewById(R.id.productImageView);
        addToWishlistTextView = findViewById(R.id.addToWishlistTextView);

        queue = Volley.newRequestQueue(this);
        final Intent intent = getIntent();
        productId = intent.getIntExtra("productId", -1);
        getSharedPreferences();

        IMagDatabase = Room.databaseBuilder(getApplicationContext(),
                dao.IMagDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();

        weAreOnline = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (weAreOnline) {
            loadProductDetails(productId);
        }
        addToWishlistTextView.setTextColor(Color.rgb(0, 0, 0));
    }

    private void getSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("security", Context.MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        token = prefs.getString("token", "");
    }

    private void loadProductDetails(final Integer productId) {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                MainActivity.serverUrl + productUrl + "/" + productId,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject jsonProduct) {
                        final Product product = jsonToProduct(jsonProduct);
                        if (product != null) {
                            populateView(product);
                        }
                        else {
                            productNameView.setText("Produsul nu este valid.");
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    weAreOnline = false;
                    loadProductDetailsFromLocalStorage(productId);
                } else {
                    productNameView.setText("Produs indisponibil");
                }
            }
        });
        queue.add(jsonObjectRequest);
    }

    private Product jsonToProduct(final JSONObject jsonProduct) {
        try {
            final Integer id = jsonProduct.getInt("id");
            final String name = jsonProduct.getString("name");
            final Integer price = jsonProduct.getInt("price");
            final String details = jsonProduct.getString("details");
            final String image = jsonProduct.getString("image");
            return new Product(id, name, price, details, image);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadProductDetailsFromLocalStorage(final Integer productId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Product product = IMagDatabase.productDao().getById(productId);
                final Handler handler = new Handler(Looper.getMainLooper());
                final Runnable runnable = new Runnable() {
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                populateView(product);
                            }
                        });
                    }
                };
                new Thread(runnable).start();
            }
        }).start();
    }

    private void populateView(final Product product) {
        productNameView.setText(product.getName());
        productPriceView.setText(product.getPrice() + " lei");
        productDetailsView.setText(product.getDetails());

        if (product.getImage() != null) {
            final byte[] imageByteArray = Base64.decode(product.getImage(), Base64.DEFAULT);
            final Bitmap bmp = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            if (bmp != null) {
                productImageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, productImageView.getWidth(), productImageView.getHeight(), false));
            }
        }
    }

    public void goToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void addToWishlist(View view) {
        if(!weAreOnline) {
            addToWishlistLocalStorage(productId);
            return;
        }

        final StringRequest stringRequest = new StringRequest(Request.Method.POST,
                MainActivity.serverUrl + wishlistUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        addToWishlistTextView.setText("Adăugat\nîn wishlist !");
                        addToWishlistTextView.setTextColor(Color.rgb(37, 178, 41));
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                addToWishlistTextView.setText("Nu a fost adăugat\nîn wishlist.");
                addToWishlistTextView.setTextColor(Color.rgb(232, 73, 30));
                error.printStackTrace();
            }
        }) {
            // save header
            @Override
            public Map<String, String> getHeaders() {
                final HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/json");
                headers.put("userId", userId.toString());
                headers.put("productId", productId.toString());
                headers.put("token", token);
                return headers;
            }
        };
        queue.add(stringRequest);
    }

    private void addToWishlistLocalStorage(final Integer productId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Product product = IMagDatabase.productDao().getById(productId);
                product.setInWishlist(true);
                IMagDatabase.productDao().save(product);

                final TaskToDoWhenOnline taskToDoWhenOnline = new TaskToDoWhenOnline(product.getId(), true);
                IMagDatabase.taskToDoWhenOnlineDao().save(taskToDoWhenOnline);

                final Handler handler = new Handler(Looper.getMainLooper());
                final Runnable runnable = new Runnable() {
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                addToWishlistTextView.setText("Adăugat\nîn wishlist\noffline.");
                                addToWishlistTextView.setTextColor(Color.rgb(208, 219, 61));
                            }
                        });
                    }
                };
                new Thread(runnable).start();

            }
        }).start();
    }
}
