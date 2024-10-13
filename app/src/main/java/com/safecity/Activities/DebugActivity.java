package com.safecity.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.safecity.R;

public class DebugActivity extends AppCompatActivity {

    FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase SAFECITYDB = FirebaseDatabase.getInstance();

    TextInputEditText et;
    MaterialButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        initialize();

        btn.setOnClickListener(view -> {
            DatabaseReference dbDebug = SAFECITYDB.getReference("debug");
            dbDebug.push().setValue(et.getText().toString());
        });
    }

    private void initialize() {
        et = findViewById(R.id.et);
        btn = findViewById(R.id.btn);
    }
}