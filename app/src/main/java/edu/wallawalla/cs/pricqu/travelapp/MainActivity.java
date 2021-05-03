package edu.wallawalla.cs.pricqu.travelapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View mTravelRadio = findViewById(R.id.travel_radio);

        // find destinations button and functions
        final Button findDestinationsButton = findViewById(R.id.find_destinations);
        findDestinationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // TODO: Add functionality to find destinations
                FragmentTransaction fragment = getSupportFragmentManager().beginTransaction();
                fragment.replace(R.id.destinationFragmentPlaceholder, new DestinationsFragment());
                fragment.commit();
            }
        });
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
            Toast.makeText(getApplicationContext(),"Help menu will be added later!",Toast.LENGTH_LONG).show();
            return true;
        }
        else if (item.getItemId() == R.id.action_destinations_list) {
            // Destination list selected
            Intent intent = new Intent(MainActivity.this,Activity2.class);
            startActivity(intent);
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