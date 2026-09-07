package com.example.udomgo;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Route immediately without artificial delay:
        // Android's system splash screen displays the app logo during startup,
        // and this transitions seamlessly straight to the active screen.
        Intent intent;
        if (UserPreferences.isLoggedIn(this)) {
            intent = new Intent(this, HomeActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}