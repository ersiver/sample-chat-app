package com.breiter.chatter.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.breiter.chatter.R;
import com.breiter.chatter.activity.MessageActivity;
import com.breiter.chatter.model.ChatMessage;
import com.breiter.chatter.model.User;
import com.breiter.chatter.tool.MessageTimeConverter;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<User> userList;
    private boolean isChat;
    private DatabaseReference rootRef;

    public UserAdapter(Context context, List<User> userList, boolean isChat) {
        this.context = context;
        this.userList = userList;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        rootRef = FirebaseDatabase.getInstance().getReference();
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final User user = userList.get(position);

        holder.usernameTextView.setText(user.getUsername());
        holder.usernameTextView.setTextColor(Color.WHITE);

        if (user.getImageURL().equals("default"))
            holder.profileImageView.setImageResource(R.drawable.account);
        else
            Glide.with(context).load(user.getImageURL()).into(holder.profileImageView);

        //Set user's online status in chat fragment & user fragment
        if (user.getStatus().equals("online"))
            holder.statusImageView.setVisibility(View.VISIBLE);
        else
            holder.statusImageView.setVisibility(View.GONE);

        //Add description to each chat (last message, dateTime)
        if (isChat) {
            setChatDescription(user.getUserId(), user.getUsername(), holder.lastMsgTextView, holder.dateTimeTextView);
        } else
            holder.lastMsgTextView.setVisibility(View.GONE);

        //Redirect to individual chat with selected user
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userId", user.getUserId());
                context.startActivity(intent);
            }
        });
    }

    /*
    Retrieve last message and its time for each chat and set them the TextViews
    Message from the current user starts with "You: " otherwise with user name
    Where the last message is a photo, there's an info "(user) sent photo"
    Unread message is in bold
     */
    private void setChatDescription(final String userid, final String username, final TextView lastMessageTextView, final TextView dateTimeTextView) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        rootRef.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage chat = snapshot.getValue(ChatMessage.class);
                    if (currentUser != null && chat != null) {
                        if (chat.getRecipient().equals(currentUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getRecipient().equals(userid) && chat.getSender().equals(currentUser.getUid())) {

                            String lastMessage = chat.getMessage();

                            if (chat.getSender().equals(currentUser.getUid())) {
                                if (!lastMessage.equals(""))
                                    lastMessage = "You: " + lastMessage;
                                else
                                    lastMessage = "You sent photo.";

                            } else {
                                if (!lastMessage.equals(""))
                                    lastMessage = username + ": " + lastMessage;
                                else
                                    lastMessage = username + " sent photo.";

                                if (!chat.isIsread())
                                    lastMessageTextView.setTypeface(Typeface.DEFAULT_BOLD);

                            }
                            lastMessageTextView.setText(lastMessage);

                            //Add time of last message sent
                            String dateTime = MessageTimeConverter.getMessageTime(chat);
                            dateTimeTextView.setText(dateTime);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

     class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView usernameTextView;
        CircleImageView statusImageView;

        //relevant to chat fragment only
        TextView dateTimeTextView;
        TextView lastMsgTextView;

         ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            statusImageView = itemView.findViewById(R.id.statusImageView);

            //relevant to chat fragment only
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            lastMsgTextView = itemView.findViewById(R.id.lastMsgTextView);
        }
    }
}
