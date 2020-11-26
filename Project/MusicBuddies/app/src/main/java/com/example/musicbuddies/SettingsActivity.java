
package com.example.musicbuddies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {
    private Button btnLogout, btnDelete, btnConfirm, btnCancel;

    // hamburger /sliding menu boilerplate
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Setup variables
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Logout user and return to signin page
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();

            }
        });

        //hamburger / sliding menu
        drawerLayout=findViewById(R.id.drawerLayout);
        navigationView=findViewById(R.id.navigationView);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //when toggle opened, back arrow
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1: //find matches / swipe page
                        Intent intent = new Intent(getApplicationContext(), SwipeActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.item2: // chats
                        Intent intent2 = new Intent(getApplicationContext(), AllChatsActivity.class);
                        startActivity(intent2);
                        return true;
                    case R.id.item3: // edit profile
                        Intent intent3 = new Intent(getApplicationContext(), EditProfileActivity.class);
                        startActivity(intent3);
                        return true;
                    case R.id.item4: // edit preferences
                        Intent intent4 = new Intent(getApplicationContext(), EditPreferencesActivity.class);
                        startActivity(intent4);
                        return true;
                    case R.id.item5: // settings
                        return true;
                }
                return true;
            }
        });
    }

    //for hamburger / sliding menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog() {
        //Setup delete confirmation dialog
        AlertDialog.Builder dialog;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        //Depending on version, build different dialog
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new AlertDialog.Builder(SettingsActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            dialog = new AlertDialog.Builder(SettingsActivity.this);
        }

        //Inflate delete dialog layout page
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.delete_dialog, null);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        btnCancel = view.findViewById(R.id.btnCancel);

        dialog.setView(view);
        dialog.setCancelable(false);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get database reference of where the user information is stored
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

                //Delete the user from firebase auth
                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Delete user profile information from database
                            databaseReference.removeValue();
                            Toast.makeText(SettingsActivity.this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SettingsActivity.this, R.string.unable_to_delete_account, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //Create popup dialog
        AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        alertDialog.show();

        //Dismiss popup dialog
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }
      @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(SettingsActivity.this, SwipeActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
