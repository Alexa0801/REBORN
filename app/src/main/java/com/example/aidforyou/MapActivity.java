package com.example.aidforyou;

import android.Manifest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;

import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.search.SearchBar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    SearchBar searchBar;
    private ListView listView;
    private ListView institutionsListView;
    private ListView entertainmentListView;
    private ListView healthListView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayAdapter<String> institutionsArrayAdapter;
    private ArrayAdapter<String> entertainmentArrayAdapter;
    private ArrayAdapter<String> healthArrayAdapter;
    private AlertDialog.Builder alert;

    private final String[] categories = {
            "Institutions", "Shops", "Restaurants",
            "Pharmacy and other health related", "Entertainment"
    };

    private final ArrayList<String> institutions = new ArrayList<>();
    private final ArrayList<String> entertainment = new ArrayList<>();
    private final ArrayList<String> health = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        alert = new AlertDialog.Builder(this);


        listView = findViewById(R.id.listView);
        institutionsListView = findViewById(R.id.institutionsListView);
        entertainmentListView = findViewById(R.id.entertainmentListView);
        healthListView = findViewById(R.id.healthListView);

        arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_customtext, categories);
        listView.setAdapter(arrayAdapter);

        searchBar = findViewById(R.id.searchBar);

        getInstitutions();
        getEntertainment();
        getHealth();

        institutionsArrayAdapter = new ArrayAdapter<>(this, R.layout.list_customtext, institutions);
        entertainmentArrayAdapter = new ArrayAdapter<>(this, R.layout.list_customtext, entertainment);
        healthArrayAdapter = new ArrayAdapter<>(this, R.layout.list_customtext, health);

        institutionsListView.setAdapter(institutionsArrayAdapter);
        entertainmentListView.setAdapter(entertainmentArrayAdapter);
        healthListView.setAdapter(healthArrayAdapter);

        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setVisibility(View.VISIBLE);
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            handleInput(selectedItem);
            listView.setVisibility(View.GONE);
        });
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        checkLocationPermission();
    }

    private void getInstitutions(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Institutions");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    String name = (String) ds.getKey();
                    institutions.add(name);
                }
                institutionsArrayAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getEntertainment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Entertainment");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    String name = (String) ds.getKey();
                    entertainment.add(name);
                }
                entertainmentArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getHealth(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Pharmacy&other");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    String name = (String) ds.getKey();
                    health.add(name);
                }
                healthArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    private void handleInput(String selectedItem){
        switch (selectedItem){
            case "Institutions":
                Toast.makeText(this.getApplicationContext(), "Selected: Institutions", Toast.LENGTH_SHORT).show();
                Log.e("Institutions", institutions.toString());
                institutionsClicked();
                break;
            case "Entertainment":
                Toast.makeText(this.getApplicationContext(), "Selected: Entertainment", Toast.LENGTH_SHORT).show();
                entertainmentClicked();
                break;
            case "Pharmacy and other health related":
                Toast.makeText(this.getApplicationContext(), "Selected: PPharmacy and other health related" , Toast.LENGTH_SHORT).show();
                healthClicked();
                break;
            case "Shops":
                Toast.makeText(this.getApplicationContext(), "Selected: Shops, no data for now", Toast.LENGTH_SHORT).show();
                break;
            case "Restaurants":
                Toast.makeText(this.getApplicationContext(), "Selected: Restaurants, no data for now" , Toast.LENGTH_SHORT).show();
                break;

        }
    }

    private void institutionsClicked() {
        institutionsListView.setVisibility(View.VISIBLE);
        institutionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedInstitution = institutions.get(position);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Institutions");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren())
                        {
                            if(Objects.equals(ds.getKey(), selectedInstitution)){
                                double latitude = (double) ds.child("latitude").getValue();
                                double longitude = (double) ds.child("longitude").getValue();
                                LatLng latLng = new LatLng(latitude, longitude);
                                googleMap.clear();
                                currentLocation();
                                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(latLng));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                                handleOpenMaps(selectedInstitution, latLng);
                            }

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Toast.makeText(MapActivity.this, "Clicked: " + selectedInstitution, Toast.LENGTH_SHORT).show();
                institutionsListView.setVisibility(View.GONE);
            }
        });
    }



    private void entertainmentClicked(){
        entertainmentListView.setVisibility(View.VISIBLE);
        entertainmentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedEntertainment = entertainment.get(position);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Entertainment");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren())
                        {
                            if(Objects.equals(ds.getKey(), selectedEntertainment)){
                                double latitude = (double) ds.child("latitude").getValue();
                                double longitude = (double) ds.child("longitude").getValue();
                                LatLng latLng = new LatLng(latitude, longitude);
                                googleMap.clear();
                                currentLocation();
                                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(latLng));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                                handleOpenMaps(selectedEntertainment, latLng);
                            }

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Toast.makeText(MapActivity.this, "Clicked: " + selectedEntertainment, Toast.LENGTH_SHORT).show();
                entertainmentListView.setVisibility(View.GONE);
            }
        });
    }

    private void healthClicked(){
        healthListView.setVisibility(View.VISIBLE);
        healthListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedHealth = health.get(position);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Pharmacy&other");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren())
                        {
                            if(Objects.equals(ds.getKey(), selectedHealth)){
                                double latitude = (double) ds.child("latitude").getValue();
                                double longitude = (double) ds.child("longitude").getValue();
                                LatLng latLng = new LatLng(latitude, longitude);
                                googleMap.clear();
                                currentLocation();
                                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).position(latLng));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                                handleOpenMaps(selectedHealth, latLng);
                            }

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Toast.makeText(MapActivity.this, "Clicked: " + selectedHealth, Toast.LENGTH_SHORT).show();
                healthListView.setVisibility(View.GONE);
            }
        });
    }

    private void handleOpenMaps(String selectedItem, LatLng latLng){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                alert.setTitle(selectedItem)
                        .setMessage("If you want to go to " + selectedItem + " press yes")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Uri gmmIntentUri = Uri.parse("geo:" + latLng.latitude + "," + latLng.longitude + "?q=" + latLng.latitude + "," + latLng.longitude);
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");

                                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .create().show();
            }
        }, 1500);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(googleMap);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        currentLocationWithZoom();
    }

    @SuppressLint("MissingPermission")
    private void currentLocationWithZoom(){
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .icon(setIcon(this, R.drawable.outline_emoji_people_24)));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void currentLocation(){
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .icon(setIcon(this, R.drawable.outline_emoji_people_24)));

            }
        });
    }
    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(location, 1);
            if (!addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(latLng).title(location));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BitmapDescriptor setIcon(Activity context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) return null;

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
