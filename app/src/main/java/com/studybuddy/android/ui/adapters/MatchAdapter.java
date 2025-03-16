package com.studybuddy.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.studybuddy.android.R;
import com.studybuddy.android.data.model.Match;
import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private List<Match> matchList;
    private OnMatchClickListener matchClickListener;

    public interface OnMatchClickListener {
        void onMatchClick(Match match);
    }

    public MatchAdapter(List<Match> matchList, OnMatchClickListener matchClickListener) {
        this.matchList = matchList;
        this.matchClickListener = matchClickListener;
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public MatchViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.matchName);
        }
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_item, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matchList.get(position);
        holder.name.setText(match.getName());
        holder.itemView.setOnClickListener(v -> matchClickListener.onMatchClick(match));
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }
}
