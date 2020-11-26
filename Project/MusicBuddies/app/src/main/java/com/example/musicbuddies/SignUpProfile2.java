package com.example.musicbuddies;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.musicbuddies.model.User;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;

import com.spotify.protocol.types.Track;


public class SignUpProfile2 extends YouTubeBaseActivity {

    private Button btnConfirm, btnAddYoutube;
    private EditText txtYoutubeURL;
    private LinearLayout lnYoutube;
    private String key;
    private YouTubePlayerView youtubePlaylist;
    YouTubePlayer.OnInitializedListener youTubeOnlistener;

    //boilerplate for spotify connection
    private String CLIENT_ID = BuildConfig.SPOTIFY_KEY;
    private String REDIRECT_URI = "com.example.musicbuddies://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    //boilerplate for xml
    private Button btnPlay, btnNext, btnPrevious, btnAddSpotify;
    private EditText edtTxtSpotifyURL;
    private TextView txtCurrentSong;
    private FirebaseAuth auth;
    private String spotifyUri, spotifyUrl;
    private String youtubeUrl;
    private String[] userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_profile2);

        //Recovery request for youtube api
        final Integer RECOVERY_REQUEST = 1;

        //Check if intent has been passed and retrieve user info from Profile Page 1
        if (getIntent() != null) {
            if (getIntent().getExtras() != null) {
                Bundle b = this.getIntent().getExtras();
                userInfo = b.getStringArray("user");
            }
        }

        //initiate youtube variables
        txtYoutubeURL = (EditText) findViewById(R.id.txtYoutubeURL);
        btnAddYoutube = (Button) findViewById(R.id.btnAddYoutube);
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        lnYoutube = (LinearLayout) findViewById(R.id.lnYoutube);
        lnYoutube.setVisibility(LinearLayout.INVISIBLE);
        youtubePlaylist = (YouTubePlayerView) findViewById(R.id.ytbPlaylist);

        //Setup key for retrieving videos from youtube
        key = SignUpProfile2.this.getResources().getString(R.string.google_api_key);


        //initiate spotify variables
        btnAddSpotify = (Button) findViewById(R.id.btnAddSpotify);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        edtTxtSpotifyURL = (EditText) findViewById(R.id.edtTxtSpotifyURL);
        txtCurrentSong = (TextView) findViewById(R.id.txtCurrentSong);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnPrevious = (Button) findViewById(R.id.btnPrevious);

        //Youtube
        btnAddYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                youtubeUrl = txtYoutubeURL.getText().toString();
                if (!youtubeUrl.equals("")) {

                    //Checks if Youtube URL is valid if invalid, empty string is returned.
                    final String playlistID = extractYtPlaylistID(youtubeUrl);
                    if (!playlistID.equals("")) {

                        //Show youtube video
                        lnYoutube.setVisibility(LinearLayout.VISIBLE);
                        youTubeOnlistener = new YouTubePlayer.OnInitializedListener() {
                            @Override
                            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                                if (!b) {
                                    //load youtube playlist
                                    youTubePlayer.loadPlaylist(playlistID);
                                }
                            }

                            @Override
                            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                                if (youTubeInitializationResult.isUserRecoverableError()) {
                                    youTubeInitializationResult.getErrorDialog(SignUpProfile2.this, RECOVERY_REQUEST).show();
                                } else {
                                    String error = String.format("Error initializing YouTube player: " + youTubeInitializationResult.toString());
                                    Toast.makeText(SignUpProfile2.this, error, Toast.LENGTH_LONG).show();
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
                        txtYoutubeURL.setText("");

                        //hides keyboard from user
                        InputMethodManager keyboard = (InputMethodManager) getSystemService(SignUpProfile2.this.INPUT_METHOD_SERVICE);
                        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        Toast.makeText(SignUpProfile2.this, R.string.youtube_playlist_updated, Toast.LENGTH_SHORT).show();

                    } else {
                        youtubeUrl="";
                        txtYoutubeURL.setText("");
                        Toast.makeText(SignUpProfile2.this, R.string.playlist_url_is_invalid, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignUpProfile2.this, R.string.input_youtube, Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                // default search preference: "Any", "Any"
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("gender", "Any");
                hashMap.put("genre", "Any");
                hashMap.put("id", user.getUid());

                //add user profile to database
                User newUser = new User(userInfo[0], userInfo[1], userInfo[2], userInfo[3], userInfo[4], userInfo[5], spotifyUrl, youtubeUrl, user.getUid());
                databaseReference.child("Users").child(user.getUid()).setValue(newUser);
                databaseReference.child("Pref").child(user.getUid()).setValue(hashMap);
                Intent intent = new Intent(SignUpProfile2.this, SwipeActivity.class);
                startActivity(intent);
                finish();
            }
        });


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
                        btnAddSpotify.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                spotifyUrl = edtTxtSpotifyURL.getText().toString();
                                if (!spotifyUrl.equals("")) {
                                    //Checks if spotify URL is valid if invalid, empty string is returned.
                                    spotifyUri = extractSpPlaylistID(spotifyUrl);
                                    if (!spotifyUri.equals("")) {
                                        startPlaylist();
                                        Toast.makeText(SignUpProfile2.this, R.string.press_play_to_listen, Toast.LENGTH_SHORT).show();
                                    }else {
                                        spotifyUrl="";
                                        edtTxtSpotifyURL.setText("");
                                        Toast.makeText(SignUpProfile2.this, R.string.playlist_url_is_invalid, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(SignUpProfile2.this, R.string.input_spotify, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        //pause or play playlist
                        btnPlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                spotifyUrl = edtTxtSpotifyURL.getText().toString();
                                if (!spotifyUrl.equals("")) {
                                    btnPlay.setText(R.string.pause_playlist);
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

                        // skip to next song in the playlist
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
                        Toast.makeText(SignUpProfile2.this, R.string.toast, Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(SignUpProfile2.this, SignUpProfile1.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}