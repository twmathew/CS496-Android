package com.example.tom.sqlandlocation;


import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import static com.google.android.gms.common.GooglePlayServicesUtil.getErrorDialog;

//Declare the class "GPSActivity". Extends AppCompat. uses Google API callbacks and failed listener.
//So we need to handle all the callbacks and such in this class.
public class GPSActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //Text things to hold Latitude and Longitude, and some things to test
    private TextView myLatit;
    private TextView myLongit;
    private TextView mouthy;
    private TextView mouthy2;
    //Instance we will use to request from server.
    private GoogleApiClient myGMSInstance;
    private LocationRequest myLocRequest;
    private Location mLastLocation;
    private LocationListener mLocationListener;
    private static final int LOCATION_PERMISSON_RESULT = 17;


    //On Creation. Call super version of constructor. Use activity_gps layout.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        //If instance doesn't exists, create, including adding the Location services API.

    /*
    //The app isnt asking for permission every time. MAybe this will work
       if (myGMSInstance != null) {
           myGMSInstance.disconnect();
       }

    //Then re-start the instance
        myGMSInstance = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
 //   }

*/

        //say where we are putting them
        myLatit = (TextView) findViewById(R.id.lat_output);
        myLongit = (TextView) findViewById(R.id.lon_output);
        mouthy = (TextView) findViewById(R.id.noisy);
        mouthy2 = (TextView) findViewById(R.id.noisy2);

        //set text for testing/debugging- we got this far, activity was created
        myLatit.setText("Activity Created");

        //Location request, create and set properties of it.
        myLocRequest = LocationRequest.create();
        myLocRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        myLocRequest.setInterval(10000);
        myLocRequest.setFastestInterval(10000);

        //Listener, called when the location gets updated. Updates myLatit and myLongit.
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    myLatit.setText(String.valueOf(location.getLongitude()));
                    myLongit.setText(String.valueOf(location.getLatitude()));
                } else {
                    //to show that it didnt work
                    myLongit.setText("No Location Avaliable");
                }
            }
        };
    }

    //When we connect, this will be called.
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Just some text to show what's happening
        myLatit.setText("onConnect");
        //Check if we have either Fine Location permission, and/or coarse location permission
        //      if (ActivityCompat.checkSelfPermission(this,
        //             android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        //            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


//Set the permissions to denied, force us to ask the user every time for permission
        //   ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION = PackageManager.PERMISSION_DENIED;
        //   ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) = PackageManager.PERMISSION_DENIED;

        //If we didn't have either permission, request.
        //Update: commented out the IF statement to force location request every time.
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSON_RESULT);
        //Again, some text to show what's going on. We return, nothing happens.
        myLongit.setText("reqd permis");
        //      return;
        //      }


        //If we had permission for at least one of them, we go ahead and proceed to update Location.

        //copy-paste the IF stuff from above, to see if we got perm or not. Change to OR so we check correctly
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            myLongit.setText("got permission(s)");
        }
        mouthy.setText("here we are");
        updateLocation();
    }

    //This function updates the location. Gets called by some other functions as needed.
    private void updateLocation() {

        mouthy2.setText("updLoc again");

        //If we don't have permission for either coarse or fine, do nothing (return).
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            mouthy2.setText("updLoc noperm");
            return;
        }

        //Otherwise, get the last location (piggyback off a previous app), and set Lat and Long.
        mouthy2.setText("updLoc lastloc");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(myGMSInstance);

        if(mLastLocation != null){
            mouthy2.setText("lastloc was null");
            myLatit.setText(String.valueOf(mLastLocation.getLongitude()));
            myLongit.setText(String.valueOf(mLastLocation.getLatitude()));
        }
        //If there is no last location (common in an emulator), need to use Location Services package.
        //call the function to update location, passing the instance, request, and listener.
        else {
            mouthy2.setText("updLoc finale");
            LocationServices.FusedLocationApi.requestLocationUpdates(myGMSInstance,myLocRequest,mLocationListener);
        }

    }


    /*
    What does this do...

        @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == LOCATION_PERMISSON_RESULT){
            if(grantResults.length > 0){
                updateLocation();
            }
        }
    }
     */

    //handles connection failure
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Dialog errDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0);
        errDialog.show();
        return;
    }


    //Onstart
    @Override
    protected void onStart() {
        //connect intance, some debug text, call parent onStart.
        myGMSInstance.connect();
        myLatit.setText("onStart called, we connected");
        super.onStart();
    }

    //OnStop
    @Override
    protected void onStop() {
        //DC from instance and call parent onStop
        myGMSInstance.disconnect();
        super.onStop();
    }

    //Need to have onSuspended, just do nothing.
    @Override
    public void onConnectionSuspended(int i) {

    }


//boolean permissionCheck = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == DefineStuff.PERMISSION_GRANTED));



}
