package com.example.musicbuddies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import com.example.musicbuddies.model.Pref;
import com.example.musicbuddies.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.musicbuddies.EditProfileActivity.extractYtPlaylistID;
import static com.example.musicbuddies.EditProfileActivity.extractSpPlaylistID;

public class SwipeActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {
    //swiping
    private GestureDetector GD;
    private static final int SWIPE_DISTANCE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    //views and buttons
    private CircleImageView buddyProfilePic;
    private TextView txtVwBuddyName;
    private TextView txtBuddyAge;
    private TextView txtBuddyFavGenre;
    private TextView txtBuddyFavArtist;
    private TextView txtBuddyBio;
    private Button btnBuddyPlayYoutube;
    private TextView txtVwFindBuddies;
    private TextView txtBuddyGender;

    //youtube
    private String youtubeUrl;
    private LinearLayout lnYoutube;
    private YouTubePlayerSupportFragment youtubePlaylist;
    YouTubePlayer.OnInitializedListener youTubeOnlistener;
    private String key;

    //spotify
    private String spotifyUrl, spotifyUri;
    private Button btnPlay, btnNext, btnPrevious, btnAddSpotify;
    private TextView txtCurrentSong;
    private SpotifyAppRemote mSpotifyAppRemote;
    private static final String CLIENT_ID = "f5d5abcb90b344d1b8eb3b69cec4bdcd";
    private static final String REDIRECT_URI = "com.example.musicbuddies://callback";

    // hamburger /sliding menu boilerplate
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private DatabaseReference reference;
    private DatabaseReference user_reference;
    private ImageButton imgBtnMessages;
    private String pref_type1;      // gender
    private String pref_type2;      // genre

    //Uploading profile pictures
    private CircleImageView profileImageView;
    private StorageReference storageReference;
    private StorageReference fileRef;


    private LinearLayout lnPlaylist;
    private View vwBox;
    private Button btnCancel, btnPlaylists;

