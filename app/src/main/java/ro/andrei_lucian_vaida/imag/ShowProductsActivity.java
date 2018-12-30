package ro.andrei_lucian_vaida.imag;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static android.view.Gravity.CENTER_VERTICAL;

public class ShowProductsActivity extends AppCompatActivity {
    private final String productsUrl = "/product";
    private RequestQueue queue;
    private TextView title;
    private ScrollView productsScrollView;
    private LinearLayout productsLayout;
    private Integer pageNumber;
    private Integer totalPages;
    private Integer itemsPerPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_products);
        // initialize components from XML
        title = findViewById(R.id.title);
        productsScrollView = findViewById(R.id.productsScrollView);
        productsLayout = findViewById(R.id.productsLayout);
        queue = Volley.newRequestQueue(this);
        pageNumber = 1;
        totalPages = 1;
        itemsPerPage = 10;

        // add scroll view listener
        productsScrollView.getViewTreeObserver()
                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (productsScrollView.getChildAt(0).getBottom() <= (productsScrollView.getHeight() + productsScrollView.getScrollY())) {
                            if (pageNumber < totalPages) {
                                pageNumber++;
                                loadProducts();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadProducts();
    }

    private void goToProductDetailsActivity(Integer productId) {
        Intent intent = new Intent(this, ShowProductDetailsActivity.class);
        intent.putExtra("productId", productId);
        startActivity(intent);
    }

    private void loadProducts() {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                MainActivity.serverUrl + productsUrl + "?pageNumber=" + pageNumber.toString() + "&itemsPerPage=" + itemsPerPage.toString(),
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject jsonProductPage) {
                        //productsLayout.removeAllViews();
                        try {
                            final JSONArray jsonProductArray = jsonProductPage.getJSONArray("content");
                            totalPages = jsonProductPage.getInt("totalPages");

                            for (int i = 0; i < jsonProductArray.length(); i++) {
                                // get JSON product
                                final JSONObject jsonProduct = jsonProductArray.getJSONObject(i);

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
        });

        queue.add(jsonObjectRequest);
    }

    private LinearLayout createNewProductLayout(final JSONObject jsonProduct) throws JSONException {
        // image
        final byte[] byteArray = jsonProduct.getString("image").getBytes();
        final Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        final ImageView imageView = new ImageView(ShowProductsActivity.this);
        imageView.setImageBitmap(bmp);
        // name
        final TextView productNameView = new TextView(ShowProductsActivity.this);
        productNameView.setText("Denumire: " + jsonProduct.getString("name"));
        // price
        final TextView productPriceView = new TextView(ShowProductsActivity.this);
        productPriceView.setText("Preț: " + jsonProduct.getString("price"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(200, 0, 0, 0);
        productPriceView.setLayoutParams(params);

        // create a new layout for the product
        final LinearLayout productLayout = new LinearLayout(ShowProductsActivity.this);
        productLayout.setBackgroundColor(Color.parseColor("#91bbd1"));
        productLayout.setPadding(5, 5, 5, 5);
        productLayout.setGravity(CENTER_VERTICAL);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 5, 0, 5);
        layoutParams.height = 110;
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
        // populate the product layout
        productLayout.addView(imageView);
        productLayout.addView(productNameView);
        productLayout.addView(productPriceView);

        return productLayout;
    }

    public void goToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
