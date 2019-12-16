package com.breiter.chatter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.breiter.chatter.R;
import com.breiter.chatter.adapter.ViewPagerAdapter;
import com.breiter.chatter.model.ChatMessage;
import com.breiter.chatter.model.User;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class AccountActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private DatabaseReference rootRef;
    private CircleImageView profileImageView;
    private TextView usernameTextView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private TextView messageAlertTextView;
    private RelativeLayout alertLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        bindViews();      //1
        setupUserInBar(); //2
        setupAdapter();   //3
    }

    //1.
    private void bindViews() {
        profileImageView = findViewById(R.id.profileImageView);
        usernameTextView = findViewById(R.id.usernameTextView);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        alertLayout = findViewById(R.id.alertLayout);
        messageAlertTextView = findViewById(R.id.messageAlertTextView);
    }

    //2. Retrieve current user's photo and username from Firebase. Set the photo and username to the Toolbar
    private void setupUserInBar() {
        rootRef.child("Users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    usernameTextView.setText(user.getUsername());
                    if (user.getImageURL().equals("default"))
                        profileImageView.setImageResource(R.drawable.account);
                    else
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(profileImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //3. Set up bottom navigation bar with Fragments, each with title and icon
    //ChatFragment's title depends on quantity of unread messages
    private void setupAdapter() {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        setupTabIcons();   //3a
        addMessageAlert(); //3b
    }

    //3a.
    private void setupTabIcons() {
        int[] tabIcons = {
                R.drawable.chat_icon,
                R.drawable.users_icon,
                R.drawable.account_icon};

        if (tabLayout != null) {
            final TabLayout.Tab chatTab = tabLayout.getTabAt(0);
            final TabLayout.Tab usersTab = tabLayout.getTabAt(1);
            final TabLayout.Tab profileTab = tabLayout.getTabAt(2);

            if (chatTab != null && usersTab != null && profileTab != null) {
                chatTab.setIcon(tabIcons[0]);
                usersTab.setIcon(tabIcons[1]);
                profileTab.setIcon(tabIcons[2]);
            }
        }
    }

    //3b. Add unread messages counter in the red circle
    private void addMessageAlert() {
        rootRef.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int countUnread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        if (chatMessage.getRecipient().equals(currentUser.getUid()) && !chatMessage.isIsread())
                            countUnread++;
                    }
                }
                if (countUnread == 0)
                    alertLayout.setVisibility(View.GONE);
                else {
                    alertLayout.setVisibility(View.VISIBLE);
                    messageAlertTextView.setText(Integer.toString(countUnread));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("online"); //4
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateStatus("offline"); //4
    }

    //Setup menu with logout option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            updateStatus("offline"); //4
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    //4. Update activity status of current user on Firebase
    //Activity status is online onResume, offline onPause and when logout
    private void updateStatus(String status) {
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        rootRef.child("Users").child(currentUser.getUid()).updateChildren(hashMap);
    }

}

