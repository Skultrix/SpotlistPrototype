package com.aabdalla.spotlist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText emailField, usernameField, passwordField, confirmPasswordField;
    private Button registerButton;
    private TextView loginButton;
    private TextView error;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Check if user should even be here
        setContentView(R.layout.activity_registration);

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        emailField = findViewById(R.id.field_email);
        usernameField = findViewById(R.id.field_username);
        passwordField = findViewById(R.id.field_password);
        confirmPasswordField = findViewById(R.id.field_confirmation);

        registerButton = findViewById(R.id.button_create);
        loginButton = findViewById(R.id.text_login);

        error = findViewById(R.id.error);
        progressBar = findViewById(R.id.progress);

        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        registerButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirmation = confirmPasswordField.getText().toString().trim();

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmation.isEmpty()) {
                notifyError("All fields must be entered.");
                return;
            }

            if (password.length() < 6) {
                notifyError("Password must be >= 6 characters.");
                return;
            }

            if (username.length() < 4) {
                notifyError("Username must be >= 4 characters.");
                return;
            }

            if (!password.equals(confirmation)) {
                notifyError("Passwords do not match.");
                return;
            }

            //Check if email is already in use

            //Check if username is taken
            DocumentReference ref = db.collection("users").document(username);

            ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        notifyError("Username In Use");
                    } else {
                        attemptAccountCreation(email, password, username);
                    }
                } else {
                    notifyError("Unsuccessful DB Connection");
                }
            });

        });
    }

    private void attemptAccountCreation(String email, String password, String username) {
        progressBar.setVisibility(View.VISIBLE);
        error.setVisibility(View.INVISIBLE);

        loginButton.postDelayed(() -> {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Map<String, Object> user = new HashMap<>();

                            user.put("username", username);
                            user.put("email", email);
                            user.put("points", 0);
                            user.put("uid", task.getResult().getUser().getUid());
                            user.put("reservation", -1);
                            user.put("show", Arrays.asList(0, 1, 2));

                            db.collection("users")
                                    .document(username)
                                    .set(user)
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "There is an issue!");
                                    });

                            auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            finish();
                                        } else {
                                            notifyError("Unable to login");
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                        } else {
                            Exception exception = task.getException();
                            assert exception != null;
                            Toast.makeText(this, "Authentication failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            notifyError("Please check entered details");
                        }
                    });
        }, 3000L);

    }

    private void notifyError(String error) {
        if (this.error.getVisibility() == View.INVISIBLE) {
            this.error.setVisibility(View.VISIBLE);
        }
        this.error.setText(error);
    }
}