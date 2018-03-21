package com.snatik.storage.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

/**
 * Created by root on 3/20/18.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }
    private void initView() {
        android.support.v7.widget.Toolbar mtoolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);


        findViewById(R.id.btn_bootanimation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BootanimationActivity.class);
                intent.putExtra("title","Boot Animation");
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_bootlogo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BootlogoActivity.class);
                intent.putExtra("title","Boot Logo");
                startActivity(intent);
            }
        });

    }
}
