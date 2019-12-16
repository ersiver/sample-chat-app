package com.breiter.chatter.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.breiter.chatter.R;
import com.breiter.chatter.adapter.UserAdapter;
import com.breiter.chatter.model.User;
import com.breiter.chatter.model.UserChat;
import com.breiter.chatter.notification.Token;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatsFragment extends Fragment {
    private DatabaseReference rootRef;
    private FirebaseUser currentUser;
    private RecyclerView chatsRecyclerView;
    private List<UserChat> userChatList;
    private List<User> usersInChat;
    private UserAdapter userAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        userChatList = new ArrayList<>();
        usersInChat = new ArrayList<>();
        userAdapter = new UserAdapter(requireContext(), usersInChat, true);
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        initRecyclerView(view); //1
        getUserMailList();      //2
        return view;
    }

    //1.
    private void initRecyclerView(View view) {
        chatsRecyclerView = view.findViewById(R.id.chatsRecyclerView);
        chatsRecyclerView.setHasFixedSize(true);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    //2. Retrieve all chats of current user and add to the list:
    private void getUserMailList() {
        rootRef.child("UserChats").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userChatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserChat userChats = snapshot.getValue(UserChat.class);
                    if (userChats != null)
                        userChatList.add(userChats);
                }
                //Sorted by sent time
                Collections.sort(userChatList);
                getUsersInChat(); //2a
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(requireActivity(), new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                updateToken(newToken); //2b
            }
        });
    }

    //2a. Retrieve current chat user by their ID and add to list:
    private void getUsersInChat() {
        rootRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersInChat.clear();
                for (UserChat chat : userChatList) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (chat.getId().equals(user.getUserId()))
                                usersInChat.add(user);
                        }
                    }
                }
                //Set the list with users on recycler view:
                chatsRecyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //2b.
    private void updateToken(String newToken) {
        Token token = new Token(newToken);
        rootRef.child("Tokens").child(currentUser.getUid()).setValue(token);
    }
}


