package com.studybuddy.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.studybuddy.android.R;

public class SettingsActivity extends AppCompatActivity {

    private Button logoutButton;
    private FirebaseAuth auth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        logoutButton = findViewById(R.id.logoutButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_chat) {
                startActivity(new Intent(SettingsActivity.this, ChatActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }
}
