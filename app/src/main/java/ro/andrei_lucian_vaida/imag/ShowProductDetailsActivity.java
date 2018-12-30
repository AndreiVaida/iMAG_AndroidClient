package ro.andrei_lucian_vaida.imag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ShowProductDetailsActivity extends AppCompatActivity {
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadProductDetails(productId);
        addToWishlistTextView.setTextColor(Color.rgb(0,0,0));
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
                    public void onResponse(JSONObject response) {
                        try {
                            productNameView.setText(response.getString("name"));
                            productPriceView.setText(response.getString("price") + " lei");
                            productDetailsView.setText(response.getString("details"));

                            if (response.has("image")) {
                                final byte[] imageByteArray = Base64.decode(response.getString("image"), Base64.DEFAULT);
                                final Bitmap bmp = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                                if (bmp != null) {
                                    productImageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, productImageView.getWidth(), productImageView.getHeight(), false));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        productNameView.setText("Produs indisponibil");
                        productDetailsView.setText(error.getMessage());
                    }
                });
        queue.add(jsonObjectRequest);
    }

    public void goToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void addToWishlist(View view) {
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
}
