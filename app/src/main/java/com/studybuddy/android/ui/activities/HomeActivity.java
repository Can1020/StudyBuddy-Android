package com.studybuddy.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.studybuddy.android.data.model.Like;
import com.studybuddy.android.data.model.Match;
import com.studybuddy.android.data.model.User;
import com.studybuddy.android.ui.adapters.UserAdapter;
import com.studybuddy.android.ui.adapters.OnUserActionListener;
import com.studybuddy.android.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.userRecyclerView);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();

        userAdapter = new UserAdapter(this, userList, new OnUserActionListener() {
            @Override
            public void onLikeClicked(User user) {
                HomeActivity.this.onLikeClicked(user);
            }

            @Override
            public void onDislikeClicked(User user) {
                HomeActivity.this.onDislikeClicked(user);
            }
        });

        recyclerView.setAdapter(userAdapter);

        fetchUsers();
        fetchMatches();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true; // Stay on home
            } else if (item.getItemId() == R.id.nav_chat) {
                startActivity(new Intent(HomeActivity.this, ChatActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void fetchUsers() {
        String currentUserId = auth.getCurrentUser().getUid();
        List<String> ignoredUsers = new ArrayList<>();

        // ✅ Step 1: Fetch Liked Users
        db.collection("likes")
                .whereEqualTo("likedBy", currentUserId)
                .get()
                .addOnSuccessListener(likesSnapshot -> {
                    for (QueryDocumentSnapshot likeDoc : likesSnapshot) {
                        ignoredUsers.add(likeDoc.getString("likedUserId"));
                    }

                    // ✅ Step 2: Fetch Disliked Users
                    db.collection("dislikes")
                            .whereEqualTo("dislikedBy", currentUserId)
                            .get()
                            .addOnSuccessListener(dislikesSnapshot -> {
                                for (QueryDocumentSnapshot dislikeDoc : dislikesSnapshot) {
                                    ignoredUsers.add(dislikeDoc.getString("dislikedUserId"));
                                }

                                // ✅ Step 3: Fetch Users excluding both liked/disliked ones
                                db.collection("users")
                                        .whereNotEqualTo("id", currentUserId)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            userList.clear();
                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                User user = document.toObject(User.class);
                                                if (!ignoredUsers.contains(user.getId())) {
                                                    userList.add(user);
                                                }
                                            }
                                            userAdapter.notifyDataSetChanged();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(HomeActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                                            Log.e("Firestore", "Error fetching users: ", e);
                                        });
                            });
                });
    }


    private void onLikeClicked(User user) {
        String currentUserId = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> likeData = new HashMap<>();
        likeData.put("likedBy", currentUserId);
        likeData.put("likedUserId", user.getId());
        likeData.put("timestamp", System.currentTimeMillis());

        db.collection("likes")
                .add(likeData)
                .addOnSuccessListener(documentReference -> {
                    userList.remove(user);
                    userAdapter.notifyDataSetChanged();

                    checkMatch(user.getId());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(HomeActivity.this, "Failed to like user", Toast.LENGTH_SHORT).show()
                );
    }

    private void onDislikeClicked(User user) {
        String currentUserId = auth.getCurrentUser().getUid();

        Map<String, Object> dislikeData = new HashMap<>();
        dislikeData.put("dislikedBy", currentUserId);
        dislikeData.put("dislikedUserId", user.getId());
        dislikeData.put("timestamp", System.currentTimeMillis());

        db.collection("dislikes")
                .add(dislikeData)
                .addOnSuccessListener(documentReference -> {
                    // ✅ Remove user PERMANENTLY from the list
                    userList.remove(user);
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(HomeActivity.this, "Failed to dislike user", Toast.LENGTH_SHORT).show()
                );
    }


    private void checkMatch(String likedUserId) {
        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("likes")
                .whereEqualTo("likedUserId", currentUserId)
                .whereEqualTo("userId", likedUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d("Match", "Match found: " + currentUserId + " & " + likedUserId);
                        Toast.makeText(this, "You have a new match!", Toast.LENGTH_SHORT).show();
                        saveMatch(currentUserId, likedUserId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error checking match: " + e.getMessage(), e));
    }


    private void saveMatch(String userId, String matchedUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String matchId = db.collection("matches").document().getId();

        Match match = new Match(userId, matchedUserId, matchId, "");
        db.collection("matches")
                .document(matchId)
                .set(match)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Match saved: " + matchId);

                    // ✅ Create Chat When Match Happens
                    createChat(userId, matchedUserId);

                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving match: " + e.getMessage(), e));
    }

    private void createChat(String userId, String matchedUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String chatId = db.collection("chats").document().getId();

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("chatId", chatId);
        chatData.put("participants", Arrays.asList(userId, matchedUserId));
        chatData.put("lastMessage", "");
        chatData.put("timestamp", System.currentTimeMillis());

        db.collection("chats").document(chatId)
                .set(chatData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Chat created between " + userId + " and " + matchedUserId))
                .addOnFailureListener(e -> Log.e("Firestore", "Error creating chat: ", e));
    }

    private void fetchMatches() {
        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("matches")
                .whereArrayContains("userIds", currentUserId)
                .get()
                .addOnSuccessListener(matchesSnapshot -> {
                    List<User> matchedUsers = new ArrayList<>();

                    for (QueryDocumentSnapshot matchDoc : matchesSnapshot) {
                        String matchedUserId = matchDoc.getString("matchedUserId");

                        db.collection("users")
                                .document(matchedUserId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        try {
                                            User user = userDoc.toObject(User.class);
                                            matchedUsers.add(user);
                                        } catch (Exception e) {
                                            Log.e("Firestore", "Error parsing user: " + e.getMessage(), e);
                                        }
                                    }
                                    userList.addAll(matchedUsers);
                                    userAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching user: " + e.getMessage(), e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching matches: " + e.getMessage(), e));
    }
}
