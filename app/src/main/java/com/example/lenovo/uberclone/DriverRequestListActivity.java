package com.example.lenovo.uberclone;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener {

    private Button btnGetNearbyRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ListView listView;
    private ArrayList<String> nearByDriveRequests;
    private ArrayAdapter adapter;
    private ArrayList<Double> passengersLatutides;
    private ArrayList<Double> passengersLongitudes;
    private ParseGeoPoint pLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        listView = findViewById(R.id.requestListView);
        nearByDriveRequests = new ArrayList<>();
        passengersLatutides = new ArrayList<Double>();
        passengersLongitudes = new ArrayList<Double>();
        adapter = new ArrayAdapter(DriverRequestListActivity.this,
                android.R.layout.simple_list_item_1,nearByDriveRequests);
        listView.setAdapter(adapter);
        btnGetNearbyRequests=findViewById(R.id.btnGetNearbyRequests);


        btnGetNearbyRequests.setOnClickListener(DriverRequestListActivity.this);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);



        if(Build.VERSION.SDK_INT<23 || ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            0,0,locationListener);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
        }

        listView.setOnItemClickListener(DriverRequestListActivity.this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.driver_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.driverLogOutItem){
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if(e==null){
                        finish();
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {


        if(Build.VERSION.SDK_INT <23){

            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestListView(currentDriverLocation);
        } else if(Build.VERSION.SDK_INT >=23){

            if(ContextCompat.checkSelfPermission(DriverRequestListActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(DriverRequestListActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);
            }else {
               // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLocation);
            }
        }

    }

    private void updateRequestListView(Location driverLocation) {

        if(driverLocation !=null){


            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(),
                    driverLocation.getLongitude());
            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");

            requestCarQuery.whereNear("passengerLocation",driverCurrentLocation);
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null) {
                        if(nearByDriveRequests.size()>0){
                            nearByDriveRequests.clear();
                        }
                        if(passengersLatutides.size()>0){
                            passengersLatutides.clear();
                        }
                        if(passengersLongitudes.size()>0){
                            passengersLongitudes.clear();
                        }
                        if (objects.size() > 0) {
                            for (ParseObject nearRequest : objects) {
                                ParseGeoPoint pLocation = (ParseGeoPoint) nearRequest.get("passengerLocation");
                                Double milesDistanceToPassenger = driverCurrentLocation.
                                        distanceInMilesTo(pLocation);
                                float roundedDistanceValue = Math.round(milesDistanceToPassenger * 10) / 10;
                                nearByDriveRequests.add("There are " + roundedDistanceValue + " "
                                        + " miles to " + nearRequest.get("username"));
                              //  Toast.makeText(DriverRequestListActivity.this,nearRequest.getParseGeoPoint("passengerLocation").getLatitude()+"",Toast.LENGTH_SHORT).show();
                                passengersLatutides.add(pLocation.getLatitude());
                                passengersLongitudes.add(pLocation.getLongitude());
                            }
                        } else {
                            Toast.makeText(DriverRequestListActivity.this,
                                    "Sorry,There are no request yet", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1000
                && grantResults.length>0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(DriverRequestListActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
               // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

               Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
               updateRequestListView(currentDriverLocation);
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Toast.makeText(DriverRequestListActivity.this,"Tapted!",Toast.LENGTH_SHORT).show();
    }
}
