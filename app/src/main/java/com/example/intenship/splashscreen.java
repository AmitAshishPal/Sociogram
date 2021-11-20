package com.example.intenship;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                boolean hasLoggedIn = sharedPreferences.getBoolean("hasloggedIn", false);

                if(hasLoggedIn){
                    Intent intent = new Intent(splashscreen.this, DashActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(splashscreen.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 5000);

    }
}