package com.example.gloriaproject;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Dialog extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_dialog, menu);
        return true;
    }
}


