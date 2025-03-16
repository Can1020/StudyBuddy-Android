package com.studybuddy.android.data.model;

public class Match {
    private String userId;
    private String matchedUserId;
    private String matchId;
    private String name;

    public Match() {}

    public Match(String userId, String matchedUserId, String matchId, String name) {
        this.userId = userId;
        this.matchedUserId = matchedUserId;
        this.matchId = matchId;
        this.name = name;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMatchedUserId() { return matchedUserId; }
    public void setMatchedUserId(String matchedUserId) { this.matchedUserId = matchedUserId; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
