package com.breiter.chatter.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.breiter.chatter.R;
import com.breiter.chatter.adapter.UserAdapter;
import com.breiter.chatter.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsersFragment extends Fragment {
    private DatabaseReference rootRef;
    private FirebaseUser currentUser;
    private RecyclerView userRecyclerView;
    private List<User> userList;
    private UserAdapter userAdapter;
    private EditText searchEditText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(requireContext(), userList, false);
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        initRecyclerView(view); //1
        getUsers();             //2

        //Searching users by input
        searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUser(charSequence.toString().toLowerCase().trim());  //3
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        return view;
    }

    //1.
    private void initRecyclerView(View view) {
        userRecyclerView = view.findViewById(R.id.usersRecyclerView);
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }


    //2. Display all users except current user
    private void getUsers() {
        rootRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (searchEditText.getText().toString().equals("")) {
                    retrieveUsers(dataSnapshot); //4
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }

    //3. Search users by input
    private void searchUser(String input) {
        Query query = rootRef.child("Users").orderByChild("search").startAt(input).endAt(input + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                retrieveUsers(dataSnapshot); //4
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    //4.Retrieve users from firebase, add to the list and set in recyclerView
    private void retrieveUsers(DataSnapshot dataSnapshot) {
        userList.clear();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            User user = snapshot.getValue(User.class);
            if (user != null) {
                if (!user.getUserId().equals(currentUser.getUid()))
                    userList.add(user);
            }
        }
        Collections.sort(userList); //Sort users alphabetically
        userRecyclerView.setAdapter(userAdapter);
    }

}
