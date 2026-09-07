package com.example.udomgo;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect this Activity to the Login screen
        setContentView(R.layout.activity_login);

        // =========================
        // FIND UI COMPONENTS
        // =========================

        EditText emailField = findViewById(R.id.emailField);
        EditText passwordField = findViewById(R.id.passwordField);

        ImageButton passwordEye = findViewById(R.id.passwordEye);

        Button loginButton = findViewById(R.id.loginButton);


        // =========================
        // PASSWORD VISIBILITY
        // =========================

        passwordEye.setOnClickListener(v -> {

            if (passwordField.getInputType()
                    == (InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

                passwordField.setInputType(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                );

            } else {

                passwordField.setInputType(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_PASSWORD
                );
            }

            // Keep cursor at the end of the password
            passwordField.setSelection(passwordField.length());
        });


        // =========================
        // LOGIN BUTTON
        // =========================

        loginButton.setOnClickListener(v -> {

            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();


            // Check if email is empty
            if (email.isEmpty()) {

                emailField.setError("Enter your email");
                emailField.requestFocus();
                return;
            }


            // Check if password is empty
            if (password.isEmpty()) {

                passwordField.setError("Enter your password");
                passwordField.requestFocus();
                return;
            }


            // Temporary success message
            Toast.makeText(
                    LoginActivity.this,
                    "Login successful",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent =
                    new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        Button createAccountButton =
                findViewById(R.id.createAccountButton);

        createAccountButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(LoginActivity.this, RegisterActivity.class);

            startActivity(intent);
        });

    }
}