package com.example.tom.sqlandlocation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView itemTres = (TextView) findViewById(R.id.Layout3);
        itemTres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intentTres = new Intent(MainActivity.this, ActivityCombo.class);
                startActivity(intentTres);
            }

        });


    }



}