package com.example.filesynchor.Extras;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.filesynchor.R;

public class Splash extends AppCompatActivity {

    String TAG = "abcd";
    TextView tvDeviceInfo;

    private static final String dstFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+"/FileSync";
    //private static final String dstFolder = Environment.getRootDirectory().getAbsolutePath()+"/FileSync";
    private static final String srcFolder = "/storage/191D-0C26/";
    private Button btnSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
        btnSync = findViewById(R.id.btnSync);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });


    }



}
