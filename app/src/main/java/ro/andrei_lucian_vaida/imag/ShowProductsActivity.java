package ro.andrei_lucian_vaida.imag;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.view.Gravity.CENTER_VERTICAL;

public class ShowProductsActivity extends AppCompatActivity {
    private final String productsUrl = "/product";
    private RequestQueue queue;
    private TextView title;
    private LinearLayout productsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_products);
        // initialize components from XML
        title = findViewById(R.id.title);
        productsLayout = findViewById(R.id.productslayout);
        queue = Volley.newRequestQueue(this);
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
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(MainActivity.serverUrl + productsUrl,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        productsLayout.removeAllViews();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                // get JSON product
                                final JSONObject jsonProduct = response.getJSONObject(i);

                                // create a new layout for the product
                                final LinearLayout productLayout = createNewProductLayout(jsonProduct);

                                // add the product view to the page
                                productsLayout.addView(productLayout);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        title.setText("Error: " + error.toString());
                    }
                });

        queue.add(jsonArrayRequest);
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
        layoutParams.height = 100;
        productLayout.setLayoutParams(layoutParams);
        productLayout.setOrientation(LinearLayout.HORIZONTAL);
        productLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final Integer productId = jsonProduct.getInt("id");
                    goToProductDetailsActivity(productId);
                }
                catch (JSONException e) {
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
