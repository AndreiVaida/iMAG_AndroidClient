package ro.andrei_lucian_vaida.imag;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class RegisterActivity extends AppCompatActivity {
    private final String userUrl = "/user";
    private EditText emailInput;
    private EditText nameInput;
    private EditText passwordInput;
    private TextView errorTextView;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.emailInput);
        nameInput = findViewById(R.id.nameInput);
        passwordInput = findViewById(R.id.passwordInput);
        errorTextView = findViewById(R.id.errorTextView);
        queue = Volley.newRequestQueue(this);
    }

    public void register(View view) {
        final JSONObject userRegisterJson = new JSONObject();
        try {
            userRegisterJson.put("email", emailInput.getText());
            userRegisterJson.put("name", nameInput.getText());
            userRegisterJson.put("password", passwordInput.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                MainActivity.serverUrl + userUrl + "/register",
                userRegisterJson,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        goToLoginActivity();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                errorTextView.setText("Nu s-a creat contul. \n" + error.getMessage());
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
