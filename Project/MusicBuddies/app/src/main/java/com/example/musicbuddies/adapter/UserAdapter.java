package com.example.musicbuddies.adapter;

/*
 * Reference:
 * 1. https://github.com/KODDevYouTube/ChatAppTutorial/blob/master/app/src/main/java/com/koddev/chatapp/Adapter/UserAdapter.java
 * 2. https://firebase.google.com/docs/reference/android/com/google/firebase/storage/FirebaseStorage
 * 3. https://firebase.google.com/docs/reference/android/com/google/firebase/storage/StorageReference
 */


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicbuddies.ChatActivity;
import com.example.musicbuddies.R;
import com.example.musicbuddies.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{
    private Context context;
    private List<User> users;
    private StorageReference storageReference;
    private StorageReference fileRef;

    // a beautiful constructor
    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = users.get(position);
        holder.username.setText(user.getName());
        // reference to firebase storage to retrieve profile image
        storageReference = FirebaseStorage.getInstance().getReference();

        // get profile image from firebase storage
        try {
            fileRef = storageReference.child(user.getId()).child("profile.jpeg");
            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(holder.profile_image);
                }
            });
        } catch (Exception e){
            //User has not uploaded image
        }

        // if user item is clicked, jump to the chat with that user
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userid", user.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // locate username and profile image
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
        }
    }
}
