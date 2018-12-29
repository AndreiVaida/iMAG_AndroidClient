package ro.andrei_lucian_vaida.imag;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public static final String serverUrl = "http://10.0.2.2:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToShowProductsActivity(View view) {
        Intent intent = new Intent(this, ShowProductsActivity.class);
        startActivity(intent);
    }

    public void goToLoginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void goToRegisterActivity(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void goToWishlistActivity(View view) {
        Intent intent = new Intent(this, WishlistActivity.class);
        startActivity(intent);
    }
}
