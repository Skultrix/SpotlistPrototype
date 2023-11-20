package com.aabdalla.spotlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public static Map<Integer, Triple<String, Integer, Integer>> SPOT_INFO = new HashMap<>();
    public static Map<Integer, LatLng> LOCATIONS = new HashMap<>();

    static {
        SPOT_INFO.put(0, new Triple<>("\"This area is littered with cardboard boxes," +
                " plastic cups, and other plastic items. " +
                "It is near QNL on the north side.\"", 35, 2));

        SPOT_INFO.put(1, new Triple<>("\"There are plastic cups here on the ground" +
                " and small boxes. It is in the south-side parking lot of Multaqa. " +
                "\"", 50, 3));

        SPOT_INFO.put(2, new Triple<>("\"This area contains fallen tree sticks " +
                "and other small amounts of litter such as plastic bottles and cups. " +
                "It is near the OakBerry stand in the parking lot." +
                "\"", 20, 1));

        LOCATIONS.put(0, new LatLng(25.319059, 51.442011));
        LOCATIONS.put(1, new LatLng(25.315475, 51.442070));
        LOCATIONS.put(2, new LatLng(25.312575, 51.440063));
    }

    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return;
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        findViewById(R.id.to_rewards).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RewardsActivity.class));
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

        TextView welcome = findViewById(R.id.welcome);

        String uid = auth.getCurrentUser().getUid();
        Query query = db.collection("users").whereEqualTo("uid", uid);

        query.limit(1).get().addOnSuccessListener(task -> {
            DocumentSnapshot doc = task.getDocuments().get(0);
            String username = doc.getString("username");
            Log.e("d", username);
            welcome.setText(getString(R.string.welcome, username));
        });

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng cmu = new LatLng(25.316765, 51.440299);

        loadUserMarkers(googleMap);

        googleMap.setOnMarkerClickListener(this);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(cmu));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_marker_info, null);

        TextView desc = popupView.findViewById(R.id.description);
        desc.setText(SPOT_INFO.get(marker.getTag()).x);

        TextView points = popupView.findViewById(R.id.points_amount);
        points.setText("Earn " + SPOT_INFO.get(marker.getTag()).y + " points for completion");

        TextView difficulty = popupView.findViewById(R.id.difficulty);
        difficulty.setText("This is a difficulty level-" + SPOT_INFO.get(marker.getTag()).z + " level job");

        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, 900, 1173, focusable);

        popupWindow.showAtLocation(findViewById(R.id.defView), Gravity.CENTER, 0, 0);

        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });

        popupView.findViewById(R.id.button).setOnClickListener(task -> {
            //Check if user already has one
            String uid = auth.getCurrentUser().getUid();
            Query query = db.collection("users").whereEqualTo("uid", uid);

            query.limit(1).get().addOnSuccessListener(t -> {
                DocumentSnapshot doc = t.getDocuments().get(0);
                long reservation = (long) doc.get("reservation");

                if (reservation != -1) {
                    popupWindow.dismiss();
                    Toast.makeText(this, "You already have a reservation!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //hide marker and set reservation

                int tag = (int) marker.getTag();

                List<Long> show = (List<Long>) doc.get("show");
                show.remove(tag);
                String docTitle = doc.getId();

                db.collection("users").document(docTitle).update("reservation", tag);
                db.collection("users").document(docTitle).update("show", show);

                marker.setVisible(false);
                popupWindow.dismiss();
                Toast.makeText(this, "You have a new reservation!", Toast.LENGTH_LONG).show();
            });
        });


        return false;
    }

    private void loadUserMarkers(GoogleMap map) {
        Query query = db.collection("users").whereEqualTo("uid", auth.getCurrentUser().getUid());

        query.limit(1).get().addOnSuccessListener(t -> {
            DocumentSnapshot doc = t.getDocuments().get(0);
            List<Long> show = (List<Long>) doc.get("show");

            for (long toShow : show) {
                map.addMarker(new MarkerOptions().position(LOCATIONS.get((int) toShow))).setTag((int) toShow);
            }
        });
    }
}