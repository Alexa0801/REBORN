package com.example.aidforyou;

import android.Manifest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;

import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.search.SearchBar;

import java.io.IOException;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    SearchBar searchBar;
    private ListView listView;
    private ListView institutionsListView;
    private ListView entertainmentListView;
    private ListView healthListView;
    private ArrayAdapter<String> arrayAdapter;
    private final String[] categories = {
            "Institutions", "Shops", "Restaurants",
            "Pharmacy and other health related", "Entertainment"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        listView = findViewById(R.id.listView);
        institutionsListView = findViewById(R.id.institutionsListView);
        entertainmentListView = findViewById(R.id.entertainmentListView);
        healthListView = findViewById(R.id.healthListView);
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_customtext, categories);
        listView.setAdapter(arrayAdapter);
        searchBar = findViewById(R.id.searchBar);


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
                break;
            case "Entertainment":
                Toast.makeText(this.getApplicationContext(), "Selected: Entertainment", Toast.LENGTH_SHORT).show();
                break;
            case "Pharmacy and other health related":
                Toast.makeText(this.getApplicationContext(), "Selected: PPharmacy and other health related" , Toast.LENGTH_SHORT).show();
                break;
            case "Shops":
                Toast.makeText(this.getApplicationContext(), "Selected: Shops, no data for now", Toast.LENGTH_SHORT).show();
                break;
            case "Restaurants":
                Toast.makeText(this.getApplicationContext(), "Selected: Restaurants, no data for now" , Toast.LENGTH_SHORT).show();
                break;

        }
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
