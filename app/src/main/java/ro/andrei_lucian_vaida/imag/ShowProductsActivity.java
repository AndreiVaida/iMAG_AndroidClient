package ro.andrei_lucian_vaida.imag;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

public class ShowProductsActivity extends AppCompatActivity {
    private final String productsUrl = "/product";
    private RequestQueue queue;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_products);
        title = findViewById(R.id.title);
        System.out.println();
        System.out.println("onCreate");
        System.out.println();
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println();
        System.out.println("onStart");
        System.out.println();
        queue = Volley.newRequestQueue(this);
        loadProducts();
    }

    private void loadProducts() {
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(MainActivity.serverUrl + productsUrl,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        title.setText("Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        title.setText("Error: " + error.toString());
                    }
                });

        // Access the RequestQueue through your singleton class.
        queue.add(jsonObjectRequest);
    }
}
