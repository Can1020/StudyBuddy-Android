package com.studybuddy.android.data.database;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.studybuddy.android.data.model.User;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public DatabaseHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void saveUserToDatabase(User user, OnUserSavedListener listener) {
        db.collection(USERS_COLLECTION).document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User saved successfully.");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }

    public void getUser(String userId, OnUserLoadedListener listener) {
        db.collection(USERS_COLLECTION).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        listener.onSuccess(user);
                    } else {
                        listener.onFailure("User not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }

    public interface OnUserSavedListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface OnUserLoadedListener {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }
}
