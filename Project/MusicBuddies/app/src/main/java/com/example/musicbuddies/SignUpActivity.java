package com.example.musicbuddies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    private Button btnSignUp;
    private EditText txtName, txtEmail, txtPassword, txtConfirmPass;
    private String TAG="SIGNUP";
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Set up variables
        auth = FirebaseAuth.getInstance();
        txtName = (EditText) findViewById(R.id.txtName);
        txtEmail= (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtConfirmPass = (EditText) findViewById(R.id.txtConfirmPass);
        btnSignUp = (Button) findViewById(R.id.btnConfirm);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Convert variables to strings
                String name = txtName.getText().toString();
                String email = txtEmail.getText().toString();
                String password= txtPassword.getText().toString();
                String confirmPass = txtConfirmPass.getText().toString();

                //check if password is correct and greater than 6 in length
                if(password.equals(confirmPass) & password.length()>=6) {
                    createUser(name, email, password);
                }else if(password.length()<6){
                    //Required length of passwords is 6
                    Toast.makeText(SignUpActivity.this, R.string.password_is_too_short, Toast.LENGTH_SHORT).show();
                    txtPassword.setText("");
                    txtConfirmPass.setText("");
                }
                else{
                    //Password is not confirmed
                    Toast.makeText(SignUpActivity.this, R.string.password_is_not_the_same, Toast.LENGTH_SHORT).show();
                    txtPassword.setText("");
                    txtConfirmPass.setText("");
                }
            }
        });
    }
    public void createUser(final String name, String email, String password){

        //Create user with email and password in Firebase
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");

                    //Move to Profile setup page
                    Intent intent = new Intent(SignUpActivity.this, SignUpProfile1.class);
                    intent.putExtra("Name", name);
                    startActivity(intent);

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(SignUpActivity.this, R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}