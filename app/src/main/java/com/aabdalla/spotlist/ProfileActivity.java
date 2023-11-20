package com.aabdalla.spotlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private CardView resHolder;
    private CardView placeholder;
    private CardView completed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.to_maps).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });

        findViewById(R.id.to_rewards).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RewardsActivity.class));
            finish();
        });

        findViewById(R.id.logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        if (auth.getCurrentUser() == null) {
            Log.e("Error", "User null in Profile.");
            finish();
            return;
        }

        resHolder = findViewById(R.id.reservation);
        placeholder = findViewById(R.id.res_placeholder);
        completed = findViewById(R.id.finished);

        loadReservation(auth.getCurrentUser());

        TextView unreserve = findViewById(R.id.unreserve);
        Button complete = findViewById(R.id.complete);



        unreserve.setOnClickListener(t -> unreserve(auth.getCurrentUser()));
        complete.setOnClickListener(t -> complete(auth.getCurrentUser()));
    }

    private void loadReservation(@NonNull FirebaseUser user) {
        Query query = db.collection("users").whereEqualTo("uid", user.getUid());

        TextView descView = findViewById(R.id.profile_desc);

        query.limit(1).get().addOnSuccessListener(task -> {
            DocumentSnapshot doc = task.getDocuments().get(0);

            Long tag = doc.getLong("reservation");

            if (tag == null) return;
            int index = tag.intValue();

            if (index == -1) return;

            //Show the reservation banner
            resHolder.setVisibility(View.VISIBLE);
            placeholder.setVisibility(View.INVISIBLE);

            String desc = Objects.requireNonNull(MainActivity.SPOT_INFO.get(index)).x;

            descView.setText(desc);
        });
    }

    private void unreserve(@NonNull FirebaseUser user) {
        Query query = db.collection("users").whereEqualTo("uid", user.getUid());

        query.limit(1).get().addOnSuccessListener(task -> {
            DocumentSnapshot doc = task.getDocuments().get(0);

            resHolder.setVisibility(View.INVISIBLE);
            placeholder.setVisibility(View.VISIBLE);

            Long tag = doc.getLong("reservation");

            if (tag == null) return;
            int index = tag.intValue();

            db.collection("users").document(doc.getString("username")).update("reservation", -1);

            List<Long> show = (List<Long>) doc.get("show");
            show.add(tag);

            db.collection("users").document(doc.getString("username")).update("show", show);
        });
    }

    private void complete(@NonNull FirebaseUser user) {
        //Show nice pop-up
        Query query = db.collection("users").whereEqualTo("uid", user.getUid());

        query.limit(1).get().addOnSuccessListener(task -> {
            DocumentSnapshot doc = task.getDocuments().get(0);

            resHolder.setVisibility(View.INVISIBLE);
            completed.setVisibility(View.VISIBLE);

            Long tag = doc.getLong("reservation");
            Long pts = doc.getLong("points");

            if (tag == null) return;
            int index = tag.intValue();

            int toAdd = MainActivity.SPOT_INFO.get(index).y;

            ((TextView) findViewById(R.id.points_count)).setText("+" + toAdd + " points have been added to your profile");

            db.collection("users").document(doc.getString("username")).update("points", toAdd + pts);
            db.collection("users").document(doc.getString("username")).update("reservation", -1);
        });
    }
}