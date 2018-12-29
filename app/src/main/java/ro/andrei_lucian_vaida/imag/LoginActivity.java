package ro.andrei_lucian_vaida.imag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private final String userUrl = "/user";
    private EditText emailInput;
    private EditText passwordInput;
    private TextView errorTextView;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        errorTextView = findViewById(R.id.errorTextView);
        queue = Volley.newRequestQueue(this);
    }

    public void login(View view) {
        final JSONObject userLoginJson = new JSONObject();
        try {
            userLoginJson.put("email", emailInput.getText());
            userLoginJson.put("password", passwordInput.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                MainActivity.serverUrl + userUrl + "/login",
                userLoginJson,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final String token = response.getString("token");
                            final Integer userId = response.getInt("userId");
                            saveSharedPreferences(token, userId);
                            goToWishlistActivity();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                errorTextView.setText("E-mail sau parolă greșită.");
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void saveSharedPreferences(final String token, Integer userId) {
        SharedPreferences prefs = getSharedPreferences("security", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", token);
        editor.putInt("userId", userId);
        editor.commit();
    }

    private void goToWishlistActivity() {
        Intent intent = new Intent(this, WishlistActivity.class);
        startActivity(intent);
    }
}
