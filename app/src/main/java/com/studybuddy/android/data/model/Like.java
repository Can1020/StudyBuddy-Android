package com.studybuddy.android.data.model;

public class Like {
    private String userId;
    private String likedUserId;

    public Like() {}

    public Like(String userId, String likedUserId) {
        this.userId = userId;
        this.likedUserId = likedUserId;
    }

    public String getUserId() { return userId; }
    public String getLikedUserId() { return likedUserId; }
}
