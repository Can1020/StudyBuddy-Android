package com.studybuddy.android.ui.adapters;

import com.studybuddy.android.data.model.User;

public interface OnUserActionListener {
    void onLikeClicked(User user);
    void onDislikeClicked(User user);
}
