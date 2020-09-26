package com.example.file_upload;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;

public class MyRecyclerViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recycler_view);

        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setVisibility(View.GONE);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Uploaded Files").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                String fileName = dataSnapshot.getKey();
                String fileUrl = dataSnapshot.getValue(String.class);

                ((MyAdapter) recyclerView.getAdapter()).update(fileName, fileUrl);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(MyRecyclerViewActivity.this));

        MyAdapter myAdapter = new MyAdapter(recyclerView, MyRecyclerViewActivity.this, new ArrayList<String>(), new ArrayList<String>());
        recyclerView.setAdapter(myAdapter);
    }
}