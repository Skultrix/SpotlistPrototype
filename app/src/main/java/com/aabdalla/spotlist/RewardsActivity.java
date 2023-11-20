package com.aabdalla.spotlist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class RewardsActivity extends AppCompatActivity {

    private CardView resContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        findViewById(R.id.to_maps).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });

        findViewById(R.id.to_profile).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            finish();
        });

        findViewById(R.id.logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        resContainer = findViewById(R.id.reservation);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseFirestore.getInstance().collection("users").whereEqualTo("uid", user.getUid());

        query.limit(1).get().addOnSuccessListener(task -> {
            DocumentSnapshot doc = task.getDocuments().get(0);

            TextView pointsMessage = findViewById(R.id.points);

            Long points = doc.getLong("points");

            if (points == null) return;
            int pts = points.intValue();

            pointsMessage.setText("You currently have " + pts + " points.");
        });
    }


}