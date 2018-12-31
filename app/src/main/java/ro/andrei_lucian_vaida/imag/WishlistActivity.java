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
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.IMagDatabase;
import domain.Product;
import domain.TaskToDoWhenOnline;

import static android.view.Gravity.CENTER_VERTICAL;

public class WishlistActivity extends AppCompatActivity {
    private final String wishlistUrl = "/user/wishlist";
    private static final String DATABASE_NAME = "iMag_db";
    private dao.IMagDatabase IMagDatabase;
    private boolean weAreOnline;
    private RequestQueue queue;
    private LinearLayout productsLayout;
    private TextView titleView;
    private TextView statusTextView;
    private Integer userId;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        productsLayout = findViewById(R.id.productsLayout);
        titleView = findViewById(R.id.titleView);
        statusTextView = findViewById(R.id.statusTextView);
        queue = Volley.newRequestQueue(this);

        getSharedPreferences();
        if (userId < 0 || token.length() == 0) {
            goToLoginActivity();
            return;
        }

        IMagDatabase = Room.databaseBuilder(getApplicationContext(),
                IMagDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        weAreOnline = true;
        statusTextView.setText("");

        titleView.setText("Produse.");
        titleView.setTextColor(Color.rgb(0, 0, 0));
        loadWishlist(userId);
        tryToSynchronizeWithServer(0);
    }

    private void loadWishlist(final Integer userId) {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                MainActivity.serverUrl + wishlistUrl,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        weAreOnline = true;
                        try {
                            JSONArray jsonProducts = response.getJSONArray("productDtos");
                            productsLayout.removeAllViews();

                            for (int i = 0; i < jsonProducts.length(); i++) {

                                // get JSON product
                                final JSONObject jsonProduct = jsonProducts.getJSONObject(i);

                                // save the product in local storage
                                final Product product = jsonToProduct(jsonProduct, true);
                                if (product != null) {
                                    saveInLocalStorage(product);

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
                weAreOnline = false;
                titleView.setText("Conexiune eșuată.\nSe afișează produsele salvate local.");
                titleView.setTextColor(Color.rgb(232, 73, 30));
                loadWishlistFromLocalStorage();
            }
        }) {
            // save header
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

    private void loadWishlistFromLocalStorage() {
        productsLayout.removeAllViews();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Product> products = IMagDatabase.productDao().getAllInWishlist();
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

    private void getSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("security", Context.MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        token = prefs.getString("token", "");
    }

    private LinearLayout createNewProductLayout(final Product product) {
        // image
        final byte[] byteArray = product.getImage().getBytes();
        final Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        final ImageView imageView = new ImageView(WishlistActivity.this);
        imageView.setImageBitmap(bmp);
        // name
        final TextView productNameView = new TextView(WishlistActivity.this);
        productNameView.setText("Denumire: " + product.getName());
        // price
        final TextView productPriceView = new TextView(WishlistActivity.this);
        productPriceView.setText("Preț: " + product.getPrice());
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
                final Integer productId = product.getId();
                goToProductDetailsActivity(productId);
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
                removeProductFromWishlist(product);
            }
        });

        // populate the product layout
        productLayout.addView(imageView);
        productLayout.addView(productNameView);
        productLayout.addView(productPriceView);
        productLayout.addView(removeProductButton);

        return productLayout;
    }

