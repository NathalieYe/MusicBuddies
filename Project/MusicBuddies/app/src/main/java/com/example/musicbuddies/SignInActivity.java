package com.example.musicbuddies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    private Button btnSignUp, btnSignIn;
    private FirebaseAuth auth;
    private EditText txtEmail, txtPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //Initialize xml variables
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        auth = FirebaseAuth.getInstance();

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txtEmail.getText().toString();
                String password = txtPassword.getText().toString();

                //Check if email or password input is empty
                if (email.equals("") || email.isEmpty()) {
                    Toast.makeText(SignInActivity.this, R.string.please_enter_your_email, Toast.LENGTH_SHORT).show();
                } else if (password.equals("") || password.isEmpty()) {
                    Toast.makeText(SignInActivity.this, R.string.please_enter_your_password, Toast.LENGTH_SHORT).show();
                } else {

                    //Check with firebase to see if user exists in database
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                //Reset texts if authentication fails
                                txtEmail.setText("");
                                txtPassword.setText("");
                                Toast.makeText(SignInActivity.this, R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                            } else {
                                //Go into Swipe page after use is authenticated
                                Intent intent = new Intent(SignInActivity.this, SwipeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

                }
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Go to Sign Up page
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }
}