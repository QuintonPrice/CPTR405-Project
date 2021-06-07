package edu.wallawalla.cs.pricqu.travelapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class DestinationLookupActivity extends AppCompatActivity {

    double currLatitude = 46.0493, currLongitude = -118.3883; // initializes current location to college place
    long locationTime;
    private static final String TAG = "DestinationActivity";
    FusedLocationProviderClient fusedLocationProviderClient;
    int LOCATION_REQUEST_CODE = 10001;
    boolean useKilometers = true;
    SharedPreferences sharedPreferences;
    EditText mDestination;
    String destinationString;
    private RequestQueue mQueue;
    TextView mTextViewTitle;
    TextView mTextViewContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_lookup);

        // location variables
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // API variables
        mQueue = Volley.newRequestQueue(this);
        mTextViewTitle = findViewById(R.id.destinationTextViewTitle);
        mTextViewContent = findViewById(R.id.destinationTextViewContent);

        // preferences variable
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // kilometers preference
        useKilometers = sharedPreferences.getBoolean("use_kilometers", true);

        // view results button
        final Button viewResultsButton = findViewById(R.id.viewResults);
        viewResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // TODO: Add functionality to find destinations

                mDestination = findViewById(R.id.locationName);
                destinationString = mDestination.getText().toString();
                jsonParse(destinationString);
            }
        });

        // find distance between locations
        final Button findDistanceButton = findViewById(R.id.findDistance);
        findDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDestination = findViewById(R.id.locationName);
                destinationString = mDestination.getText().toString();
                findDistanceDialogue(destinationString, useKilometers);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            askLocationPermission();
        }
    }

    private void jsonParse(String city) {
        city = city.toLowerCase();
        String url = "https://api.opentripmap.com/0.1/en/places/geoname?name=" + city + "&apikey=5ae2e3f221c38a28845f05b652a7f39e0918e84292a5fbe0e9042c2e";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // resets textView title
                            mTextViewContent.setText("");
                            mTextViewTitle.setText("");

                            String locationName = response.getString("name");
                            String locationCountry = response.getString("country");
                            String locationLat = response.getString("lat").toString();
                            String locationLon = response.getString("lon").toString();
                            String locationPop = response.getString("population").toString();

                            // sets title
                            mTextViewTitle.setText(locationName);

                            // sets content
                            mTextViewContent.append("Country: " +locationCountry + "\n");
                            mTextViewContent.append("Population: " + locationPop + "\n");
                            mTextViewContent.append("Latitude: " + locationLat + "\n");
                            mTextViewContent.append("Longitude: " + locationLon + "\n");

                            //Log.e(TAG, "onResponse: " + locationName);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }

    // gets last location
    private void getLastLocation() {
        @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // set currLatitude and currLongitude
                    currLatitude = location.getLatitude();
                    currLongitude = location.getLongitude();
                    locationTime = location.getTime();
                    Log.i(TAG, "currLatitude: " + currLatitude + "currLongitude: " + currLongitude);
                } else {
                    Log.d(TAG, "onSuccess: Location was null...");
                }
            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e.getLocalizedMessage());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getLastLocation();
            } else {
                // Permission not granted
            }
        }
    }


    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,  Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "askLotcationPermission: should show alert dialogue");
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }
        }
    }

    // calculates the distance from current location to given destinationString location
    public float findDistance(String destinationString, boolean useKilometers) {

        double destLatitude = 47.6062, destLongitude = -122.3321; // initializes longitude and latitude to Seattle
        Geocoder geoCoder = new Geocoder(this);

        Location currLocation = new Location("currLocation");
        Location destLocation = new Location("destLocation");

        // Find the distance between location and destination
        try {
            List<Address> addresses = geoCoder.getFromLocationName(destinationString, 1);
            if (addresses.size() > 0) {
                destLatitude = addresses.get(0).getLatitude();
                destLongitude = addresses.get(0).getLongitude();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // sets college place coordinates
        currLocation.setLatitude(currLatitude);
        currLocation.setLongitude(currLongitude);

        // sets destination coordinates
        destLocation.setLatitude(destLatitude);
        destLocation.setLongitude(destLongitude);

        if (useKilometers) {
            return currLocation.distanceTo(destLocation) / 1000;
        }
        else {
            return (float) ((currLocation.distanceTo(destLocation) / 1000) * 0.62137);
        }
    }

    // runs distance calculations on thread and displays a dialogue
    public void findDistanceDialogue(String destinationString, boolean useKilometers) {

        // Create a background thread
        Thread thread = new Thread(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {

                float distanceFloat = findDistance(destinationString, useKilometers);
                DecimalFormat formattedDistance = new DecimalFormat("###.##");
                String distanceString = formattedDistance.format(distanceFloat); // updates global value
                String units = "";

                if (useKilometers) {
                    units = "kilometers";
                }
                else {
                    units = "miles";
                }

                String finalUnits = units;
                DestinationLookupActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // creates dialogue for distance
                        AlertDialog.Builder builder = new AlertDialog.Builder(DestinationLookupActivity.this);
                        builder.setMessage(destinationString + " is " + distanceString + " " + finalUnits + " from you.");
                        builder.setTitle("Distance");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Close", null);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
            }
        });
        thread.start();
    }

    // for saving state values
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final EditText textBox = (EditText) findViewById(R.id.locationName);
        CharSequence userText = textBox.getText();
        outState.putCharSequence("savedText", userText);
    }
}