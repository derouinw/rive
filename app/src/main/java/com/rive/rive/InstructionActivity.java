package com.rive.rive;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;


public class InstructionActivity extends ActionBarActivity {
    String name, destination, origin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Intent incoming = getIntent();
        name = incoming.getStringExtra(MainActivity.NAME_EXTRA);
        destination = incoming.getStringExtra(MainActivity.DEST_EXTRA);
        origin = incoming.getStringExtra(MainActivity.ORIGIN_EXTRA);
    }

    public void continueAction(View view) {
        Intent intent = new Intent(this, NavigateActivity.class);

        intent.putExtra(MainActivity.NAME_EXTRA, name);
        intent.putExtra(MainActivity.ORIGIN_EXTRA, origin);
        intent.putExtra(MainActivity.DEST_EXTRA, destination);

        startActivity(intent);
    }

    public void uberAction(View view) {
        Intent intent = new Intent(this, UberAuthenticateActivity.class);

        startActivity(intent);
    }
}
