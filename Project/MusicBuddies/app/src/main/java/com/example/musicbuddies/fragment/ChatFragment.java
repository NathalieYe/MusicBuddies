package com.example.musicbuddies.fragment;

/*
 * Reference:
 * 1. https://github.com/KODDevYouTube/ChatAppTutorial/blob/master/app/src/main/java/com/koddev/chatapp/Fragments/ChatsFragment.java
 * 2. https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseUser
 * 3. https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
 * 4. https://firebase.google.com/docs/reference/android/com/google/firebase/database/FirebaseDatabase
 * 5. https://firebase.google.com/docs/reference/android/com/google/firebase/database/DatabaseReference
 * 6. https://firebase.google.com/docs/database/admin/retrieve-data
 */

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.musicbuddies.adapter.UserAdapter;
import com.example.musicbuddies.model.Chatlist;
import com.example.musicbuddies.model.User;
import com.example.musicbuddies.R;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    // boiler plate
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> users;
    FirebaseUser fuser;
    DatabaseReference reference;
    private List<Chatlist> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.chat_box);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        usersList = new ArrayList<>();

        // reference to "chatlist" child in database
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    // retrieve users from database using users list
    private void chatList() {
        users = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (Chatlist chatlist : usersList){
                        if (user.getId().equals(chatlist.getId())){
                            users.add(user);
                        }
                    }
                }
                // adapt and display users
                userAdapter = new UserAdapter(getContext(), users);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
