package com.studybuddy.android.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.studybuddy.android.R;
import com.studybuddy.android.data.model.Chat;
import com.studybuddy.android.ui.activities.ChatRoomActivity;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Chat> chatList;
    private Context context;

    public ChatAdapter(Context context, List<Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        if (chat.getUsername() != null && !chat.getUsername().isEmpty()) {
            holder.usernameTextView.setText(chat.getUsername());
        } else {
            holder.usernameTextView.setText("Loading...");
        }

        if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
            holder.lastMessageTextView.setText(chat.getLastMessage());
        } else {
            holder.lastMessageTextView.setText("No messages yet");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatRoomActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView lastMessageTextView;


        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.chatUsername);
            lastMessageTextView = itemView.findViewById(R.id.chatLastMessage);
        }
    }
}