package server.william.ffats.Model;

public class Token {
    private String token,serverToken;
    private boolean isServerToken;

    public Token(String token, boolean isServerToken) {
        this.token = token;
        this.serverToken = "true";
        this.isServerToken = isServerToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isServerToken() {
        return isServerToken;
    }

    public void setServerToken(boolean serverToken) {
        isServerToken = serverToken;
    }
}
