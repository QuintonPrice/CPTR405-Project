package edu.wallawalla.cs.pricqu.travelapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Activity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        final Button loadMasterButton = findViewById(R.id.loadMasterView);
        loadMasterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity2.this,CityListActivity.class);
                startActivity(intent);
                // TODO: Add this functionality to main activity
            }
        });
    }
}