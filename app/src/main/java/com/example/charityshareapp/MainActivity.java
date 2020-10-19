package com.example.charityshareapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.app_title) TextView app_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        //get notification data info
        getIntent().getExtras();

        // subscribe to admin notifications
        FirebaseMessaging.getInstance().subscribeToTopic("admin")
        .addOnCompleteListener(task -> {
            if (!task.isSuccessful())
                Toast.makeText(MainActivity.this, "admin notification subscription failed", Toast.LENGTH_SHORT).show();
        });

        // check login
        if(this.getSharedPreferences(STORE.SP, MODE_PRIVATE).getString("exists", "").equals("YES")) {
            if(this.getSharedPreferences(STORE.SP, MODE_PRIVATE).getString("email", ".").equals("admin"))
                startActivity(new Intent(getApplicationContext(), AdminHome.class));
            else
                startActivity(new Intent(getApplicationContext(), BrowsePosts.class));

        } else {
            startActivity(new Intent(this, Login.class));
        }

        finish();
    }
}
