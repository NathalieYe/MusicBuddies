package com.example.musicbuddies.adapter;

/*
 * Reference:
 * 1. https://github.com/KODDevYouTube/ChatAppTutorial/blob/master/app/src/main/java/com/koddev/chatapp/Adapter/MessageAdapter.java
 * 2. https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseUser
 * 3. https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
 * 4. https://firebase.google.com/docs/reference/android/com/google/firebase/storage/FirebaseStorage
 * 5. https://firebase.google.com/docs/reference/android/com/google/firebase/storage/StorageReference
 * 6. https://firebase.google.com/docs/database/admin/retrieve-data
 */


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicbuddies.R;
import com.example.musicbuddies.model.Chat;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    // boiler plate
    public static final int TYPE_RIGHT = 0;
    public static final int TYPE_LEFT = 1;
    private Context context;
    private List<Chat> chats;
    private String userid;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private StorageReference fileRef;

    // a glorious constructor
    public MessageAdapter(Context context, List<Chat> chats, String userid) {
        this.context = context;
        this.chats = chats;
        this.userid = userid;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.show_message.setText(chat.getMessage());
        storageReference = FirebaseStorage.getInstance().getReference();

        // if message item is right item (sender)
        if (getItemViewType(position) == TYPE_RIGHT) {
            try {
                fileRef = storageReference.child(firebaseUser.getUid()).child("profile.jpeg");
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(holder.profile_image);
                    }
                });
            } catch (Exception e){
                //User has not uploaded image
            }
        }
        // if message item is left item (receiver)
        else {
            try {
                fileRef = storageReference.child(userid).child("profile.jpeg");
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(holder.profile_image);
                    }
                });
            } catch (Exception e){
                //User has not uploaded image
            }
        }

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
        }
    }

    // get item type (left: receiver, right: sender)
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chats.get(position).getSender().equals(firebaseUser.getUid())) {
            return TYPE_RIGHT;
        }
        else {
            return TYPE_LEFT;
        }
    }
}

