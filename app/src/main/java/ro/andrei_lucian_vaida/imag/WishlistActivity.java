package ro.andrei_lucian_vaida.imag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.view.Gravity.CENTER_VERTICAL;

public class WishlistActivity extends AppCompatActivity {
    private final String wishlistUrl = "/user/wishlist";
    private RequestQueue queue;
    private LinearLayout productsLayout;
    private TextView title;
    private TextView statusTextView;
    private Integer userId;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        productsLayout = findViewById(R.id.productsLayout);
        title = findViewById(R.id.title);
        statusTextView = findViewById(R.id.statusTextView);
        queue = Volley.newRequestQueue(this);

        getSharedPreferences();
        if (userId < 0 || token.length() == 0) {
            goToLoginActivity();
            return;
        }
        loadWishlist(userId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        statusTextView.setText("");
    }

    private void loadWishlist(final Integer userId) {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                MainActivity.serverUrl + wishlistUrl,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonProducts = response.getJSONArray("productDtos");
                            productsLayout.removeAllViews();

                            for (int i = 0; i < jsonProducts.length(); i++) {

                                // get JSON product
                                final JSONObject jsonProduct = jsonProducts.getJSONObject(i);

                                // create a new layout for the product
                                final LinearLayout productLayout = createNewProductLayout(jsonProduct);

                                // add the product view to the page
                                productsLayout.addView(productLayout);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                title.setText("Error: " + error.toString());
            }
        }) {
            // add header
            @Override
            public Map<String, String> getHeaders() {
                final HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/json");
                headers.put("userId", userId.toString());
                headers.put("token", token);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void getSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("security", Context.MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        token = prefs.getString("token", "");
    }

    private LinearLayout createNewProductLayout(final JSONObject jsonProduct) throws JSONException {
        // image
        final byte[] byteArray = jsonProduct.getString("image").getBytes();
        final Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        final ImageView imageView = new ImageView(WishlistActivity.this);
        imageView.setImageBitmap(bmp);
        // name
        final TextView productNameView = new TextView(WishlistActivity.this);
        productNameView.setText("Denumire: " + jsonProduct.getString("name"));
        // price
        final TextView productPriceView = new TextView(WishlistActivity.this);
        productPriceView.setText("Preț: " + jsonProduct.getString("price"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(200, 0, 0, 0);
        productPriceView.setLayoutParams(params);

        // create a new layout for the product
        final LinearLayout productLayout = new LinearLayout(WishlistActivity.this);
        productLayout.setBackgroundColor(Color.parseColor("#91bbd1"));
        productLayout.setPadding(5, 5, 5, 5);
        productLayout.setGravity(CENTER_VERTICAL);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 5, 0, 5);
        layoutParams.height = 100;
        productLayout.setLayoutParams(layoutParams);
        productLayout.setOrientation(LinearLayout.HORIZONTAL);
        productLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final Integer productId = jsonProduct.getInt("id");
                    goToProductDetailsActivity(productId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        final ImageButton removeProductButton = new ImageButton(this);
        removeProductButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        final LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0, 5, 0, 5);
        buttonParams.height = 100;
        buttonParams.width = 100;
        removeProductButton.setLayoutParams(buttonParams);
        removeProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final Integer productId = jsonProduct.getInt("id");
                    removeProductFromWishlist(productId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // populate the product layout
        productLayout.addView(imageView);
        productLayout.addView(productNameView);
        productLayout.addView(productPriceView);
        productLayout.addView(removeProductButton);

        return productLayout;
    }

    private void removeProductFromWishlist(final Integer productId) {
        final StringRequest stringRequest = new StringRequest(Request.Method.DELETE,
                MainActivity.serverUrl + wishlistUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        statusTextView.setText("Produsul a fost eliminat din wishlist.");
                        statusTextView.setTextColor(Color.rgb(37, 178, 41));
                        loadWishlist(userId);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                statusTextView.setText("Produsul nu a putut fi eliminat din wishlist.");
                statusTextView.setTextColor(Color.rgb(232, 73, 30));
                error.printStackTrace();
            }
        }) {
            // add header
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

    private void goToProductDetailsActivity(Integer productId) {
        Intent intent = new Intent(this, ShowProductDetailsActivity.class);
        intent.putExtra("productId", productId);
        startActivity(intent);
    }

    public void goToMainActivity(View view) {
        goToMainActivity();
    }

    public void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
