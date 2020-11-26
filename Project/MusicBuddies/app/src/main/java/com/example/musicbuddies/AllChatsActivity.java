package com.example.musicbuddies;

/*
 * Reference:
 * 1. https://github.com/KODDevYouTube/ChatAppTutorial/blob/master/app/src/main/java/com/koddev/chatapp/MainActivity.java
 * 2. https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseUser
 * 3. https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
 * 4. https://firebase.google.com/docs/reference/android/com/google/firebase/database/FirebaseDatabase
 * 5. https://firebase.google.com/docs/reference/android/com/google/firebase/database/DatabaseReference
 * 6. https://firebase.google.com/docs/database/admin/retrieve-data
 */


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.musicbuddies.fragment.ChatFragment;
import com.example.musicbuddies.fragment.MatchFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class AllChatsActivity extends AppCompatActivity {
    // boiler plate
    private RecyclerView recyclerView;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    // hamburger /sliding menu boiler plate
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_chats);

        // get current logged in user and reference to "Users" child in database
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

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
                        // do not do anything
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
                        Intent intent5 = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(intent5);
                        return true;
                }
                return true;
            }
        });

        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        final ViewPager viewPager = findViewById(R.id.viewPager);

        // reference to "Chats" child in database
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // setting up view pager for tab layout
                // (WE HAVE FRAGMENTS THAT SLIDE LEFT AND RIGHT AS WELL)
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                viewPagerAdapter.addFragment(new MatchFragment(), getResources().getString(R.string.match));
                viewPagerAdapter.addFragment(new ChatFragment(), getResources().getString(R.string.chats));
                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    // view pager adapter for displaying fragments in tab layout
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm){
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

}