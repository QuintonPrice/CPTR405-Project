package edu.wallawalla.cs.pricqu.travelapp;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.EditText;
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // insert items that need to be saved. currently don't have any
        super.onSaveInstanceState(outState);
        final EditText textBox = (EditText) findViewById(R.id.how_many_travelers);
        CharSequence userText = textBox.getText();
        outState.putCharSequence("savedText", userText);

    }

    @Override
    public void onRestoreInstanceState(Bundle savedState) {
        final EditText textBox = (EditText) findViewById(R.id.how_many_travelers);
        CharSequence userText = savedState.getCharSequence("savedText");
        textBox.setText(userText);
    }
}