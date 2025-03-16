package com.studybuddy.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.studybuddy.android.R;
import com.studybuddy.android.data.model.User;
import com.studybuddy.android.data.database.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseHelper dbHelper;
    private EditText emailInput, passwordInput, nameInput, ageInput, locationInput, universityInput,
            courseInput, semesterInput, skillsInput, passwordConfirmInput;
    private Button registerButton;
    private TextView loginRedirect;
    private ImageView backArrow, nextArrow;


    private int currentStep = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStepContentView(currentStep);

        auth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper();

        backArrow = findViewById(R.id.backArrow);
        nextArrow = findViewById(R.id.nextArrow);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);


        backArrow.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--;
                setStepContentView(currentStep);
            } else {
                finish();
            }
        });

        nextArrow.setOnClickListener(v -> {
            if (currentStep < 9) {
                currentStep++;
                setStepContentView(currentStep);
            } else {
                registerUser();
            }
        });

        if (loginRedirect != null) {
            loginRedirect.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }
    }

    private void setStepContentView(int step) {
        switch (step) {
            case 1:
                setContentView(R.layout.activity_register_step1);
                nameInput = findViewById(R.id.nameInput);
                break;
            case 2:
                setContentView(R.layout.activity_register_step2);
                ageInput = findViewById(R.id.ageInput);
                break;
            case 3:
                setContentView(R.layout.activity_register_step3);
                locationInput = findViewById(R.id.locationInput);
                break;
            case 4:
                setContentView(R.layout.activity_register_step4);
                universityInput = findViewById(R.id.universityInput);
                break;
            case 5:
                setContentView(R.layout.activity_register_step5);
                courseInput = findViewById(R.id.courseInput);
                break;
            case 6:
                setContentView(R.layout.activity_register_step6);
                semesterInput = findViewById(R.id.semesterInput);
                break;
            case 7:
                setContentView(R.layout.activity_register_step7);
                skillsInput = findViewById(R.id.skillsInput);
                break;
            case 8:
                setContentView(R.layout.activity_register_step8);
                emailInput = findViewById(R.id.emailInput);
                break;
            case 9:
                setContentView(R.layout.activity_register_step9);
                passwordInput = findViewById(R.id.passwordInput);
                passwordConfirmInput = findViewById(R.id.passwordConfirmInput);
                nextArrow = findViewById(R.id.nextArrow);
                nextArrow.setOnClickListener(v -> registerUser());
                break;
        }

        backArrow = findViewById(R.id.backArrow);
        nextArrow = findViewById(R.id.nextArrow);
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> {
                if (currentStep > 1) {
                    currentStep--;
                    setStepContentView(currentStep);
                } else {
                    finish();
                }
            });
        }
        if (nextArrow != null) {
            nextArrow.setOnClickListener(v -> {
                if (currentStep < 9) {
                    currentStep++;
                    setStepContentView(currentStep);
                } else {
                    registerUser();
                }
            });
        }
    }

    private void registerUser() {
        if (emailInput == null || passwordInput == null) {
            Toast.makeText(this, "Please complete all steps first", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = passwordConfirmInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String ageText = ageInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String university = universityInput.getText().toString().trim();
        String course = courseInput.getText().toString().trim();
        String semester = semesterInput.getText().toString().trim();
        String skills = skillsInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty() || ageText.isEmpty() ||
                location.isEmpty() || university.isEmpty() || course.isEmpty() || semester.isEmpty() || skills.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age format", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();
                        User user = new User(userId, name, age, location, university, course, semester, skills);

                        dbHelper.saveUserToDatabase(user, new DatabaseHelper.OnUserSavedListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(RegisterActivity.this, "Database Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
