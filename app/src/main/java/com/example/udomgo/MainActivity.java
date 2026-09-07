package com.example.udomgo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.udomgo.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        new Handler().postDelayed(() -> {

            Intent intent =
                    new Intent(MainActivity.this, LoginActivity.class);

            startActivity(intent);

            finish();

        }, 2500);
    }
}