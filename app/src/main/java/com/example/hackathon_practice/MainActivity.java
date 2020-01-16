package com.example.hackathon_practice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Button btnShow,btnNear;
    private GoogleMap mMap;
    private Location mLocation,farLocation;
    private LocationManager mlocationManager;
    private LocationRequest mlocationRequest;
    private LocationListener listener;
    private long UPDATE_INTERVAL = 20;
    private long FASTEST_intervall = 2000;
    private LocationManager locationManager;
    private boolean isPermission;
    private GoogleApiClient mGoogleApiClient;
    private Marker currentLocationMarker;
    private float start_rotation;
    int PROXIMITY_RADIUS = 10000;
    LatLng latLng;
    float distance;
    ArrayList<Float> distances ;
    String string;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnShow = findViewById(R.id.btnShow);
        btnNear = findViewById(R.id.btnNear);

        if (requestSinglePermission()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
            mapFragment.getMapAsync(this);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mlocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            checkLocation();
        }

        btnNear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetNearbyPlacesData getNearest = new GetNearbyPlacesData();

                try {
                    JSONObject parentObject = new JSONObject();
                    JSONArray jsonArray = parentObject.getJSONArray("results");
                    mLocation.setLatitude(latLng.latitude);
                    mLocation.setLongitude(latLng.longitude);
                    distances = new ArrayList<>();

                    for(int i = 0;i<jsonArray.length();i++){

                        JSONObject locationObject = jsonArray.getJSONObject(i);
                        String latitude_new  = locationObject.getString("lat");
                        String longitude_new = locationObject.getString("lng");
                        farLocation.setLatitude(Double.parseDouble(latitude_new));
                        farLocation.setLatitude(Double.parseDouble(longitude_new));
                        distance = mLocation.distanceTo(farLocation);
                        distances.add(distance);

                    }
                   String s = String.valueOf(Collections.min(distances)) ;
                    Toast.makeText(MainActivity.this,s, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

         btnShow.setOnClickListener(new View.OnClickListener() {
           @Override
              public void onClick(View v) {

                 findPetrolPumps();

                   /*String petrolPump = "PetrolPump";
                   String url = getUrl(latLng.latitude,latLng.longitude,petrolPump);
                   Object dataTransfer[] = new Object[2];
                   dataTransfer[0] = mMap;
                   dataTransfer[1] = url;
                   GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                   getNearbyPlacesData.execute(dataTransfer);*/

                }
               });
    }

    public static float getMin(float[] inputArray){
        float minValue = inputArray[0];
        for(int i=1;i<inputArray.length;i++){
            if(inputArray[i] < minValue){
                minValue = inputArray[i];
            }
        }
        return minValue;
    }

    /*private String getUrl(double latitude,double longitude,String nearbyPlace){

        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + latitude + "," + longitude);
        stringBuilder.append("&radius=" + PROXIMITY_RADIUS);
        stringBuilder.append("&type=" + nearbyPlace);
        stringBuilder.append("&keyword="+nearbyPlace);
        stringBuilder.append("&key=AIzaSyChE2NwROMNfL058B4rGgB3hXOUpHvInds");

        String url = stringBuilder.toString();
        return url;
    }*/
    private boolean checkLocation() {

        if (!isLocationEnabled()) {
            showAlert();
        }
        return isLocationEnabled();

    }

    private void showAlert() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Please Enable Location")
                .setPositiveButton("LocationSettings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        dialog.show();

    }

    private boolean isLocationEnabled() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean requestSinglePermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        isPermission = true;
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            isPermission = false;
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
        return isPermission;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation == null) {
            startLocationUpdates();
        } /*else {
            Toast.makeText(this, "Location not detected", Toast.LENGTH_SHORT).show();
        }*/

    }

    private void startLocationUpdates() {

        mlocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_intervall);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mlocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (currentLocationMarker != null) {
            moveVechile(currentLocationMarker, location);
            /*rotateMarker(currentLocationMarker,location.getBearing(),start_rotation);*/

        } else {
            /*String msg = "UpdatedLocation :" + Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude());
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();*/

            latLng = new LatLng(location.getLatitude(), location.getLongitude());


            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
            mapFragment.getMapAsync(this);

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (latLng != null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("I am here");
            currentLocationMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void moveVechile(final Marker myMarker, final Location finalPosition) {
         final LatLng startPosition = myMarker.getPosition();

         final Handler handler = new Handler();
         final long start = SystemClock.uptimeMillis();
         final Interpolator interpolator = new AccelerateDecelerateInterpolator();
         final float durationInMs = 2000;
         final boolean hideMarker = false;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude * (1 - t) + (finalPosition.getLatitude()) * t,
                        startPosition.longitude * (1 - t) + (finalPosition.getLongitude()) * t);
                myMarker.setPosition(currentPosition);
                // myMarker.setRotation(finalPosition.getBearing());


                // Repeat till progress is completeelse
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                    // handler.postDelayed(this, 100);
                } else {
                    if (hideMarker) {
                        myMarker.setVisible(false);
                    } else {
                        myMarker.setVisible(true);
                    }
                }
            }
        });
    }

    /*public void rotateMarker(final Marker marker, final float toRotation, final float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = st;
        final long duration = 1555;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;


                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                start_rotation = -rot > 180 ? rot / 2 : rot;
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }*/

    public void findPetrolPumps(){
            String petrolpump = "PetrolPump";
            StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            stringBuilder.append("location=" + latLng.latitude + "," + latLng.longitude);
            stringBuilder.append("&radius=" + PROXIMITY_RADIUS);
            stringBuilder.append("&type=" + petrolpump);
        stringBuilder.append("&keyword="+petrolpump);
            stringBuilder.append("&key=AIzaSyChE2NwROMNfL058B4rGgB3hXOUpHvInds");

            String url = stringBuilder.toString();

            Object dataTransfer[] = new Object[2];
            dataTransfer[0] = mMap;
            dataTransfer[1] = url;


            GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
            getNearbyPlacesData.execute(dataTransfer);

    }
}
