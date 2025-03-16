package com.studybuddy.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.studybuddy.android.R;
import com.studybuddy.android.data.model.Chat;
import com.studybuddy.android.data.model.Match;
import com.studybuddy.android.ui.activities.ChatRoomActivity;
import com.studybuddy.android.ui.adapters.ChatAdapter;
import com.studybuddy.android.ui.adapters.MatchAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private RecyclerView matchRecyclerView, chatRecyclerView;
    private MatchAdapter matchAdapter;
    private ChatAdapter chatAdapter;
    private List<Match> matchList;
    private List<Chat> chatList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        matchRecyclerView = view.findViewById(R.id.matchRecyclerView);
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);

        matchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        matchList = new ArrayList<>();
        matchAdapter = new MatchAdapter(matchList, this::onMatchClicked);
        matchRecyclerView.setAdapter(matchAdapter);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), chatList);
        chatRecyclerView.setAdapter(chatAdapter);

        fetchMatches();
        fetchChats();

        return view;
    }

    private void fetchMatches() {
        String currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        db.collection("matches")
                .whereArrayContains("userIds", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    matchList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String userId = document.getString("userId");
                        String matchedUserId = document.getString("matchedUserId");
                        String matchId = document.getId();
                        String name = document.getString("name");

                        String displayUserId = userId.equals(currentUserId) ? matchedUserId : userId;

                        if (displayUserId != null) {
                            matchList.add(new Match(userId, matchedUserId, matchId, name));
                        }
                    }
                    matchAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load matches", Toast.LENGTH_SHORT).show()
                );
    }



    private void fetchChats() {
        db.collection("chats")
                .whereArrayContains("participants", Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Chat chat = document.toObject(Chat.class);
                        chatList.add(chat);
                    }
                    chatAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load chats", Toast.LENGTH_SHORT).show()
                );
    }

    private void onMatchClicked(Match match) {
        String currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        String matchedUserId = match.getMatchId();

        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .whereArrayContains("participants", matchedUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String chatId = querySnapshot.getDocuments().get(0).getId();
                        openChatRoom(chatId, match.getName());
                    } else {
                        createNewChat(currentUserId, matchedUserId, match.getName());
                    }
                });
    }

    private void openChatRoom(String chatId, String userName) {
        Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("userName", userName);
        startActivity(intent);
    }

    private void createNewChat(String user1, String user2, String userName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String chatId = db.collection("chats").document().getId();

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("chatId", chatId);
        chatData.put("participants", Arrays.asList(user1, user2));
        chatData.put("lastMessage", "Say hi to " + userName + "!");
        chatData.put("lastMessageSender", "");
        chatData.put("timestamp", System.currentTimeMillis());

        db.collection("chats").document(chatId)
                .set(chatData)
                .addOnSuccessListener(aVoid -> openChatRoom(chatId, userName))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create chat", Toast.LENGTH_SHORT).show());
    }



}
