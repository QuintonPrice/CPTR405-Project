package edu.wallawalla.cs.pricqu.travelapp;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.RadioButton;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View mTravelRadio = findViewById(R.id.travel_radio);

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
}