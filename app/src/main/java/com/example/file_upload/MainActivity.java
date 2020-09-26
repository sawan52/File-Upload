package com.example.file_upload;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    private static final int READ_WRITE_PERMISSION = 9;
    private static final int FILE_SELECTION_CONFIRMED = 86;
    private EditText savedFileName;
    private TextView notification, userNameText;
    private Uri pdfUri; // uri are actually URLs which are meant for local storage
    private String downloadUrl;
    private FirebaseAuth mAuth;
    private FirebaseUser mFireBaseCurrentUser;
    private FirebaseStorage storage; // used for uploading... Ex: pdf
    private FirebaseDatabase database; // used to store URLs of uploaded files...
    private ProgressDialog progressDialog;
    private ImageView userImageIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(null);

        userNameText = findViewById(R.id.userName);
        userImageIcon = findViewById(R.id.userIcon);
        savedFileName = findViewById(R.id.file_name);
        Button fetch = findViewById(R.id.fetch_files);

        mAuth = FirebaseAuth.getInstance();
        mFireBaseCurrentUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance(); // returns an object of FireBase Storage
        database = FirebaseDatabase.getInstance(); // returns an object of FireBase database

        Button selectFile = findViewById(R.id.selectFile);
        Button upload = findViewById(R.id.upload);
        notification = findViewById(R.id.notification);


        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MyRecyclerViewActivity.class));
            }
        });

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectPdf();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_PERMISSION);
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pdfUri != null) { // the user has selected a file...
                    uploadFile(pdfUri);
                } else {
                    Toast.makeText(MainActivity.this, "Select a File first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mFireBaseCurrentUser == null) {
            sendUserToLoginActivity();
        } else {
            displayUserNameOnAppBar();
            Toast.makeText(MainActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayUserNameOnAppBar() {

        String currentUserID = mFireBaseCurrentUser.getUid();
        DatabaseReference mDatabaseReference = database.getReference();

        mDatabaseReference.child("Users Information").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.hasChild("userName")) {

                    String retrieveUserName = dataSnapshot.child("userName").getValue().toString();
                    userNameText.setText(retrieveUserName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void uploadFile(Uri pdfUri) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading File...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        final String fileName = savedFileName.getText().toString() + "";
        final String fileNameWithPdf = savedFileName.getText().toString() + ".pdf";

        final StorageReference storageReference = storage.getReference().child("Uploaded Files").child(fileNameWithPdf); // returns root path

        final UploadTask uploadTask = storageReference.putFile(pdfUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "File uploaded successfully...", Toast.LENGTH_SHORT).show();

                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        downloadUrl = storageReference.getDownloadUrl().toString();
                        return storageReference.getDownloadUrl();

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {
                            downloadUrl = task.getResult().toString();
                            DatabaseReference databaseReference = database.getReference();
                            databaseReference.child("Uploaded Files").child(fileName).setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "File Url saved Successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "File not Uploaded...", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                int progress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage(progress + "% Uploaded...");
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectPdf();
        } else {
            Toast.makeText(MainActivity.this, "Please provide permission first", Toast.LENGTH_SHORT).show();
        }
    }


    private void selectPdf() {
        // to offer a user to select a file using file manager
        // we will be using an Intent
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT); // to fetch files...
        startActivityForResult(intent, FILE_SELECTION_CONFIRMED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // check whether a user has selected a file or not... Ex: pdf
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData(); // return the Uri for selected file...
            notification.setText(data.getData().getLastPathSegment());
        } else {
            Toast.makeText(MainActivity.this, "Please select a file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_feedback:
                composeEmail();
                break;

            case R.id.menu_logout:
                mAuth.signOut();
                finish();
                startActivity(new Intent(MainActivity.this, LogInActivity.class));
                break;

        }
        return true;
    }

    // open an email intent
    public void composeEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:gfirebase86@gmail.com"));// only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback about File Upload App");
        startActivity(intent);
    }


    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LogInActivity.class);
        startActivity(loginIntent);
        finish();
    }

}
