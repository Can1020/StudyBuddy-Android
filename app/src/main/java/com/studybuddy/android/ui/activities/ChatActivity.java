// ChatActivity - Hauptklasse für die Chat-Übersicht
// Zeigt die aktiven Chats des Benutzers und ermöglicht den Zugriff auf Chatrooms.
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.studybuddy.android.R;
import com.studybuddy.android.data.model.Match;
import com.studybuddy.android.data.model.Chat;
import com.studybuddy.android.ui.adapters.MatchAdapter;
import com.studybuddy.android.ui.adapters.ChatAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView matchesRecyclerView, chatsRecyclerView;
    private MatchAdapter matchAdapter;
    private ChatAdapter chatAdapter;
    private List<Match> matchList;
    private List<Chat> chatList;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        matchesRecyclerView = findViewById(R.id.matchesRecyclerView);
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        matchesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        matchList = new ArrayList<>();
        matchAdapter = new MatchAdapter(matchList, this::openChat);
        matchesRecyclerView.setAdapter(matchAdapter);

        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatList);
        chatsRecyclerView.setAdapter(chatAdapter);

        loadMatches();
        loadChats();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_chat);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(ChatActivity.this, HomeActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_chat) {
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(ChatActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    // Lädt alle Matches des aktuellen Benutzers aus Firestore
    private void loadMatches() {
        String userId = auth.getCurrentUser().getUid();
        Log.d("Firestore", "Fetching matches for user: " + userId);

        db.collection("matches")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    matchList.clear();
                    if (querySnapshot.isEmpty()) {
                        Log.d("Firestore", "No matches found for user: " + userId);
                    }
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Match match = document.toObject(Match.class);
                        Log.d("Firestore", "Match found: " + match.getMatchedUserId());

                        if (match.getMatchedUserId() != null) {
                            matchList.add(match);
                        }
                    }
                    matchAdapter.notifyDataSetChanged();
                    Log.d("Firestore", "Total matches loaded: " + matchList.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load matches", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error fetching matches: ", e);
                });
    }

    // Lädt alle Chats des aktuellen Benutzers aus Firestore
    private void loadChats() {
        String userId = auth.getCurrentUser().getUid();
        Log.d("Firestore", "Fetching chats for user: " + userId);

        db.collection("chats")
                .whereArrayContains("participants", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    chatList.clear();
                    if (querySnapshot.isEmpty()) {
                        Log.d("Firestore", "No chats found for user: " + userId);
                    }
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Chat chat = document.toObject(Chat.class);
                        chat.setChatId(document.getId());

                        List<String> participants = (List<String>) document.get("participants");
                        if (participants != null) {
                            for (String participant : participants) {
                                if (!participant.equals(userId)) {
                                    fetchUsername(participant, chat, document.getId());
                                    break;
                                }
                            }
                        }

                        if (document.contains("lastMessage")) {
                            chat.setLastMessage(document.getString("lastMessage"));
                            Log.d("Firestore", "Last message loaded: " + document.getString("lastMessage"));
                        } else {
                            chat.setLastMessage("No messages yet");
                        }

                        chatList.add(chat);
                    }
                    chatAdapter.notifyDataSetChanged();
                    Log.d("Firestore", "Total chats loaded: " + chatList.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load chats", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error fetching chats: ", e);
                });
    }


    private void fetchUsername(String userId, Chat chat, String chatId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("name");
                        if (username != null) {
                            chat.setUsername(username);
                            chat.setChatId(chatId);
                            chatAdapter.notifyDataSetChanged();
                            Log.d("Firestore", "Fetched username for chat: " + username);
                        } else {
                            Log.e("Firestore", "Username is null for userId: " + userId);
                        }
                    } else {
                        Log.e("Firestore", "User document does not exist for userId: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching username: ", e));
    }






    private void openChat(Match match) {
        if (match.getMatchId() == null) {
            Log.e("ChatActivity", "Error: matchId is null!");
            return;
        }

        db.collection("chats")
                .whereArrayContains("participants", auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    String chatId = null;
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        List<String> participants = (List<String>) document.get("participants");
                        if (participants != null && participants.contains(match.getMatchedUserId())) {
                            chatId = document.getId();
                            Log.d("ChatActivity", "Found existing chatId: " + chatId);
                            break;
                        }
                    }

                            if (chatId == null) {
                                chatId = db.collection("chats").document().getId();
                                createNewChat(chatId, match.getMatchedUserId());
                            }

                            Intent intent = new Intent(ChatActivity.this, ChatRoomActivity.class);
                            intent.putExtra("chatId", chatId);
                            intent.putExtra("matchId", match.getMatchId());
                            startActivity(intent);
                        })
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error fetching chatId: ", e));

    }


    private void createNewChat(String chatId, String matchedUserId) {
        if (chatId == null || matchedUserId == null) {
            Log.e("ChatActivity", "Error: chatId or matchedUserId is null!");
            return;
        }

        List<String> participants = new ArrayList<>();
        participants.add(auth.getCurrentUser().getUid());
        participants.add(matchedUserId);

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("participants", participants);
        chatData.put("timestamp", System.currentTimeMillis());

        db.collection("chats").document(chatId)
                .set(chatData)
                .addOnSuccessListener(aVoid -> Log.d("ChatActivity", "New chat created successfully: " + chatId))
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error creating chat", e));
    }
}


