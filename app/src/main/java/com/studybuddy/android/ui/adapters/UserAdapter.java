package com.studybuddy.android.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.studybuddy.android.R;
import com.studybuddy.android.data.model.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;
    private Context context;
    private OnUserActionListener listener;

    public UserAdapter(Context context, List<User> users, OnUserActionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameAge, location, university, course, skills;
        ImageButton likeButton, dislikeButton;

        public UserViewHolder(View view) {
            super(view);
            nameAge = view.findViewById(R.id.nameAge);
            location = view.findViewById(R.id.location);
            university = view.findViewById(R.id.university);
            course = view.findViewById(R.id.course);
            skills = view.findViewById(R.id.skills);
            likeButton = view.findViewById(R.id.likeButton);
            dislikeButton = view.findViewById(R.id.dislikeButton);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card, parent, false);
        return new UserViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.nameAge.setText(user.getName() + ", " + user.getAge());
        holder.location.setText("📍 Lives in " + user.getLocation());
        holder.university.setText("🎓 " + user.getUniversity());
        holder.course.setText("📖 " + user.getCourseOfStudy() + " - " + user.getSemester() + ". Semester");
        holder.skills.setText("⭐ " + user.getSkills());

        // ✅ Handle Like Button Click
        holder.likeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeClicked(user);
            }
        });

        // ✅ Handle Dislike Button Click
        holder.dislikeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDislikeClicked(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


}
