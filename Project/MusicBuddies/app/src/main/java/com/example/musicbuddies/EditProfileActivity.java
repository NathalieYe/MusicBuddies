package com.example.musicbuddies;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicbuddies.model.User;
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
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    // hamburger /sliding menu boilerplate
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    //Uploading profile pictures
    private CircleImageView profileImageView;
    private StorageReference storageReference;
    private FirebaseUser user;
    private StorageReference fileRef;

    //Variables for holding data from database
    private FirebaseAuth auth;
    private String name, gender, genres, artist, bio, spotifyUri, spotifyUrl, youtubeUrl, birthday;

    //Variables for xml
    private DatePickerDialog picker;
    private TextView txtBirthday, txtCurrentSong;
    private EditText txtName, txtArtists, txtBio, edtTxtSpotifyURL, txtYoutubeURL;
    private String key;
    private Spinner spGender, spGenres;
    private Button btnPlay, btnNext, btnPrevious, btnAddSpotify, btnConfirm, btnAddYoutube;

    //Youtube vaiables
    private LinearLayout lnYoutube;
    private YouTubePlayerSupportFragment youtubePlaylist;
    YouTubePlayer.OnInitializedListener youTubeOnlistener;

    //Spotify variables
    private SpotifyAppRemote mSpotifyAppRemote;
    private String CLIENT_ID = BuildConfig.SPOTIFY_KEY;
    private String REDIRECT_URI = "com.example.musicbuddies://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        //Recovery request for youtube api
        final Integer RECOVERY_REQUEST = 1;

        //Get current user auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        //Retrieve storage information for storing and retrieving photos
        storageReference = FirebaseStorage.getInstance().getReference();
        profileImageView = (CircleImageView) findViewById(R.id.profileImageView);

        //Check if image is in storage
        try {
            fileRef = storageReference.child(user.getUid()).child("profile.jpeg");
            setImageFromFirebase();
        } catch (Exception e) {
            //User has not uploaded image
        }

        //Initiating xml variables
        txtName = (EditText) findViewById(R.id.txtName);
        spGender = (Spinner) findViewById(R.id.spGender);
        txtBirthday = (TextView) findViewById(R.id.txtBirthday);
        spGenres = (Spinner) findViewById(R.id.spGenres);
        txtArtists = (EditText) findViewById(R.id.txtArtists);
        txtBio = (EditText) findViewById(R.id.txtBio);

        //Setup key for retrieving videos from youtube
        key = EditProfileActivity.this.getResources().getString(R.string.google_api_key);

        //Variables for setting up youtube
        txtYoutubeURL = (EditText) findViewById(R.id.txtYoutubeURL);
        btnAddYoutube = (Button) findViewById(R.id.btnAddYoutube);
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        lnYoutube = (LinearLayout) findViewById(R.id.lnYoutube);
        lnYoutube.setVisibility(LinearLayout.INVISIBLE);
        youtubePlaylist = (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.ytbPlaylist);

        //Variables for setting up Spotify
        btnAddSpotify = (Button) findViewById(R.id.btnAddSpotify);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        edtTxtSpotifyURL = (EditText) findViewById(R.id.edtTxtSpotifyURL);
        txtCurrentSong = (TextView) findViewById(R.id.txtCurrentSong);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnPrevious = (Button) findViewById(R.id.btnPrevious);

        btnConfirm = (Button) findViewById(R.id.btnConfirm);

        //Setting up adapters fo genre and gender adapters
        ArrayAdapter<CharSequence> genresAdapter = ArrayAdapter.createFromResource(this,
                R.array.genres, R.layout.spinner_item);
        genresAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spGenres.setAdapter(genresAdapter);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.genders, R.layout.spinner_item);
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        //Get database instance for retrieving user profile information
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference dbRefGroup = databaseReference.child("Users").child(user.getUid());
        dbRefGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userInfo = dataSnapshot.getValue(User.class);
                assert userInfo != null;
                name = userInfo.getName();
                gender = userInfo.getGender();
                genres = userInfo.getGenres();
                artist = userInfo.getArtists();
                bio = userInfo.getBio();
                spotifyUrl = userInfo.getSpotifyPlaylistUrl();
                youtubeUrl = userInfo.getYoutubePlaylistURL();
                birthday = userInfo.getBirthday();
                txtName.setText(name);
                int genrePosition = genresAdapter.getPosition(genres);
                spGenres.setSelection(genrePosition);
                int genderPosition = genderAdapter.getPosition(gender);
                spGender.setSelection(genderPosition);
                txtBirthday.setText(birthday);
                txtArtists.setText(artist);
                txtBio.setText(bio);
                edtTxtSpotifyURL.setText(spotifyUrl);
                txtYoutubeURL.setText(youtubeUrl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Add profile picture
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

        //Add a new Youtube video
        btnAddYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                youtubeUrl = txtYoutubeURL.getText().toString();
                    if (!youtubeUrl.equals("")) {

                        //Checks if youtube URL is valid if invalid, empty string is returned.
                        final String playlistID = extractYtPlaylistID(youtubeUrl);
                        if (!playlistID.equals("")) {
                            //Show youtube video
                            lnYoutube.setVisibility(LinearLayout.VISIBLE);
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
                                        youTubeInitializationResult.getErrorDialog(EditProfileActivity.this, RECOVERY_REQUEST).show();
                                    } else {
                                        String error = String.format("Error initializing YouTube player: " + youTubeInitializationResult.toString());
                                        Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_LONG).show();
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
                            InputMethodManager keyboard = (InputMethodManager) getSystemService(EditProfileActivity.this.INPUT_METHOD_SERVICE);
                            keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        } else {
                            youtubeUrl="";
                            txtYoutubeURL.setText("");
                            Toast.makeText(EditProfileActivity.this, R.string.playlist_url_is_invalid, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditProfileActivity.this, R.string.input_youtube, Toast.LENGTH_SHORT).show();
                    }
                }
        });

        //Retrieve genre information from spinner
        spGenres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                genres = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Retrieve gender information from spinner
        spGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Create calendar for retrieving birthday
        txtBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hide keyboard from user
                InputMethodManager keyboard = (InputMethodManager) getSystemService(EditProfileActivity.this.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
                final Calendar cld = Calendar.getInstance();
                int day = cld.get(Calendar.DAY_OF_MONTH);
                int month = cld.get(Calendar.MONTH);
                int year = cld.get(Calendar.YEAR);

                //Calendar dialog selection set to textview
                picker = new DatePickerDialog(EditProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        txtBirthday.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
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
                        Intent intent = new Intent(getApplicationContext(), SwipeActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.item2: // chats
                        Intent intent2 = new Intent(getApplicationContext(), AllChatsActivity.class);
                        startActivity(intent2);
                        return true;
                    case R.id.item3: // edit profile
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

        //Update profile information
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                birthday = txtBirthday.getText().toString();
                name = txtName.getText().toString();
                artist = txtArtists.getText().toString();
                bio = txtBio.getText().toString();

                //add user profile to database
                User newUser = new User(name, birthday, gender, genres, artist, bio, spotifyUrl, youtubeUrl, user.getUid());
                databaseReference.child("Users").child(user.getUid()).setValue(newUser);
                Toast.makeText(EditProfileActivity.this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
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

    public static String extractYtPlaylistID(String strLink) {
        //retrieves playlist id from youtube url
        String reg = "(?:youtube(?:-nocookie)?\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]list=)|youtu\\.be\\/)([a-zA-Z0-9_-]{34})";
        Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(strLink);

        if (matcher.find())
            return matcher.group(1);
        return "";
    }

    public static String extractSpPlaylistID(String strLink) {
        //retrieves uri from spotify url
        String[] linkSplit = strLink.split("/");
        if(linkSplit.length>4) {
            if (linkSplit[3].equals("playlist") && linkSplit[2].equals("open.spotify.com")) {
                String[] playlistID = linkSplit[4].split("\\?");
                return "spotify:user:spotify:playlist:" + playlistID[0];
            }
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();
                profileImageView.setImageURI(imageUri);
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // upload image to Firebase Storage
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(EditProfileActivity.this, R.string.image_uploaded, Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfileActivity.this, R.string.image_not_uploaded, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setImageFromFirebase() {
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImageView);
            }
        });

    }

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
                        Log.d("EditProfileActivity", "Connected! Yay!");

                        // when connected, play the playlist the user entered
                        btnAddSpotify.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                spotifyUrl = edtTxtSpotifyURL.getText().toString();
                                if (!spotifyUrl.equals("")) {
                                    //Checks if spotify URL is valid if invalid, empty string is returned.
                                    spotifyUri = extractSpPlaylistID(spotifyUrl);
                                    if (!spotifyUri.equals("")) {
                                        startPlaylist();
                                        Toast.makeText(EditProfileActivity.this, R.string.press_play_to_listen, Toast.LENGTH_SHORT).show();
                                    } else {
                                        spotifyUrl="";
                                        edtTxtSpotifyURL.setText("");
                                        Toast.makeText(EditProfileActivity.this, R.string.playlist_url_is_invalid, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(EditProfileActivity.this, R.string.input_spotify, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        //pause or resume playlist
                        btnPlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                spotifyUrl = edtTxtSpotifyURL.getText().toString();
                                if (!spotifyUrl.equals("")) {
                                    btnPlay.setText(R.string.play_playlist);
                                    mSpotifyAppRemote.getPlayerApi().getPlayerState().setResultCallback(playerState -> {
                                        if (playerState.isPaused) {
                                            mSpotifyAppRemote.getPlayerApi().resume();
                                            btnPlay.setText(R.string.pause_playlist);
                                            Log.d("MainActivity:", "playlist resumed");
                                        } else {
                                            mSpotifyAppRemote.getPlayerApi().pause();
                                            btnPlay.setText(R.string.play_playlist);
                                            Log.d("MainActivity:", "playlist paused");
                                        }
                                    });
                                }
                            }
                        });
                        btnNext.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                spotifyUrl = edtTxtSpotifyURL.getText().toString();
                                if (!spotifyUrl.equals("")) {
                                    mSpotifyAppRemote.getPlayerApi().skipNext();
                                    Log.d("MainActivity:", "playlist skipped next");
                                }
                            }
                        });

                        //skip to previous song in the playlist
                        btnPrevious.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                spotifyUrl = edtTxtSpotifyURL.getText().toString();
                                if (!spotifyUrl.equals("")) {
                                    mSpotifyAppRemote.getPlayerApi().skipPrevious();
                                    Log.d("MainActivity:", "playlist skipped previous");
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("Doesn't work :(", throwable.getMessage(), throwable);
                        Toast.makeText(EditProfileActivity.this, R.string.toast, Toast.LENGTH_LONG).show();
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
        mSpotifyAppRemote.getPlayerApi().play(spotifyUri);
        mSpotifyAppRemote.getPlayerApi().pause();
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