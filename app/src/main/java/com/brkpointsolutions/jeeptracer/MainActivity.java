package com.brkpointsolutions.jeeptracer;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final static String TAG = "MainActivity";

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private boolean requestingLocationUpdate;

    private enum UpdatingState {STOPPED, REQUESTING, STARTED}

    private UpdatingState state = UpdatingState.STOPPED;

    private final static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final static int REQCODE_PERMISSIONS = 1111;


    private Location location;

    /*public void onClickCurrentLocation(View view) {
        Log.d(TAG, "onLocationChanged: " + location);
        googleMap.animateCamera(CameraUpdateFactory
                .newLatLng(new LatLng(10.327191, 123.9059663)));
    }
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                //map.moveCamera(CameraUpdateFactory.zoomTo(15f));
                googleMap = map;
                map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        View view = getLayoutInflater().inflate(R.layout.info_window, null);

                        TextView title = (TextView) view.findViewById(R.id.info_title);
                        title.setText(marker.getTitle());

                        TextView info = (TextView) view.findViewById(R.id.info);
                        info.setText(marker.getSnippet());

                        /*TextView time = (TextView)view.findViewById(R.id.info_time);
                        time.setText(marker.getTitle());

                        TextView in_or_off = (TextView)view.findViewById(R.id.info_in_or_off);
                        in_or_off.setText(marker.getTitle());
*/
                        return view;

                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        return null;
                    }
                });

                Intent intent_in = new Intent();
                intent_in.putExtra("name", "Ben");
                intent_in.putExtra("time", "10:00am");
                intent_in.putExtra("state", true);
                intent_in.putExtra("latitude", "10.327191");
                intent_in.putExtra("longitude", "123.9059663");
                intent_in.putExtra("jeep", "TMQ 202");
                intent_in.setPackage("com.brkpointsolutions.jeeptracer");
                String uri_in = intent_in.toUri(Intent.URI_ANDROID_APP_SCHEME);
                Log.d("URI_in", uri_in);

                Intent intent_off = new Intent();
                intent_off.putExtra("name", "Ben");
                intent_off.putExtra("time", "10:30am");
                intent_off.putExtra("state", false);
                intent_off.putExtra("latitude", "10.3163127");
                intent_off.putExtra("longitude", "123.9049761");
                intent_off.putExtra("jeep", "TMQ 202");
                intent_off.setPackage("com.brkpointsolutions.jeeptracer");
                String uri_off = intent_off.toUri(Intent.URI_ANDROID_APP_SCHEME);
                Log.d("URI_off", uri_off);

                /*LatLng ben_get_in = new LatLng(10.327191, 123.9059663);
                LatLng ben_get_off = new LatLng(10.3163127,123.9049761);
                */

                Intent intent = getIntent();
                String latitude = "";
                String longitude = "";
                String time = "";
                String name = "";
                boolean state = false;
                String jeep = "";


                if (intent.getData() != null) {
                    try {
                        intent = Intent.parseUri(intent.getData().toString(), Intent.URI_ANDROID_APP_SCHEME);
                    } catch (URISyntaxException e) {

                    }


                    latitude = intent.getStringExtra("latitude");
                    longitude = intent.getStringExtra("longitude");
                    time = intent.getStringExtra("time");
                    name = intent.getStringExtra("name");
                    state = intent.getBooleanExtra("state", false);
                    jeep = intent.getStringExtra("jeep");

                    LatLng ben = new LatLng(Double.parseDouble(latitude),
                            Double.parseDouble(longitude));

                    //map.setMyLocationEnabled(true);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(ben, 13));


                    MarkerOptions marker = new MarkerOptions()
                            .title(name)
                            .snippet(" Jeep: " + jeep + "\n" +
                                    " Time: " + time)
                            .position(ben);

                    if (!state) marker.icon(BitmapDescriptorFactory.defaultMarker(100));

                    map.addMarker(marker).showInfoWindow();

                }
            }
        });

        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API).build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        googleApiClient.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://com.brkpointsolutions.jeeptracer/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.brkpointsolutions.jeeptracer/android-app/com.brkpointsolutions.jeeptracer/path")
        );
        AppIndex.AppIndexApi.start(googleApiClient, viewAction);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (state != UpdatingState.STARTED && googleApiClient.isConnected())
            startLocationUpdate(true);
        else
            state = UpdatingState.REQUESTING;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        if (state == UpdatingState.STARTED)
            stopLocationUpdate();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        googleApiClient.disconnect();
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://com.brkpointsolutions.jeeptracer/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.brkpointsolutions.jeeptracer/android-app/com.brkpointsolutions.jeeptracer/path")
        );
        AppIndex.AppIndexApi.end(googleApiClient, viewAction);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        if (state == UpdatingState.REQUESTING)
            startLocationUpdate(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspented");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        /*Log.d(TAG, "onLocationChanged: " + location);
        googleMap.animateCamera(CameraUpdateFactory
                .newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));*/
    }

    @Override
    public void onRequestPermissionsResult(int reqCode,
                                           @NonNull String[] permissions, @NonNull int[] grants) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (reqCode) {
            case REQCODE_PERMISSIONS:
                startLocationUpdate(false);
                break;
        }

    }

    private void startLocationUpdate(boolean reqPermission) {
        Log.d(TAG, "startLocationUpdate: " + reqPermission);
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                if (reqPermission)
                    ActivityCompat.requestPermissions(this, PERMISSIONS, REQCODE_PERMISSIONS);
                else
                    Toast.makeText(this, getString(R.string.toast_requires_permission, permission),
                            Toast.LENGTH_SHORT).show();
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        state = UpdatingState.STARTED;
    }

    private void stopLocationUpdate() {
        Log.d(TAG, "stopLocationUpdate");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        state = UpdatingState.STOPPED;
    }
}
