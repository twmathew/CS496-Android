package com.example.tom.aussiefinal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

/*
        TextView itemDos = (TextView) findViewById(R.id.PlayerLink);
        itemDos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intentDos = new Intent(MainActivity.this, PlayersActivity.class);
                startActivity(intentDos);
            }

        });
        */

        TextView itemEagles = (TextView) findViewById(R.id.EaglesLink);
        itemEagles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intentTeams = new Intent(MenuActivity.this, EaglesActivity.class);
                startActivity(intentTeams);
            }

        });

        TextView itemBulldogs = (TextView) findViewById(R.id.BulldogsLink);
        itemBulldogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intentDogs = new Intent(MenuActivity.this, BulldogsActivity.class);
                startActivity(intentDogs);
            }

        });

        TextView itemPick = (TextView) findViewById(R.id.WinnersLink);
        itemPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intentWinners = new Intent(MenuActivity.this, PickActivity.class);
                startActivity(intentWinners);
            }

        });

    }
}