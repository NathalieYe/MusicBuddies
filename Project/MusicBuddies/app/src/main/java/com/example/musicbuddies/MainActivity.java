package com.example.musicbuddies;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                auth = FirebaseAuth.getInstance();
                //Determine whether the user is signed in
                FirebaseUser user = auth.getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(intent);
                    finish();

                }
                if (user != null) {
                    Intent intent = new Intent(MainActivity.this, SwipeActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        };

        h.sendEmptyMessageDelayed(0, 1500);
        //Set Up variables

    }

}