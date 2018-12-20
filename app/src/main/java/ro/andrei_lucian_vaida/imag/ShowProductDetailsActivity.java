package ro.andrei_lucian_vaida.imag;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class ShowProductDetailsActivity extends AppCompatActivity {
    private Integer productId;
    private TextView productNameView;
    private TextView productPriceView;
    private TextView productDetailsView;
    private final String productsUrl = "/product";
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_product_details);
        productNameView = findViewById(R.id.productNameView);
        productPriceView = findViewById(R.id.productPriceView);
        productDetailsView = findViewById(R.id.productDetailsView);

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
                MainActivity.serverUrl + productsUrl + "/" + productId,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            productNameView.setText(response.getString("name"));
                            productPriceView.setText(response.getString("price") + " lei");
                            productDetailsView.setText(response.getString("details"));
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
}
