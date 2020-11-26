package com.example.musicbuddies;

/*
 * Reference:
 * 1. https://github.com/KODDevYouTube/ChatAppTutorial/blob/master/app/src/main/java/com/koddev/chatapp/MessageActivity.java
 * 2. https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseUser
 * 3. https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
 * 4. https://firebase.google.com/docs/reference/android/com/google/firebase/database/FirebaseDatabase
 * 5. https://firebase.google.com/docs/reference/android/com/google/firebase/database/DatabaseReference
 * 6. https://firebase.google.com/docs/database/admin/retrieve-data
 */


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicbuddies.adapter.MessageAdapter;
import com.example.musicbuddies.model.Chat;
import com.example.musicbuddies.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    // boiler plate stuff
    private ImageButton sendBtn;
    private EditText msgInput;
    private Intent intent;
    private TextView username;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private MessageAdapter messageAdapter;
    private List<Chat> chats;
    private RecyclerView recyclerView;
    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // setting up top tool bar to display user name and back button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.back_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, AllChatsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // display chat messages
        recyclerView = findViewById(R.id.chatbox);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // setting up basic variables
        username = findViewById(R.id.username);
        sendBtn = findViewById(R.id.sendBtn);
        msgInput = findViewById(R.id.msgInput);
        intent = getIntent();
        // get receiver userid from view holder in message adapter
        userid = intent.getStringExtra("userid");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = msgInput.getText().toString();
                if (!msg.equals("")){
                    sendMessage(firebaseUser.getUid(), userid, msg);
                } else {
                    // empty input box: nothing to send
                    Toast.makeText(ChatActivity.this, R.string.you_cant_send_empty_messages, Toast.LENGTH_SHORT).show();
                }
                // after sent, input box cleared
                msgInput.setText("");
            }
        });

        // reference to "Users" child in database
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                // display username of receiver and messages
                username.setText(user.getName());
                readMessages(firebaseUser.getUid(), userid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // store chat messages into database
    private void sendMessage(String sender, final String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        // store info in hash map
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        // set info into database
        reference.child("Chats").push().setValue(hashMap);

        // add the receiver to chat list
        // chat list: a list that keeps track of sender and receiver for retrieving purpose
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(userid);

        // if receiver does not exist in chat list, create one entry for it
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // add sender to chat list
        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(firebaseUser.getUid());
        chatRefReceiver.child("id").setValue(firebaseUser.getUid());
    }

    // retrieve and display chat messages
    private void readMessages(final String myid, final String userid){
        chats = new ArrayList<>();

        // reference to "Chats" child in database
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // clear chats because it's global variable to avoid repeats
                chats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    // if receiver and sender matches in database
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        // add just sent messages to database
                        chats.add(chat);
                    }
                    // adapt and display the messages
                    messageAdapter = new MessageAdapter(ChatActivity.this, chats, userid);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}