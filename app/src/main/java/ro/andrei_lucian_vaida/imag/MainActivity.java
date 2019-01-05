package ro.andrei_lucian_vaida.imag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    public static final String serverUrl = "http://10.0.2.2:8080";
    private Button registerButton;
    private Button wishlistButton;
    private Button loginButton;
    private Button showProductsButton;
    private Integer userId;
    private String token;
    private Animation bounceAnimation;
    private Animation fadeInAnimation;
    private Animation leftToRightAnimation;
    private Animation rightToLeftAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerButton = findViewById(R.id.registerButton);
        wishlistButton = findViewById(R.id.wishlistButton);
        loginButton = findViewById(R.id.loginButton);
        showProductsButton = findViewById(R.id.showProductsButton);
        bounceAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein);
        leftToRightAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.lefttoright);
        rightToLeftAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.righttoleft);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateLoginButton();
        playAnimations();
    }

    private void playAnimations() {
        registerButton.startAnimation(leftToRightAnimation);
        loginButton.startAnimation(rightToLeftAnimation);
        wishlistButton.startAnimation(fadeInAnimation);
        showProductsButton.startAnimation(bounceAnimation);
    }

    private void updateLoginButton() {
        getSharedPreferences();
        if (isLoggedIn()) {
            loginButton.setText("Logout");
        } else {
            loginButton.setText("Login");
        }
    }

    private boolean isLoggedIn() {
        return userId > 0 && token.length() > 0;
    }

    public void goToShowProductsActivity(View view) {
        Intent intent = new Intent(this, ShowProductsActivity.class);
        startActivity(intent);
    }

    public void loginOrLogout(View view) {
        if (isLoggedIn()) {
            logout();
            loginButton.setText("Login");
        } else {
            goToLoginActivity();
        }
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("security", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("userId");
        editor.remove("token");
        editor.apply();
    }

    public void goToLoginActivity() {
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

    private void getSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("security", Context.MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        token = prefs.getString("token", "");
    }
}
