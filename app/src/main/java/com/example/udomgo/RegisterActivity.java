package com.example.udomgo;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);


        // =========================
        // FIND UI COMPONENTS
        // =========================

        EditText nameField =
                findViewById(R.id.nameField);

        EditText emailField =
                findViewById(R.id.emailField);

        EditText passwordField =
                findViewById(R.id.passwordField);

        EditText confirmPasswordField =
                findViewById(R.id.confirmPasswordField);

        ImageButton passwordEye =
                findViewById(R.id.passwordEye);

        ImageButton confirmPasswordEye =
                findViewById(R.id.confirmPasswordEye);

        Button registerButton =
                findViewById(R.id.registerButton);

        TextView backToLogin =
                findViewById(R.id.backToLogin);


        // =========================
        // PASSWORD EYE
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

            passwordField.setSelection(
                    passwordField.length()
            );
        });


        // =========================
        // CONFIRM PASSWORD EYE
        // =========================

        confirmPasswordEye.setOnClickListener(v -> {

            if (confirmPasswordField.getInputType()
                    == (InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

                confirmPasswordField.setInputType(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                );

            } else {

                confirmPasswordField.setInputType(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_PASSWORD
                );
            }

            confirmPasswordField.setSelection(
                    confirmPasswordField.length()
            );
        });


        // =========================
        // CREATE ACCOUNT
        // =========================

        registerButton.setOnClickListener(v -> {

            String name =
                    nameField.getText().toString().trim();

            String email =
                    emailField.getText().toString().trim();

            String password =
                    passwordField.getText().toString();

            String confirmPassword =
                    confirmPasswordField.getText().toString();


            // =========================
            // NAME
            // =========================

            if (name.isEmpty()) {

                nameField.setError(
                        "Enter your name"
                );

                nameField.requestFocus();
                return;
            }


            // =========================
            // EMAIL
            // =========================

            if (email.isEmpty()) {

                emailField.setError(
                        "Enter your email"
                );

                emailField.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()) {

                emailField.setError(
                        "Enter a valid email"
                );

                emailField.requestFocus();
                return;
            }


            // =========================
            // PASSWORD
            // =========================

            if (password.isEmpty()) {

                passwordField.setError(
                        "Enter a password"
                );

                passwordField.requestFocus();
                return;
            }

            if (password.length() < 5) {

                passwordField.setError(
                        "Password must be at least 5 characters"
                );

                passwordField.requestFocus();
                return;
            }


            // =========================
            // CONFIRM PASSWORD
            // =========================

            if (confirmPassword.isEmpty()) {

                confirmPasswordField.setError(
                        "Confirm your password"
                );

                confirmPasswordField.requestFocus();
                return;
            }


            // =========================
            // PASSWORD MATCH
            // =========================

            if (!password.equals(confirmPassword)) {

                confirmPasswordField.setError(
                        "Passwords do not match"
                );

                confirmPasswordField.requestFocus();
                return;
            }


            // =========================
            // CHECK EXISTING ACCOUNT & REGISTER
            // =========================

            if (UserPreferences.hasAccount(RegisterActivity.this, email)) {
                emailField.setError("An account with this email already exists");
                emailField.requestFocus();
                return;
            }

            // Save new account locally
            UserPreferences.registerUser(RegisterActivity.this, name, email, password);

            // Establish active session
            UserPreferences.setLoggedInSession(RegisterActivity.this, email, name);

            android.widget.Toast.makeText(
                    RegisterActivity.this,
                    "Account created! Welcome, " + name,
                    android.widget.Toast.LENGTH_SHORT
            ).show();

            // =========================
            // OPEN HOME
            // =========================

            Intent intent =
                    new Intent(
                            RegisterActivity.this,
                            HomeActivity.class
                    );

            startActivity(intent);

            // Prevent returning to registration
            finish();
        });


        // =========================
        // BACK TO LOGIN
        // =========================

        backToLogin.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            RegisterActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);

            finish();
        });
    }
}