package server.william.ffats.Service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.HashMap;

import server.william.ffats.Database.SessionManager;
import server.william.ffats.Model.Token;

public class FirebaseIdService extends FirebaseMessagingService{

    SessionManager sessionManager = new SessionManager(getApplicationContext(), SessionManager.SESSION_USER);
    HashMap<String, String> userInformation = sessionManager.getInfomationUser();

    @Override
    public void onNewToken(@NonNull String refreshedToken ) {
        super.onNewToken(refreshedToken);
        Log.d("NEW_TOKEN",refreshedToken);
        if (userInformation.get(SessionManager.KEY_FULLNAME) != null){
            updateTokenToFirebase(refreshedToken);
        }

    }

    private void updateTokenToFirebase(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token token = new Token(refreshedToken,true);
        tokens.child(userInformation.get(SessionManager.KEY_PHONENUMBER)).setValue(token);
    }
}
