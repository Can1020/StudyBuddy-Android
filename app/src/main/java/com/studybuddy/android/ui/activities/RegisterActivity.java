package com.studybuddy.android.ui.activities;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.studybuddy.android.R;
public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText emailInput, passwordInput;
    private Button registerButton;
    private TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerButton = findViewById(R.id.registerButton);
        loginRedirect = findViewById(R.id.loginRedirect);

        registerButton.setOnClickListener(v -> registerUser());
        loginRedirect.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    private void registerUser() {
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
