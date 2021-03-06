package edu.wallawalla.cs.pricqu.travelapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.os.Bundle;
import android.view.View;
import android.location.Location;
import android.widget.Spinner;
import android.widget.Switch;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mMediaPlayer;
    double currLatitude = 46.0493, currLongitude = -118.3883; // initializes current location to college place
    long locationTime;
    boolean useKilometers = true;
    int LOCATION_REQUEST_CODE = 10001;
    private static final String TAG = "MainActivity";
    FusedLocationProviderClient fusedLocationProviderClient;
    private RequestQueue mQueue;
    TextView mTextViewResult;
    EditText mRadius;
    String radius;
    String category = "";
    EditText mResults;
    Integer maxResults = 50;
    String units;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // preferences variable
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // kilometers preference
        useKilometers = sharedPreferences.getBoolean("use_kilometers", true);
        if (useKilometers) {
            units = "km";
        } else {
            units = "mi";
        }

        TextView selectRadiusTextView = findViewById(R.id.select_radius_text);
        selectRadiusTextView.setText("");
        selectRadiusTextView.append("Radius From Location (" + units + "):");

        // location variables
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // API variables
        mQueue = Volley.newRequestQueue(this);
        mTextViewResult = findViewById(R.id.APItext);
        mTextViewResult.setMovementMethod(ScrollingMovementMethod.getInstance());

        Spinner categorySpinner = (Spinner) findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // find destinations button and functions
        final Button findResultsButton = findViewById(R.id.find_results_button);
        findResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getLastLocation();
                String latString = String.valueOf(currLatitude);
                String lonString = String.valueOf(currLongitude);

                String tempCategory = categorySpinner.getSelectedItem().toString();
                category = tempCategory.toLowerCase();


                if (tempCategory.equals("Monuments")) {
                    category = "monuments_and_memorials";
                } else if (tempCategory.equals("Urban Environment")) {
                    category = "urban_environment";
                } else if (tempCategory.equals("Accommodations")) {
                    category = "accomodations";
                } else if (tempCategory.equals("All")) {
                    category = "interesting_places"; // default value for the API search
                }

                mRadius = findViewById(R.id.radius_input);
                if (mRadius.getText().toString().equals("")) {
                    mRadius.setText("100");
                }
                radius = mRadius.getText().toString();
                double radiusDouble = Double.parseDouble(radius);

                // convert either miles or kilometers to meters (which is used by API call)
                if (useKilometers) {
                    radiusDouble = radiusDouble * 1000;
                } else {
                    radiusDouble = radiusDouble * 1609.344;
                }

                radiusDouble = radiusDouble * 1000;

                mResults = findViewById(R.id.max_results_input);
                if (mResults.getText().toString().equals("")) {
                    mResults.setText("50");
                }
                maxResults = Integer.parseInt(mResults.getText().toString());

                jsonParse(category, latString, lonString, radiusDouble, maxResults);
                playButtonClick(this);
            }
        });
    }

    private void jsonParse(String categoryString, String lat, String lon, Double radiusDouble, int maxResults) {
        String radiusString = String.valueOf(radiusDouble);

        Log.e(TAG, "Lat: " + lat);
        Log.e(TAG, "Long: " + lon);

        if (maxResults > 50) {
            maxResults = 50;
        }
        String maxResultsString = String.valueOf(maxResults);

        String url = "https://api.opentripmap.com/0.1/en/places/radius?radius=" + radiusString + "&lon=" + lon + "&lat=" + lat +"&kinds=" + categoryString + "&limit=" + maxResultsString + "&apikey=5ae2e3f221c38a28845f05b652a7f39e0918e84292a5fbe0e9042c2e";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mTextViewResult.setText("");
                            Log.e(TAG, "API URL: " + url);

                            JSONArray jsonArray = response.getJSONArray("features");
                            JSONObject test = jsonArray.getJSONObject(1);
                            JSONObject testObject = test.getJSONObject("properties");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject placeObject = jsonArray.getJSONObject(i);
                                JSONObject place = placeObject.getJSONObject("properties");

                                String locationName = place.getString("name");
                                double locationDistance = place.getDouble("dist");
                                //String openStreetMap = place.getString("osm");

                                if (useKilometers) {
                                    locationDistance = locationDistance / 1000;
                                } else {
                                    locationDistance = locationDistance / 1609;
                                }

                                DecimalFormat formattedDistance = new DecimalFormat("#####.#");
                                String distanceString = formattedDistance.format(locationDistance); // updates global value

                                if ((!locationName.equals("")) && locationDistance >= 0) {
                                    mTextViewResult.append("Name: " + locationName + "\n");
                                    mTextViewResult.append("Distance: " + distanceString + " " +units + "\n\n");
                                }

                            }
                            mTextViewResult.append("\n\n");
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

    public void playButtonClick(View.OnClickListener v) {
        mMediaPlayer = MediaPlayer.create(this, R.raw.note_f);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mMediaPlayer.start();
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mMediaPlayer.release();
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
                    Log.d(TAG, "currLatitude: " + currLatitude + "currLongitude: " + currLongitude);
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


    // dialogue for help button
    public void helpDialog() {
        // creates object of AlertDialogue Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // message for the dialogue
        builder.setMessage("To find locations with the desired paramaters, fill out the fields and press 'Find Results!' The resulting list will be a collection of items returned by the API, as well as the distance from your current location");
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

    // displays dialogue when screen is touched. kinda useless
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
            Intent destinationIntent = new Intent(MainActivity.this,CityListActivity.class);
            // TODO: Enable this is if you want to see the master / detail view. I did not find it necessary to implement it w/ any values but it is implemented
            // startActivity(destinationIntent);
            return true;
        }
        else if (item.getItemId() == R.id.action_exit_app) {
            // Exit app selected
            exitApp();
            return true;
        }
        else if (item.getItemId() == R.id.action_google_maps) {
            Intent mapsIntent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(mapsIntent);
        }
        else if (item.getItemId() == R.id.action_search_destinations) {
            Intent searchIntent = new Intent(MainActivity.this, DestinationLookupActivity.class);
            startActivity(searchIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    // for saving state values
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final EditText radiusSave = (EditText) findViewById(R.id.radius_input);
        final EditText maxSave = (EditText) findViewById(R.id.max_results_input);

        CharSequence radiusInput = radiusSave.getText();
        CharSequence maxInput = maxSave.getText();
        outState.putCharSequence("radius", radiusInput);
        outState.putCharSequence("maxInput", maxInput);

    }

    // for getting state values
    @Override
    public void onRestoreInstanceState(Bundle savedState) {
        final EditText radiusSave = (EditText) findViewById(R.id.radius_input);
        final EditText maxSave = (EditText) findViewById(R.id.max_results_input);

        CharSequence radiusInput = savedState.getCharSequence("radius");
        radiusSave.setText(radiusInput);

        CharSequence maxInput = savedState.getCharSequence("maxInput");
        maxSave.setText(maxInput);
    }

    public boolean onTouchEvent(MotionEvent event){
        String action = "";
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                // touchDialog();
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
