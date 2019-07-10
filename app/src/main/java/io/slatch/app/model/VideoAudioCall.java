package io.slatch.app.model;

public class VideoAudioCall {

    private String sessionID;
    private String token;

    public VideoAudioCall(String sessionID, String token) {
        this.sessionID = sessionID;
        this.token = token;
    }

    public String getSessionID() {
        return sessionID;
    }
    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}
