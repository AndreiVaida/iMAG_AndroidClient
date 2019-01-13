package ro.andrei_lucian_vaida.imag;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class SendEmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email);
    }

    public void sendEmail(View view) {
        String[] TO = {"andrei_vd2006@yahoo.com"};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact iMAG");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Mesajul tău aici.");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            System.out.println("Finished sending email...");
            //Toast.makeText(SendEmailActivity.this, "E-mail trimis cu succes.", Toast.LENGTH_SHORT).show();
        } catch (android.content.ActivityNotFoundException ex) {
            System.out.println("ERROR: " + ex.toString());
            Toast.makeText(SendEmailActivity.this, "A apărut o eroare.", Toast.LENGTH_SHORT).show();
        }
    }
}
