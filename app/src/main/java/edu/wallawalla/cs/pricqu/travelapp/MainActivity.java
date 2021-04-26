package edu.wallawalla.cs.pricqu.travelapp;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.RadioButton;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View mTravelRadio = findViewById(R.id.travel_radio);
        final Button findDestinationsButton = findViewById(R.id.find_destinations);
        findDestinationsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText numberInputEditText=findViewById(R.id.how_many_travelers);
                String temp = numberInputEditText.getText().toString();
                int value = 0;
                if (!"".equals(temp)) {
                    value=Integer.parseInt(temp);
                }
                if (value == 0) {
                    Toast.makeText(getApplicationContext(), "Cannot have 0 travelers!", Toast.LENGTH_SHORT).show();
                }
                else if (value > 0) {
                    Toast.makeText(getApplicationContext(), "Valid number of travelers!", Toast.LENGTH_SHORT).show();
                }
            }
        });



/*        public void onRadioButtonClicked(android.view.View view) {
            // Which radio button was selected?
            switch (view.getId()) {
                case R.id.radio_yes:
                    findViewById(R.id.radio_output.setText("Button1 has been chosen");
                    // "Local" selected
                    break;
                case R.id.radio_no:
                    // "International" selected
                    break;
            }
        }*/

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
            // About selected
            Toast.makeText(getApplicationContext(),"List of destinations will be added later!",Toast.LENGTH_SHORT).show();
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


}