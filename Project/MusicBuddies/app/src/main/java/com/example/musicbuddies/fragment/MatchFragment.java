package com.example.musicbuddies.fragment;

/*
 * Reference:
 * 1. https://github.com/KODDevYouTube/ChatAppTutorial/blob/master/app/src/main/java/com/koddev/chatapp/Fragments/UsersFragment.java
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

import com.example.musicbuddies.model.Match;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.musicbuddies.adapter.UserAdapter;
import com.example.musicbuddies.model.User;
import com.example.musicbuddies.R;

import java.util.ArrayList;
import java.util.List;

public class MatchFragment extends Fragment {
    // boiler plate
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> user_list;
    private List<String> match_list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);

        recyclerView = view.findViewById(R.id.match_box);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // initialize global lists to store info
        match_list = new ArrayList<>();
        user_list = new ArrayList<>();

        readMatches();

        return view;
    }

    // retrieve matched users from swipe activity
    private void readMatches() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Matches").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Match match = snapshot.getValue(Match.class);
                    assert match != null;
                    // add matched user id to match list for retrieve purpose
                    match_list.add(match.getId());
                }
                fetchUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // retrieve users from database using match list
    private void fetchUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    for (String id : match_list) {
                        if (user.getId().equals(id)) {
                            user_list.add(user);
                        }
                    }
                }
                // adapt and display users from user list
                userAdapter = new UserAdapter(getContext(), user_list);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
