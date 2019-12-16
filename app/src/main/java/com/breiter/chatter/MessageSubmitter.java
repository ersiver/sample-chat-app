package com.breiter.chatter;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.breiter.chatter.activity.MessageActivity;
import com.breiter.chatter.model.User;
import com.breiter.chatter.notification.APIService;
import com.breiter.chatter.notification.Client;
import com.breiter.chatter.notification.Data;
import com.breiter.chatter.notification.MyResponse;
import com.breiter.chatter.notification.Sender;
import com.breiter.chatter.notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class MessageSubmitter {
    private Context context;
    private String currentChatUserId;
    private APIService apiService;
    private DatabaseReference rootRef;
    private FirebaseUser currentUser;
    private boolean notify;

    public MessageSubmitter(Context context, String currentChatUserId) {
        this.context = context;
        this.currentChatUserId = currentChatUserId;
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    /*
    When the send button inside the MessageActivity is clicked or photo is sent,
    sendMessage() is invoked, firebase is updated and notification about posted message is sent
    */
    public void sendMessage(String senderId, final String recipientId, String message, String imgURL, String type) {
        final Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", senderId);
        hashMap.put("recipient", recipientId);
        hashMap.put("message", message);
        hashMap.put("isread", false);
        hashMap.put("time", System.currentTimeMillis());
        hashMap.put("imageURL", imgURL);
        hashMap.put("type", type);
        rootRef.child("Chats").push().setValue(hashMap);

        updateUserChatDatabase(); //1

        //Send notification about posted message
        final String messageConent = message;
        rootRef.child("Users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {

                    if (notify)
                        sendNotification(recipientId, user.getUsername(), messageConent); //2

                    notify = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //1. Update UserChat database for each user
    private void updateUserChatDatabase() {
        HashMap<String, Object> currentUserMap = new HashMap<>();
        currentUserMap.put("id", currentChatUserId);
        currentUserMap.put("time", System.currentTimeMillis());
        rootRef.child("UserChats").child(currentUser.getUid()).child(currentChatUserId).updateChildren(currentUserMap);

        HashMap<String, Object> userChatMap = new HashMap<>();
        userChatMap.put("id", currentUser.getUid());
        userChatMap.put("time", System.currentTimeMillis());
        rootRef.child("UserChats").child(currentChatUserId).child(currentUser.getUid()).updateChildren(userChatMap);
    }

    //2.
    private void sendNotification(final String recipientId, final String username, final String messageContent) {
        Query query = rootRef.child("Tokens").orderByKey().equalTo(recipientId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Data data = new Data(currentUser.getUid(), "New message", username + ": " + messageContent, R.mipmap.ic_logo_round, recipientId);
                    Token token = snapshot.getValue(Token.class);
                    if (token != null) {
                        Sender sender = new Sender(data, token.getToken());
                        apiService.sendNotification(sender)
                                .enqueue(new Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse>  call, Response<MyResponse> response) {
                                        if (response.code() == 200) {
                                            if(response.body() != null) {
                                                if (response.body().success != 1)
                                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<MyResponse> call, Throwable t) { }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }
}
