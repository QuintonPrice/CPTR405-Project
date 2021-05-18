package edu.wallawalla.cs.pricqu.travelapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Telephony;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.RadioButton;
import android.view.View;
import android.widget.Toast;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    double currLatitude = 46.0493, currLongitude = -118.3883; // initalizes current location to college place
    long locationTime;

    int LOCATION_REQUEST_CODE = 10001;
    private static final String TAG = "MainActivity";
    private Object LocationManager;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // find destinations button and functions
        // TODO: make fragment take up more of the screen
        final Button findDestinationsButton = findViewById(R.id.find_destinations);
        findDestinationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // TODO: Add functionality to find destinations
                FragmentTransaction fragment = getSupportFragmentManager().beginTransaction();
                fragment.replace(R.id.destinationFragmentPlaceholder, new DestinationsFragment());
                fragment.commit();
            }
        });

        // find distance between locations
        final Button findDistanceButton = findViewById(R.id.destination_distance_button);
        findDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findDistanceBetween();
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

    public void findDistanceBetween() {
        EditText mDestination = findViewById(R.id.distance_destination_input);
        String destinationString = mDestination.getText().toString();
        Geocoder geoCoder = new Geocoder(this);

        Location currLocation = new Location("currLocation");
        Location destLocation = new Location("destLocation");

        // Create a background thread
        Thread thread = new Thread(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {

                double destLatitude = 47.6062, destLongitude = -122.3321; // initializes longitude and latitude to Seattle

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

                // TODO: get current location

                // sets college place coordinates
                currLocation.setLatitude(currLatitude);
                currLocation.setLongitude(currLongitude);

                destLocation.setLatitude(destLatitude);
                destLocation.setLongitude(destLongitude);

                float distanceFloat = currLocation.distanceTo(destLocation) / 1000;
                String distanceString = String.valueOf(distanceFloat);

                // UI should only be updated by main thread
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // creates dialogue for distance
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Distance between location and destination: " + distanceString + " kilometers from you. NOTE: Uses current location for Gogoleplex, unless you spoof location");
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

    // dialogue for help button
    public void helpDialog() {
        // creates object of AlertDialogue Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // message for the dialogue
        builder.setMessage("Help menu will be added later!");
        // alert title
        builder.setTitle("Help Menu");
        // keeps it so that if the user clicks outside of the dialogue box, it will stay up
        builder.setCancelable(false);
        builder.setPositiveButton("Ok!", null);
        // creates the alert dialogue
        AlertDialog alertDialog = builder.create();
        // shows the alert dialogue
        alertDialog.show();
    }



    public void touchDialog() {
        // creates object of AlertDialogue Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // message for the dialogue
        builder.setMessage("This shows that you have put your finger on the screen");
        // alert title
        builder.setTitle("Touch dialog");
        // keeps it so that if the user clicks outside of the dialogue box, it will stay up
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", null);
        // creates the alert dialogue
        AlertDialog alertDialog = builder.create();
        // shows the alert dialogue
        alertDialog.show();
    }

    // creates dialogue for exiting app
    public void exitApp() {
        // creates object of AlertDialogue Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // message for the dialogue
        builder.setMessage("Do you really want to exit the app?");
        // alert title
        builder.setTitle("Exit app?");
        // keeps it so that if the user clicks outside of the dialogue box, it will stay up
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        // negative button
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if user clicks no, then the dialogue box simply cancels
                dialog.cancel();
            }
        });
        // creates the alert dialogue
        AlertDialog alertDialog = builder.create();

        // shows the alert dialogue
        alertDialog.show();
    }

    // creates the menu and inflates items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // for handling the selection of menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.action_help) {
            // Help button selected
            helpDialog();
            return true;
        }
        else if (item.getItemId() == R.id.action_settings) {
            // Settings button selected
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        else if (item.getItemId() == R.id.action_destinations_list) {
            // Destination list selected
            Intent destinationIntent = new Intent(MainActivity.this,Activity2.class);
            startActivity(destinationIntent);
            return true;
        }
        else if (item.getItemId() == R.id.action_exit_app) {
            // Exit app selected
            exitApp();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // for saving state values
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // insert items that need to be saved. currently don't have any
        super.onSaveInstanceState(outState);
        final EditText textBox = (EditText) findViewById(R.id.how_many_travelers);
        CharSequence userText = textBox.getText();
        outState.putCharSequence("savedText", userText);

    }

    // for getting state values
    @Override
    public void onRestoreInstanceState(Bundle savedState) {
        final EditText textBox = (EditText) findViewById(R.id.how_many_travelers);
        CharSequence userText = savedState.getCharSequence("savedText");
        textBox.setText(userText);
    }

    public boolean onTouchEvent(MotionEvent event){
        String action = "";
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                touchDialog();
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                break;
        }

        Log.d(TAG, action + " x = " + event.getX() +
                " y = " + event.getY());
        return true;
    }
}