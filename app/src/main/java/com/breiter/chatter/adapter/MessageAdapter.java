package com.breiter.chatter.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.breiter.chatter.R;
import com.breiter.chatter.activity.FullScreenImageActivity;
import com.breiter.chatter.activity.LoginActivity;
import com.breiter.chatter.model.ChatMessage;
import com.breiter.chatter.tool.MessageTimeConverter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private static final int MSG_SENDER_TXT = 0;
    private static final int MSG_SENDER_PHOTO = 1;
    private static final int MSG_RECIPIENT_TXT = 2;
    private static final int MSG_RECIPIENT_PHOTO = 3;

    private FirebaseUser firebaseUser;
    private Context context;
    private String profileImageURL;
    private List<ChatMessage> chatList;
    private int[] selectedMessages;

    public MessageAdapter(Context context, List<ChatMessage> chatList, String profileImageURL) {
        this.context = context;
        this.profileImageURL = profileImageURL;
        this.chatList = chatList;

        initializeSelectedMessages(); //to reveal message date/time
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == MSG_SENDER_TXT)
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_txt_right, parent, false);

        else if (viewType == MSG_RECIPIENT_TXT)
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_txt_left, parent, false);

        else if (viewType == MSG_SENDER_PHOTO)
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_jpg_right, parent, false);

        else
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_jpg_left, parent, false);

        hideKeyboard(view); //to hide the keyboard, between typing the message

        return new MessageAdapter.ViewHolder(view);
    }

    //when clicking on the RecyclerView, keyboard is hidden
    private void hideKeyboard(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                if(inputMethodManager != null)
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, final int position) {
        final ChatMessage chatMessage = chatList.get(position);
        setMessageContent(chatMessage, holder);              //1.
        setCurrentChatUser(holder);                          //2.
        setLastMessageStatus(holder, chatMessage, position); //3.
        setMessageTime(holder, chatMessage, position);       //4.
    }

    /*
    1. Retrieve message content and set to the appropriate view:
    If the message is of type text, then string is loaded to the textView
    otherwise the picture is loaded with Glide to the imageView
     */
    private void setMessageContent(ChatMessage chatMessage, ViewHolder holder) {
        holder.messageTextView.setText(chatMessage.getMessage().trim());
        RequestOptions options = new RequestOptions();
        options.transform(new RoundedCorners(70));
        Glide.with(context).asBitmap().apply(options).load(chatMessage.getImageURL()).into(holder.photoImageView);
    }

    //2. Set current chat user photo
    private void setCurrentChatUser(ViewHolder holder) {
        if (profileImageURL.equals("default"))
            holder.profileImageView.setImageResource(R.drawable.account);
        else
            Glide.with(context).load(profileImageURL).into(holder.profileImageView);
    }

    //3. Set seen-message status for the last message
    private void setLastMessageStatus(ViewHolder holder, ChatMessage chatMessage, int position) {
        if (position == chatList.size() - 1) {
            // if it's the last message set icon for read / delivered information
            if (chatMessage.isIsread()) {
                if (profileImageURL.equals("default"))
                    holder.messageSeenImageView.setImageResource(R.drawable.account);
                else
                    Glide.with(context).load(profileImageURL).into(holder.messageSeenImageView);
            } else
                holder.messageSeenImageView.setImageResource(R.drawable.ic_delivered);
        } else
            //if it's no longer last message make the icon gone
            holder.messageSeenImageView.setVisibility(View.GONE);
    }

    //4. Reveal message time once clicked on the message. Message time is always visible for photos
    private void setMessageTime(ViewHolder holder, ChatMessage chatMessage, int position) {
        holder.dateTimeTextView.setText(MessageTimeConverter.getMessageTime(chatMessage));
        if (chatMessage.getType().equals("text")) {
            if (selectedMessages[position] == 1) {
                holder.dateTimeTextView.setVisibility(View.VISIBLE);
            } else
                holder.dateTimeTextView.setVisibility(View.GONE);
        }
    }

    //Rel. to 4. invoked in the constructor
    private void initializeSelectedMessages() {
        selectedMessages = new int[chatList.size()];
        for (int i = 0; i < chatList.size(); i++)
            selectedMessages[i] = 0;
    }

    //Rel. to 4.
    private void setSelectedMessage(int position) {
        for (int i = 0; i < chatList.size(); i++) {
            if (i == position) {
                if (selectedMessages[i] != 1)
                    selectedMessages[i] = 1;
                else
                    selectedMessages[i] = 0; //Un-select, if it's already selected
            } else
                selectedMessages[i] = 0; //Un-select everything else
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(firebaseUser.getUid())) {
            if (chatList.get(position).getType().equals("text"))
                return MSG_SENDER_TXT;
            else
                return
                        MSG_SENDER_PHOTO;
        } else {
            if (chatList.get(position).getType().equals("text"))
                return MSG_RECIPIENT_TXT;
            else
                return
                        MSG_RECIPIENT_PHOTO;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

         CircleImageView profileImageView;
         TextView messageTextView;
         CircleImageView messageSeenImageView;
         TextView dateTimeTextView;
         ImageView photoImageView;

         ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.profileImageView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            messageSeenImageView = itemView.findViewById(R.id.messageSeenImageView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            photoImageView = itemView.findViewById(R.id.photoImageView);

            //Reveal message time once text-message clicked
            messageTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSelectedMessage(getAdapterPosition());
                    notifyDataSetChanged();
                }
            });

            //Reveal full screen photo once image clicked
            photoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, FullScreenImageActivity.class);
                    intent.putExtra("imageUrl", chatList.get(getAdapterPosition()).getImageURL());
                    context.startActivity(intent);
                }
            });
        }
    }
}










