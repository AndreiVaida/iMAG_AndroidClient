package ro.andrei_lucian_vaida.imag;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class ShowProductDetailsActivity extends AppCompatActivity {
    private Integer productId;
    private TextView productNameView;
    private TextView productPriceView;
    private TextView productDetailsView;
    private ImageView productImageView;
    private final String productUrl = "/product";
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_product_details);
        productNameView = findViewById(R.id.productNameView);
        productPriceView = findViewById(R.id.productPriceView);
        productDetailsView = findViewById(R.id.productDetailsView);
        productImageView = findViewById(R.id.productImageView);

        queue = Volley.newRequestQueue(this);
        final Intent intent = getIntent();
        productId = intent.getIntExtra("productId", -1);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadProductDetails(productId);
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
}
