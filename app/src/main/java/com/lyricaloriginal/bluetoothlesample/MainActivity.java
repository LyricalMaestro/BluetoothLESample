package com.lyricaloriginal.bluetoothlesample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.centralButton1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CentralActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.peripheralButton1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PeripheralActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.centralButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CentralActivity2.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.peripheralButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PeripheralActivity2.class);
                startActivity(intent);
            }
        });
    }
}