    private void removeProductFromWishlist(final Product product) {
        if (!weAreOnline) {
            removeProductFromWishlistOffline(product);
            return;
        }

        final StringRequest stringRequest = new StringRequest(Request.Method.DELETE,
                MainActivity.serverUrl + wishlistUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        weAreOnline = true;
                        statusTextView.setText("Produsul a fost eliminat din wishlist.");
                        statusTextView.setTextColor(Color.rgb(37, 178, 41));

                        product.setInWishlist(false);
                        saveInLocalStorage(product);

                        loadWishlist(userId);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    weAreOnline = false;
                    statusTextView.setText("Produsul va fi șters complet după restabilirea conexiunii.");
                    statusTextView.setTextColor(Color.rgb(208, 219, 61));
                    removeProductFromWishlistOffline(product);
                } else {
                    final NetworkResponse response = error.networkResponse;
                    if (response.data != null) {
                        statusTextView.setText(new String(response.data));
                    }
                    else {
                        statusTextView.setText("Produsul nu a putut fi eliminat din wishlist.");
                    }
                    statusTextView.setTextColor(Color.rgb(232, 73, 30));
                    error.printStackTrace();
                }
            }
        }) {
            // add header
            @Override
            public Map<String, String> getHeaders() {
                final HashMap<String, String> headers = new HashMap<>();
                //headers.put("Content-Type", "application/json");
                headers.put("userId", userId.toString());
                headers.put("productId", product.getId().toString());
                headers.put("token", token);
                return headers;
            }
        };
        queue.add(stringRequest);
    }

    private void removeProductFromWishlistOffline(final Product product) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                product.setInWishlist(false);
                IMagDatabase.productDao().save(product);
                final TaskToDoWhenOnline taskToDoWhenOnline = new TaskToDoWhenOnline(product.getId(), false);
                IMagDatabase.taskToDoWhenOnlineDao().save(taskToDoWhenOnline);

                final Handler handler = new Handler(Looper.getMainLooper());
                final Runnable runnable = new Runnable() {
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                loadWishlistFromLocalStorage();
                            }
                        });
                    }
                };
                new Thread(runnable).start();

            }
        }).start();
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

    private Product jsonToProduct(final JSONObject jsonProduct, final boolean isInWishlist) {
        try {
            final Integer id = jsonProduct.getInt("id");
            final String name = jsonProduct.getString("name");
            final Integer price = jsonProduct.getInt("price");
            final String details = jsonProduct.getString("details");
            final String image = jsonProduct.getString("image");
            final Product product = new Product(id, name, price, details, image);
            product.setInWishlist(isInWishlist);
            return product;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveInLocalStorage(final Product product) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IMagDatabase.productDao().save(product);
            }
        }).start();
    }

    /**
     * Trying to send to server the first task from local storage (if DB is not empty), after nrOfSecondsToWait seconds.
     * If succeeds: remove the task from local storage and try to send the next one
     * If fails: try again after nrOfSecondsToWait seconds
     * Info: Updates weAreOnline variable.
     */
    private void tryToSynchronizeWithServer(final int nrOfSecondsToWait) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(nrOfSecondsToWait * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                final List<TaskToDoWhenOnline> taskToDoWhenOnlineList = IMagDatabase.taskToDoWhenOnlineDao().getAll();
                if (taskToDoWhenOnlineList.size() > 0) {
                    final TaskToDoWhenOnline task = taskToDoWhenOnlineList.get(0);
                    System.out.println(" -> Task to do: " + taskToDoWhenOnlineList.size() + ".\n -> Try to synchronize product " + task.getProductId() + " " + task.isInWishlist() + ".");
                    final int method;
                    if (task.isInWishlist()) {
                        method = Request.Method.POST;
                    } else {
                        method = Request.Method.DELETE;
                    }

                    final StringRequest stringRequest = new StringRequest(method,
                            MainActivity.serverUrl + wishlistUrl,
                            new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {
                                    weAreOnline = true;
                                    removeTaskFromLocalStorage(task);
                                    if (taskToDoWhenOnlineList.size() == 1) {
                                        loadWishlist(userId);
                                    } else {
                                        tryToSynchronizeWithServer(0);
                                    }
                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                                // cannot connect to server
                                weAreOnline = false;
                                tryToSynchronizeWithServer(10);
                            } else {
                                final NetworkResponse response = error.networkResponse;
                                if (response.data != null) {
                                    statusTextView.setText(new String(response.data));
                                }
                                // server replayed with an error
                                removeTaskFromLocalStorage(task);
                                tryToSynchronizeWithServer(0);
                            }
                        }
                    }) {
                        // add header
                        @Override
                        public Map<String, String> getHeaders() {
                            final HashMap<String, String> headers = new HashMap<>();
                            //headers.put("Content-Type", "application/json");
                            headers.put("userId", userId.toString());
                            headers.put("productId", task.getProductId().toString());
                            headers.put("token", token);
                            return headers;
                        }
                    };
                    queue.add(stringRequest);
                }
            }
        }).start();
    }

    private void removeTaskFromLocalStorage(final TaskToDoWhenOnline taskToDoWhenOnline) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IMagDatabase.taskToDoWhenOnlineDao().delete(taskToDoWhenOnline);
            }
        }).start();
    }
}
