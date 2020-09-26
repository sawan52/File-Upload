package com.example.file_upload;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CreateAccountActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private EditText mEmailText, mPasswordText, firstName, lastName;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);

        mEmailText = findViewById(R.id.email_id);
        mPasswordText = findViewById(R.id.password);
        Button mSignUpButton = findViewById(R.id.signUp_button);

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignUp();
            }
        });
    }

    private void startSignUp() {

        String email = mEmailText.getText().toString();
        String password = mPasswordText.getText().toString();

        if (email.isEmpty()) {
            mEmailText.setError("Email is required");
            mEmailText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailText.setError("Enter a valid email");
            mEmailText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            mPasswordText.setError("Password is required");
            mPasswordText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            mPasswordText.setError("Minimum length of password should be 6");
            mPasswordText.requestFocus();
            return;
        }

        progressDialog.setMessage("Creating an account...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    createUserAccountAndSaveData();
                    Toast.makeText(CreateAccountActivity.this, "Your account created successfully", Toast.LENGTH_SHORT).show();
                    sendToMainActivity();
                } else {
                    progressDialog.dismiss();
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "The user account already exists", Toast.LENGTH_SHORT).show();
                    } else if (task.getException() instanceof FirebaseNetworkException) {
                        Toast.makeText(getApplicationContext(), "Check your Internet Connection", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Some Error Occurred, Please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void createUserAccountAndSaveData() {

        String currentUserID = firebaseAuth.getCurrentUser().getUid();

        String userFirstName = firstName.getText().toString();
        String userLastName = lastName.getText().toString();
        String userEmailID = mEmailText.getText().toString();
        String userPassword = mPasswordText.getText().toString();

        HashMap<String, String> userInfo = new HashMap<>();
        userInfo.put("userName", userFirstName + " " + userLastName);
        userInfo.put("userEmail", userEmailID);
        userInfo.put("userPassword", userPassword);

        databaseReference.child("Users Information").child(currentUserID).setValue(userInfo);
    }

    private void sendToMainActivity() {
        Intent mainActivityIntent = new Intent(CreateAccountActivity.this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }

}
