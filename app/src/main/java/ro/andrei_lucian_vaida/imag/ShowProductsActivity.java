package ro.andrei_lucian_vaida.imag;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import dao.IMagDatabase;
import domain.Product;

import static android.view.Gravity.CENTER_VERTICAL;

public class ShowProductsActivity extends AppCompatActivity {
    private final String productsUrl = "/product";
    private static final String DATABASE_NAME = "iMag_db";
    private IMagDatabase IMagDatabase;
    private RequestQueue queue;
    private TextView title;
    private ScrollView productsScrollView;
    private LinearLayout productsLayout;
    private Integer pageNumber;
    private Integer totalPages;
    private Integer itemsPerPage;
    private boolean productsAreLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_products);
        // initialize components from XML
        title = findViewById(R.id.title);
        productsScrollView = findViewById(R.id.productsScrollView);
        productsLayout = findViewById(R.id.productsLayout);
        productsLayout.removeAllViews();
        queue = Volley.newRequestQueue(this);
        pageNumber = 1;
        totalPages = 1;
        itemsPerPage = 10;

        // save scroll view listener
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
        productsAreLoaded = false;


        IMagDatabase = Room.databaseBuilder(getApplicationContext(),
                IMagDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!productsAreLoaded) {
            loadProducts();
        }
        productsAreLoaded = true;
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
                        // clear local storage
                        if (pageNumber == 1) {
                            clearLocalStorage();
                        }

                        title.setText("Produse");
                        title.setTextColor(Color.rgb(0, 0, 0));
                        //productsLayout.removeAllViews();
                        try {
                            final JSONArray jsonProductArray = jsonProductPage.getJSONArray("content");
                            totalPages = jsonProductPage.getInt("totalPages");

                            for (int i = 0; i < jsonProductArray.length(); i++) {
                                // get JSON product
                                final JSONObject jsonProduct = jsonProductArray.getJSONObject(i);

                                // save the product in local storage
                                final Product product = saveInLocalStorage(jsonProduct);

                                if (product != null) {
                                    // create a new layout for the product
                                    final LinearLayout productLayout = createNewProductLayout(product);
                                    // save the product view to the page
                                    productsLayout.addView(productLayout);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                title.setText("Conexiune eșuată.\nSe afișează produsele salvate local.");
                title.setTextColor(Color.rgb(232, 73, 30));
                if (pageNumber == 1) {
                    loadProductsFromLocalStorage();
                }
            }
        });

        queue.add(jsonObjectRequest);
    }

    private void clearLocalStorage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IMagDatabase.productDao().deleteAllNotInWishlist();
            }
        }).start();
    }

    private Product saveInLocalStorage(final JSONObject jsonProduct) {
        try {
            final Integer id = jsonProduct.getInt("id");
            final String name = jsonProduct.getString("name");
            final Integer price = jsonProduct.getInt("price");
            final String details = jsonProduct.getString("details");
            final String image = jsonProduct.getString("image");
            final Product product = new Product(id, name, price, details, image);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    IMagDatabase.productDao().save(product);
                }
            }).start();

            return product;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadProductsFromLocalStorage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Product> products = IMagDatabase.productDao().getAll();
                for (Product product : products) {
                    // create a new layout for the product
                    final LinearLayout productLayout = createNewProductLayout(product);

                    // save the product view to the page
                    final Handler handler = new Handler(Looper.getMainLooper());
                    final Runnable runnable = new Runnable() {
                        public void run() {
                            handler.post(new Runnable() {
                                public void run() {
                                    productsLayout.addView(productLayout);
                                }
                            });
                        }
                    };
                    new Thread(runnable).start();
                }
            }
        }).start();
    }

    private LinearLayout createNewProductLayout(final Product product) {
        // image
        final byte[] byteArray = product.getImage().getBytes();
        final Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        final ImageView imageView = new ImageView(ShowProductsActivity.this);
        imageView.setImageBitmap(bmp);
        // name
        final TextView productNameView = new TextView(ShowProductsActivity.this);
        productNameView.setText("Denumire: " + product.getName());
        // price
        final TextView productPriceView = new TextView(ShowProductsActivity.this);
        productPriceView.setText("Preț: " + product.getPrice());
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
                final Integer productId = product.getId();
                goToProductDetailsActivity(productId);
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
