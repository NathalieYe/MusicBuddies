package com.example.musicbuddies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpProfile1 extends AppCompatActivity {
    DatePickerDialog picker;
    private Button btnNext;
    private TextView txtBirthday, txtVwName, txtVwUploadPhoto;
    private EditText txtArtists, txtBio;
    private String gender, genres;
    private CircleImageView profileImageView;
    private StorageReference storageReference;

    private FirebaseAuth auth;

    //spinner for gender and genre
    private Spinner spGender, spGenres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_profile1);

        //Setup variables
        txtVwName = (TextView) findViewById(R.id.txtVwName);
        spGender = (Spinner) findViewById(R.id.spGender);
        txtBirthday = (TextView) findViewById(R.id.txtBirthday);
        spGenres = (Spinner) findViewById(R.id.spGenres);
        txtArtists = (EditText) findViewById(R.id.txtArtists);
        txtBio = (EditText) findViewById(R.id.txtBio);

        txtVwUploadPhoto = (TextView) findViewById(R.id.txtVwUploadPhoto);
        profileImageView = (CircleImageView) findViewById(R.id.profileImageView);
        storageReference = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();


        //Retrieve user's name from previous activity
        Intent intent = getIntent();
        String name = intent.getStringExtra("Name");
        txtVwName.setText(name);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open image gallery in phone to upload photo
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

        //When textbox clicked, calender is displayed for user to choose calendar
        txtBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hide keyboard from user
                InputMethodManager keyboard = (InputMethodManager) getSystemService(SignUpProfile1.this.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
                final Calendar cld = Calendar.getInstance();
                int day = cld.get(Calendar.DAY_OF_MONTH);
                int month = cld.get(Calendar.MONTH);
                int year = cld.get(Calendar.YEAR);

                //Calendar dialog selection set to textview
                picker = new DatePickerDialog(SignUpProfile1.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        txtBirthday.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });

        //Set up array adapter for gender spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.genders, R.layout.spinner_item);
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);
        spGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Retrieve selected gender
                gender = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Set up array adapter for genre spinner
        ArrayAdapter<CharSequence> genresAdapter = ArrayAdapter.createFromResource(this,
                R.array.genres, R.layout.spinner_item);
        genresAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spGenres.setAdapter(genresAdapter);
        spGenres.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Retrieve selected genre
                genres = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Retrieve user info and sent to profile page 2
                String birthday = txtBirthday.getText().toString();
                String artists = txtArtists.getText().toString();
                String bio = txtBio.getText().toString();
                if (gender.equals("") || birthday.equals("") || genres.equals("") || artists.equals("") || bio.equals("")) {
                    Toast.makeText(SignUpProfile1.this, R.string.please_fill_in_empty_fields, Toast.LENGTH_SHORT).show();
                } else {
                    //Create String Array to hold user info
                    Bundle b = new Bundle();
                    b.putStringArray("user", new String[]{name, birthday, gender, genres, artists, bio});
                    Intent intent = new Intent(SignUpProfile1.this, SignUpProfile2.class);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                //Get image information of uploaded photo and set profile picture
                Uri imageUri = data.getData();
                profileImageView.setImageURI(imageUri);
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // Upload image to Firebase Storage
        FirebaseUser user = auth.getCurrentUser();
        StorageReference fileRef = storageReference.child(user.getUid()).child("profile.jpeg");
        txtVwUploadPhoto.setVisibility(TextView.INVISIBLE);
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(SignUpProfile1.this, R.string.image_uploaded, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpProfile1.this, R.string.image_not_uploaded, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Prevent users from accessing signup page again
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}