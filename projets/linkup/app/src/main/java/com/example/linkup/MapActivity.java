package com.example.linkup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.util.GeoPoint;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialisation de la MapView
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);  // Source de la carte (OpenStreetMap)

        // Initialiser le client de localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Demander la position actuelle de l'utilisateur
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lng = location.getLongitude();

                            // Afficher la position actuelle sur la carte
                            GeoPoint currentLocation = new GeoPoint(lat, lng);
                            mapView.getController().setCenter(currentLocation);
                            mapView.getController().setZoom(15); // Zoom sur la carte

                            // Placer un marker à la position actuelle
                            Marker currentMarker = new Marker(mapView);
                            currentMarker.setPosition(currentLocation);
                            mapView.getOverlays().add(currentMarker);

                            // Optionnel : Afficher un toast pour la position
                            Toast.makeText(MapActivity.this, "Latitude: " + lat + ", Longitude: " + lng, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
