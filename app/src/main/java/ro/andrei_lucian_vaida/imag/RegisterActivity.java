package ro.andrei_lucian_vaida.imag;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    private final String userUrl = "/user";
    private EditText emailInput;
    private EditText nameInput;
    private EditText passwordInput;
    private static EditText datePickerInput;
    private TextView errorTextView;
    private RequestQueue queue;

    public static void setDate(int year, int month, int day) {
        datePickerInput.setText(year + "." + month + "." + day);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.emailInput);
        nameInput = findViewById(R.id.nameInput);
        passwordInput = findViewById(R.id.passwordInput);
        errorTextView = findViewById(R.id.errorTextView);
        datePickerInput = findViewById(R.id.datePickerEditText);
        queue = Volley.newRequestQueue(this);
    }

    public void register(View view) {
        final JSONObject body = new JSONObject();
        try {
            body.put("email", emailInput.getText());
            body.put("name", nameInput.getText());
            body.put("password", passwordInput.getText());
            body.put("birthDay", datePickerInput.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        final StringRequest stringRequest = new StringRequest(Request.Method.POST,
                MainActivity.serverUrl + userUrl + "/register",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        goToLoginActivity();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    errorTextView.setText(new String(response.data));
                }
                else {
                    errorTextView.setText("A apărut o eroare.");
                }
            }
        }) {
            // save body
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        queue.add(stringRequest);
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void goToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
}
