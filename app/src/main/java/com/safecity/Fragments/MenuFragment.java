package com.safecity.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.safecity.Activities.AuthenticationActivity;
import com.safecity.R;

public class MenuFragment extends Fragment {

    private FirebaseDatabase SAFECITY;
    private FirebaseUser USER;

    private void initializeFirebase() {
        SAFECITY = FirebaseDatabase.getInstance();
        USER = FirebaseAuth.getInstance().getCurrentUser();
    }

    View view;
    TextView tvEmail, tvName;
    MaterialButton btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_menu, container, false);

        initializeFirebase();
        initialize();
        loadUserDetails();

        btnLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), AuthenticationActivity.class));
        });

        return view;
    }

    private void loadUserDetails() {
        DatabaseReference dbUser = SAFECITY.getReference("user_"+USER.getUid());
        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvEmail.setText(USER.getEmail());
                tvName.setText(snapshot.child("firstName").getValue().toString() + " " + snapshot.child("lastName").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initialize() {
        btnLogout = view.findViewById(R.id.btnLogout);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvName = view.findViewById(R.id.tvName);
    }
}