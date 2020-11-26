package com.example.musicbuddies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditPreferencesActivity extends AppCompatActivity {

    // hamburger /sliding menu boilerplate
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Spinner spGender, spGenres;
    private String gender, genres;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_preferences);
        spGender = (Spinner) findViewById(R.id.spGender);
        spGenres = (Spinner) findViewById(R.id.spGenres);
        btnConfirm = (Button) findViewById(R.id.btnConfirm);

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
                        return true;
                    case R.id.item5: // settings
                        Intent intent4 = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(intent4);
                        return true;
                }
                return true;
            }});

        //Setting up adapters fo genre and gender adapters
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gendersPref, R.layout.spinner_item);
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> genresAdapter = ArrayAdapter.createFromResource(this,
                R.array.genres, R.layout.spinner_item);
        genresAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spGenres.setAdapter(genresAdapter);

        //Retrieve preference data
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Pref").child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                assert dataSnapshot != null;
                gender = dataSnapshot.child("gender").getValue(String.class);
                genres = dataSnapshot.child("genre").getValue(String.class);

                //set updated user preferences
                int genrePosition = genresAdapter.getPosition(genres);
                spGenres.setSelection(genrePosition);
                int genderPosition = genderAdapter.getPosition(gender);
                spGender.setSelection(genderPosition);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        spGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spGenres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                genres = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditPreferencesActivity.this, R.string.preferences_updated, Toast.LENGTH_SHORT).show();

                // storing user search preference
                storePreference();

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

    // storing user preference into the database
    public void storePreference() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Pref");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;

        //create hashmap for user preferences
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("gender", gender);
        hashMap.put("genre", genres);
        hashMap.put("id", firebaseUser.getUid());

        // store user search preference within userid child
        reference.child(firebaseUser.getUid()).setValue(hashMap);
    }
}