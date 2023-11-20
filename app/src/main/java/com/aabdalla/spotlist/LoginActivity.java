package com.aabdalla.spotlist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
//SHOULD BE THE BASIS OF THE APPLICATION

    private FirebaseAuth auth;

    private EditText emailField, passwordField;
    private Button signInButton;
    private TextView registerButton;

    private TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(getApplicationContext());
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            user.reload();
            if (auth.getCurrentUser() != null) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                return;
            }
        }

        emailField = findViewById(R.id.field_email);
        passwordField = findViewById(R.id.field_password);
        signInButton = findViewById(R.id.button_login);
        registerButton = findViewById(R.id.button_register_new);

        error = findViewById(R.id.error);
        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            finish();
        });

        signInButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                notifyError("Complete details");
                return;
            }
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            notifyError("Email and password do not match");
                        }
                    });
        });
    }

    private void notifyError(String error) {
        if (this.error.getVisibility() == View.INVISIBLE) {
            this.error.setVisibility(View.VISIBLE);
        }
        this.error.setText(error);
    }
}