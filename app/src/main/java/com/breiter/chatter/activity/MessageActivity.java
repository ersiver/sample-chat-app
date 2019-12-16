package com.breiter.chatter.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.breiter.chatter.MessageSubmitter;
import com.breiter.chatter.R;
import com.breiter.chatter.adapter.MessageAdapter;
import com.breiter.chatter.adapter.OptionsMenuAdapter;
import com.breiter.chatter.model.ChatMessage;
import com.breiter.chatter.model.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    private MessageSubmitter messageSubmitter;

    //Firebase
    private DatabaseReference rootRef;
    private FirebaseUser currentUser;
    private ValueEventListener seenMessageListener;

    //Taking & uploading photos
    private StorageReference storageReference;
    private StorageTask uploadTask;
    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private String filePathCameraCapture;

    //From UserAdapter
    private Intent intent;
    private String currentChatUserId;

    //UI
    private CircleImageView profileImageView;
    private TextView usernameTextView;
    private TextView statusTextView;
    private ImageButton sendImageButton;
    private EditText messageEditText;
    private ImageButton attachImageButton;
    private RelativeLayout messageRelativeLayout;
    private RecyclerView chatRecyclerView;
    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatList;
    private ListView optionsListView;
    private OptionsMenuAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        intent = getIntent(); // Get the current chat user's ID from the UserAdapter intent
        currentChatUserId = intent.getStringExtra("userId");
        messageSubmitter = new MessageSubmitter(MessageActivity.this, currentChatUserId);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        setupToolbar();                //1
        bindViews();                   //2
        setChatWithCurrentUser();      //3
        controlSendButtonVisibility(); //4
        seenMessage();                 //5

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageSubmitter.setNotify(true);
                String message = messageEditText.getText().toString();

                if (!message.equals(""))
                    messageSubmitter.sendMessage(currentUser.getUid(), currentChatUserId, message, "default", "text");

                messageEditText.setText("");
            }
        });

        //When attach button is clicked, list appears with the items to be attached
        attachImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] itemsOnTheList = getResources().getStringArray(R.array.items);
                optionsListView.setVisibility(View.VISIBLE);
                messageRelativeLayout.setVisibility(View.INVISIBLE);
                itemAdapter = new OptionsMenuAdapter(MessageActivity.this, itemsOnTheList);
                optionsListView.setAdapter(itemAdapter);
                optionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                        optionsMenu(position); //6
                    }
                });
            }
        });
    }

    //1. Setup toolbar with option to leave the chat
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeListener(); //1a
                Intent intent = new Intent(MessageActivity.this, AccountActivity.class).
                        setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    //1a. Remove seenMessageListener when leave the chat
    private void removeListener() {
        rootRef.child("Chats").removeEventListener(seenMessageListener);
    }

    //2. Setup activity views
    private void bindViews() {
        //Edit text field and send button
        messageEditText = findViewById(R.id.messageEditText);
        sendImageButton = findViewById(R.id.sendImageButton);
        sendImageButton.setVisibility(View.INVISIBLE);

        //Button and list to attach photos
        attachImageButton = findViewById(R.id.attachImageButton);
        optionsListView = findViewById(R.id.attachmentListView);

        //Setting the LayoutManager to RecyclerView
        messageRelativeLayout = findViewById(R.id.messageRelativeLayout);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);

        //Current chat user in toolbar
        profileImageView = findViewById(R.id.profileImageView);
        usernameTextView = findViewById(R.id.usernameTextView);
        statusTextView = findViewById(R.id.statusTextView);
    }

    //3. Retrieve current chat user's photo, username and status from Firebase and set to the Toolbar
    //Invoke readMessages() method, which reads the chat with the retrieved user
    private void setChatWithCurrentUser() {
        rootRef.child("Users").child(currentChatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User recipient = dataSnapshot.getValue(User.class);
                if (recipient != null) {
                    usernameTextView.setText(recipient.getUsername());

                    if (recipient.getImageURL().equals("default"))
                        profileImageView.setImageResource(R.drawable.account);

                    else
                        Glide.with(getApplicationContext()).load(recipient.getImageURL()).into(profileImageView);

                    if (recipient.getStatus().equals("online"))
                        statusTextView.setText("Active now");

                    else
                        statusTextView.setText("Offline now");

                    readMessages(currentUser.getUid(), recipient.getUserId(), recipient.getImageURL()); //3b
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //3b. Retrieve all messages exchanged between current user and the current chat user
    //Attach the adapter to the RecyclerView and display all messages
    private void readMessages(final String currentUserId, final String recipientId, final String imgURL) {
        chatList = new ArrayList<>();
        rootRef.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        if (chatMessage.getSender().equals(currentUserId) && chatMessage.getRecipient().equals(recipientId)
                                || chatMessage.getSender().equals(recipientId) && chatMessage.getRecipient().equals(currentUserId))

                            chatList.add(chatMessage);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, chatList, imgURL);
                    chatRecyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //4. Hide send button when EditTextView is empty or is whitespaces. Reveal otherwise
    private void controlSendButtonVisibility() {
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String message = messageEditText.getText().toString();
                if (message.trim().equals(""))
                    sendImageButton.setVisibility(View.INVISIBLE);
                else
                    sendImageButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    //5. Update Chats database, when the new message is seen by the current user
    private void seenMessage() {
        seenMessageListener = rootRef.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        if (chatMessage.getRecipient().equals(currentUser.getUid())
                                && chatMessage.getSender().equals(currentChatUserId)) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isread", true);
                            snapshot.getRef().updateChildren(hashMap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //6. Options to send image, camera capture or cancelling attachments
    private void optionsMenu(int i) {
        if (i == 0)  //Gallery
            verifyReadStoragePermission();  //7

        if (i == 1) {  //Camera
            verifyWriteStoragePermission(); //8

        } else if (i == 2) //Cancellation
            hideAttachmentListView();       //9
    }

    //7. Check, if the user has granted permission of read external storage, if not then ask to grant it
    private void verifyReadStoragePermission() {
        if (ContextCompat.checkSelfPermission(MessageActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MessageActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_GALLERY_REQUEST); //7a ask for permission
        else {
            hideAttachmentListView();
            photoGalleryIntent();   //7b permission granted
        }
    }

    //8. Check, if the user has granted permission of write external storage, if not then ask to grant it
    private void verifyWriteStoragePermission() {
        if (ContextCompat.checkSelfPermission(MessageActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MessageActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, IMAGE_CAMERA_REQUEST); //8a ask for permission
        else {
            hideAttachmentListView();
            photoCameraIntent();      //8b permission granted
        }
    }

    //7a & 8a
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hideAttachmentListView();
                photoGalleryIntent();  //7b permission granted
            }
        } else if (requestCode == IMAGE_CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hideAttachmentListView();
                photoCameraIntent();  //8b permission granted
            }
        }
    }

    //7b. Permission granted, send image from gallery
    public void photoGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, IMAGE_GALLERY_REQUEST);  //7c
    }

    //8b. Permission granted, send captured photo
    public void photoCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();  //8c
            } catch (IOException ex) {
                // Error occurred
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.breiter.chatter", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, IMAGE_CAMERA_REQUEST); //8d
            }
        }
    }

    //8c.
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        filePathCameraCapture = image.getAbsolutePath();
        return image;
    }

    //7c & 8d
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null)
                    uploadFileFirebase(selectedImageUri); //7c
            }

        } else if (requestCode == IMAGE_CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                File file = new File(filePathCameraCapture);
                Uri contentUri = Uri.fromFile(file);
                if (contentUri != null)
                    uploadFileFirebase(contentUri);      //8e
            }
        }
    }

    //7c & 8e. Uploading image file to Firebase, invoking sendMessage()
    private void uploadFileFirebase(Uri imageUri) {
        String imageName = UUID.randomUUID().toString() + System.currentTimeMillis() + ".jpg";
        storageReference = FirebaseStorage.getInstance().getReference("ChatGallery");
        final StorageReference filePath = storageReference.child(imageName);
        uploadTask = filePath.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful())
                    throw task.getException();
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    ///
                    Uri downloadUri = task.getResult();
                    if(downloadUri != null) {
                        String imgUri = downloadUri.toString();
                        messageSubmitter.sendMessage(currentUser.getUid(), currentChatUserId, "", imgUri, "photo");
                    }
                } else
                    Toast.makeText(getApplicationContext(), "There was a problem with loading your" +
                            " image...", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Log", "onFailure sendFileFirebase " + e.getMessage());
            }
        });
    }

    //9. Hide the menu with options to send the image
    private void hideAttachmentListView() {
        optionsListView.setVisibility(View.INVISIBLE);
        messageRelativeLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("online"); //10
        currentChatUser(currentChatUserId); //11
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeListener();  // 1a
        updateStatus("offline");  //10
        currentChatUser("none"); //11
    }

    //10. Update activity status of current user on Firebase
    private void updateStatus(String status) {
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        rootRef.child("Users").child(currentUser.getUid()).updateChildren(hashMap);
    }

    //11. Save the current chat user ID and retrieve it in Message Service
    //to avoid sending notifications, when the chat with this user is open
    public void currentChatUser(String otherUserId) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentChatUser", otherUserId);
        editor.apply();
    }
}