    private Boolean swipe = true, playSpotify = true;
    //Users
    private List<User> profileList;
    private FirebaseUser firebaseUser;
    private int swipeUserPos;
    private int swipeUserMax;
    private List<String> matches;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);

        GD = new GestureDetector(this, this);
        //Users
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        swipeUserPos = 0;

        //Retrieve storage information for storing and retrieving photos
        storageReference = FirebaseStorage.getInstance().getReference();
        buddyProfilePic = (CircleImageView) findViewById(R.id.buddyProfilePic);

        //Setup key for retrieving videos from youtube
        key = SwipeActivity.this.getResources().getString(R.string.google_api_key);
        final Integer RECOVERY_REQUEST = 1;

        //views and buttons
        txtVwFindBuddies = (TextView) findViewById(R.id.txtVwFindBuddies);
        txtVwBuddyName = (TextView) findViewById(R.id.txtVwBuddyName);
        txtBuddyAge = (TextView) findViewById(R.id.txtBuddyAge);
        txtBuddyFavGenre = (TextView) findViewById(R.id.txtBuddyFavGenre);
        txtBuddyFavArtist = (TextView) findViewById(R.id.txtBuddyFavArtist);
        txtBuddyBio = (TextView) findViewById(R.id.txtBuddyBio);
        btnBuddyPlayYoutube = (Button) findViewById(R.id.btnBuddyPlayYoutube);
        btnAddSpotify = (Button) findViewById(R.id.btnAddSpotify);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        txtCurrentSong = (TextView) findViewById(R.id.txtCurrentSong);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnPrevious = (Button) findViewById(R.id.btnPrevious);
        txtBuddyGender = (TextView) findViewById(R.id.txtBuddyGender);

        //youtube
        lnYoutube = (LinearLayout) findViewById(R.id.lnYoutube);
        lnYoutube.setVisibility(LinearLayout.INVISIBLE);
        youtubePlaylist = (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.ytbPlaylist);

        vwBox = (View) findViewById(R.id.vwBox);
        lnPlaylist = (LinearLayout) findViewById(R.id.lnPlaylist);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnPlaylists = (Button) findViewById(R.id.btnPlaylists);
        vwBox.setVisibility(View.INVISIBLE);
        lnPlaylist.setVisibility(LinearLayout.INVISIBLE);

        //user info
        profileList = new ArrayList<User>();
        matches = new ArrayList<>();
        setSearchFilter();
        swipeUserMax = matches.size();

        //when play youtube button clicked
        btnBuddyPlayYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (youtubeUrl != null) {
                    if (!youtubeUrl.equals("")) {

                        //Checks if Youtube URL is valid if invalid, empty string is returned.
                        final String playlistID = extractYtPlaylistID(youtubeUrl);
                        lnYoutube.setVisibility(LinearLayout.VISIBLE);
                        //Show youtube video
                        youTubeOnlistener = new YouTubePlayer.OnInitializedListener() {
                            @Override
                            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

                                if (!b) {
                                    //Load youtube playlist
                                    youTubePlayer.loadPlaylist(playlistID);
                                }
                            }

                            @Override
                            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                                if (youTubeInitializationResult.isUserRecoverableError()) {
                                    youTubeInitializationResult.getErrorDialog(SwipeActivity.this, RECOVERY_REQUEST).show();
                                } else {
                                    Log.e("INITIALIZING YOUTUBE:" ,youTubeInitializationResult.toString());
                                }

                            }

                            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                                if (requestCode == RECOVERY_REQUEST) {
                                    // Retry initialization if user performed a recovery action
                                    getYouTubePlayerProvider().initialize(key, this);
                                }
                            }

                            protected YouTubePlayer.Provider getYouTubePlayerProvider() {
                                return youtubePlaylist;
                            }
                        };
                        youtubePlaylist.initialize(key, youTubeOnlistener);

                        //hides keyboard from user
                        InputMethodManager keyboard = (InputMethodManager) getSystemService(SwipeActivity.this.INPUT_METHOD_SERVICE);
                        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }

                } else {
                    Toast.makeText(SwipeActivity.this, R.string.no_playlist_available, Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vwBox.setVisibility(View.INVISIBLE);
                lnPlaylist.setVisibility(LinearLayout.INVISIBLE);
                youtubePlaylist.onDestroy();
                swipe = true;
                playSpotify = true;

            }
        });
        btnPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipe = false;
                vwBox.setVisibility(View.VISIBLE);
                lnPlaylist.setVisibility(LinearLayout.VISIBLE);
                lnYoutube.setVisibility(LinearLayout.INVISIBLE);
                txtCurrentSong.setText("");

            }
        });
        LinearLayout lnBuddy = findViewById(R.id.lnBuddy);
        lnBuddy.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // ... Respond to touch events
                if (swipe) {
                    GD.onTouchEvent(event);
                }
                return true;
            }
        });

        //hamburger / sliding menu
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
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
//                        Toast.makeText(SwipeActivity.this, "swipe page", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.item2: // chats
                        Intent intent = new Intent(getApplicationContext(), AllChatsActivity.class);
                        startActivity(intent);
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
    }

    //display user information on each swipe
    private void swipeUsers() {
        swipeUserMax = profileList.size();

        if (swipeUserPos < swipeUserMax) {
            txtVwBuddyName.setText(profileList.get(swipeUserPos).getName());
            txtBuddyAge.setText(getAge(profileList.get(swipeUserPos).getBirthday()));
            txtBuddyFavGenre.setText(profileList.get(swipeUserPos).getGenres());
            txtBuddyFavArtist.setText(profileList.get(swipeUserPos).getArtists());
            txtBuddyBio.setText(profileList.get(swipeUserPos).getBio());
            txtBuddyGender.setText(profileList.get(swipeUserPos).getGender());
            spotifyUrl = profileList.get(swipeUserPos).getSpotifyPlaylistUrl();
            youtubeUrl = profileList.get(swipeUserPos).getYoutubePlaylistURL();
            userId = profileList.get(swipeUserPos).getId();

            //Check if image is in storage
            fileRef = storageReference.child(userId).child("profile.jpeg");
            if (fileRef == null) {
                buddyProfilePic.setImageResource(R.drawable.personprofile);
            } else {
                setImageFromFirebase();
            }

        } else{
            Toast.makeText(SwipeActivity.this, R.string.no_more_users, Toast.LENGTH_SHORT).show();
        }
    }

    //get and set user profile pic
    private void setImageFromFirebase() {
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(buddyProfilePic);
            }
        });

    }

    // calculate users age from their birthday
    private String getAge(String date) {
        String[] arrayDate = date.split("/", 3);
        int day = Integer.parseInt(arrayDate[0]);
        int month = Integer.parseInt(arrayDate[1]);
        int year = Integer.parseInt(arrayDate[2]);

        // Set up variables to hold todays date and dob
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }

    //for hamburger / sliding menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public boolean onTouchEvent(MotionEvent event) {
        this.GD.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    //determine if gesture is a swipe left or right
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float distanceX = e2.getX() - e1.getX();
        float distanceY = e2.getY() - e1.getY();
        if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (distanceX > 0)
                onSwipeRight();
            else
                onSwipeLeft();
            return true;
        }
        return false;
    }

    // if swipe left, reject current user and display next user
    public void onSwipeLeft() {
        swipeUserPos++;
        swipeUsers();
    }

    // if swipe right, match user and display next user
    public void onSwipeRight() {
        swipeUserPos++;
        //store matched user into database
        reference = FirebaseDatabase.getInstance().getReference("Matches").child(firebaseUser.getUid());
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id", userId);
        reference.child(userId).setValue(hashMap);

        swipeUsers();
    }

    // start the search for potential users
    public void setSearchFilter() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference prefReference = FirebaseDatabase.getInstance().getReference("Pref");
        prefReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Pref pref = snapshot.getValue(Pref.class);
                    assert pref.getId() != null;
                    if (pref.getId().equals(uid)) {
                        pref_type1 = pref.getGender();
                        pref_type2 = pref.getGenre();
                    }
                }
                matching();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // fetch users that fit the criteria
    public void matching() {
        user_reference = FirebaseDatabase.getInstance().getReference("Users");
        user_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    // TODO: I think these if statements are correct, otherwise I need to go to bed (03:29:58)
                    if (!user.getId().equals(firebaseUser.getUid())) {
                        if (pref_type1.equals("Any")) {
                            // "Any", "Any"
                            if (pref_type2.equals("Any")) {
                                profileList.add(user);
                            }
                            // "Any", "Genre"
                            else if (user.getGenres().equals(pref_type2)) {
                                profileList.add(user);
                            }
                        } else if (pref_type2.equals("Any")) {
                            // "Gender", "Any"
                            if (user.getGender().equals(pref_type1)) {
                                profileList.add(user);
                            }
                        }
                        // "Gender", "Genre"
                        else if (user.getGender().equals(pref_type1) && user.getGenres().equals(pref_type2)) {
                            profileList.add(user);
                        }
                    }
                }
                // TODO: do the ADAPTER here
                swipeUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //spotify
    @Override
    protected void onStart() {
        super.onStart();

        SpotifyAppRemote.disconnect(mSpotifyAppRemote);

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        //pause or resume playlist
                        btnPlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (spotifyUrl != null) {
                                    if (playSpotify) {
                                        startPlaylist();
                                        btnPlay.setText(R.string.pause_spotify_playlist);
                                        playSpotify = false;
                                    }

                                    mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
                                        if (playerState.isPaused) {
                                            mSpotifyAppRemote.getPlayerApi().resume();
                                            btnPlay.setText(R.string.pause_spotify_playlist);
                                            Log.d("MainActivity:", "playlist resumed");
                                        } else {
                                            mSpotifyAppRemote.getPlayerApi().pause();
                                            btnPlay.setText(R.string.play_spotify_playlist);
                                            Log.d("MainActivity:", "playlist paused");
                                        }
                                    });
                                } else {
                                    txtCurrentSong.setText(R.string.no_playlist_available);
                                }
                            }
                        });

                        btnNext.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (spotifyUrl != null) {
                                    mSpotifyAppRemote.getPlayerApi().skipNext();
                                    Log.d("MainActivity:", "playlist skipped next");
                                }
                            }
                        });

                        //skip to previous song in the playlist
                        btnPrevious.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (spotifyUrl != null) {
                                    mSpotifyAppRemote.getPlayerApi().skipPrevious();
                                    Log.d("MainActivity:", "playlist skipped previous");
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("Doesn't work :(", throwable.getMessage(), throwable);
                        Toast.makeText(SwipeActivity.this, R.string.toast, Toast.LENGTH_LONG).show();
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void startPlaylist() {
        //play the spotify playlist given by user
        spotifyUri = extractSpPlaylistID(spotifyUrl);
        mSpotifyAppRemote.getPlayerApi().play(spotifyUri);
        Log.d("MainActivity", "startPlaylist called");

        // Show Current Song Playing (Subscribe to PlayerState)
        mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            final Track track = playerState.track;
            if (track != null) {
                txtCurrentSong.setText(track.name + " by " + track.artist.name);
                Log.d("MainActivity", track.name + " by " + track.artist.name);
            }
        });
    }


}