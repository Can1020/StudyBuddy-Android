package com.studybuddy.android.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.studybuddy.android.R;
import com.studybuddy.android.data.model.Message;
import com.studybuddy.android.ui.adapters.MessageAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private String matchId, chatId, currentUserId, chatPartnerId;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        currentUserId = auth.getCurrentUser().getUid();
        chatId = getIntent().getStringExtra("chatId");
        matchId = getIntent().getStringExtra("matchId");

        if (chatId == null || chatId.isEmpty()) {
            Log.e("ChatRoomActivity", "Error: chatId is null or empty!");
            Toast.makeText(this, "Chat cannot be opened.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity to prevent crashes
            return;
        }

        Log.d("ChatRoomActivity", "Opening chat for chatId: " + chatId);

        fetchChatPartnerId();

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        fetchMessages();

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            }
        });
    }

    private void fetchChatPartnerId() {
        if (chatId == null || chatId.isEmpty()) {
            Log.e("ChatRoomActivity", "Error: Cannot fetch chat, chatId is null!");
            return;
        }

        db.collection("chats").document(chatId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.e("ChatRoomActivity", "No chat found for chatId: " + chatId);
                        return;
                    }

                    List<String> participants = (List<String>) documentSnapshot.get("participants");
                    if (participants != null) {
                        for (String participant : participants) {
                            if (!participant.equals(currentUserId)) {
                                chatPartnerId = participant;
                                Log.d("ChatRoomActivity", "Chat partner found: " + chatPartnerId);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatRoomActivity", "Error fetching chat partner: ", e));
    }


    private void sendMessage(String messageText) {
        if (chatPartnerId == null) {
            Toast.makeText(this, "Chat partner not found", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        Message message = new Message(currentUserId, chatPartnerId, messageText, timestamp);

        db.collection("chats").document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageInput.setText("");

                    db.collection("chats").document(chatId)
                            .update("lastMessage", messageText, "timestamp", timestamp)
                            .addOnSuccessListener(aVoid -> Log.d("ChatRoom", "Last message updated successfully"))
                            .addOnFailureListener(e -> Log.e("ChatRoom", "Failed to update last message", e));
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }





    private void fetchMessages() {
        if (chatId == null) {
            Log.e("ChatRoomActivity", "Error: chatId is null!");
            return;
        }

        Log.d("ChatRoomActivity", "Fetching messages for chatId: " + chatId);

        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("ChatRoomActivity", "Error fetching messages: ", e);
                        return;
                    }

                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Log.e("ChatRoomActivity", "No messages found in chatId: " + chatId);
                        return;
                    }

                    messageList.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Message message = document.toObject(Message.class);
                        if (message != null) {
                            Log.d("ChatRoomActivity", "Message loaded: " + message.getText() +
                                    " (Sender: " + message.getSenderId() + ", Receiver: " + message.getReceiverId() + ")");
                            messageList.add(message);
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });
    }

}