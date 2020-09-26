package com.example.file_upload;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogInActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        TextView createAccount = findViewById(R.id.create_account);
        mEmailField = findViewById(R.id.email_id);
        mPasswordField = findViewById(R.id.password);
        Button mSignInButton = findViewById(R.id.login_button);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendUserToCreateAccountActivity();
            }

        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignIn();
            }
        });
    }

    private void startSignIn() {

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        if (email.isEmpty()) {
            mEmailField.setError("Email is required");
            mEmailField.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailField.setError("Enter a valid email");
            mEmailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            mPasswordField.setError("Password is required");
            mPasswordField.requestFocus();
            return;
        }

        if (password.length() < 6) {
            mPasswordField.setError("Minimum length of password should be 6");
            mPasswordField.requestFocus();
            return;
        }

        progressDialog.setMessage("Signing In...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {

                    sendUserToMainActivity();
                    Toast.makeText(LogInActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                } else {
                    if (task.getException() instanceof FirebaseNetworkException) {
                        Toast.makeText(getApplicationContext(), "Check your Internet Connection", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Email or Password is wrong", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LogInActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToCreateAccountActivity() {
        Intent createAccountIntent = new Intent(LogInActivity.this, CreateAccountActivity.class);
        createAccountIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(createAccountIntent);
        finish();
    }

}