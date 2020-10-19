package com.example.charityshareapp;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdminHome extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.btn_users) CardView btn_users;
    @BindView(R.id.btn_posts) CardView btn_posts;
    @BindView(R.id.btn_notification) CardView btn_notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        ButterKnife.bind(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        btn_users.setOnClickListener(this);
        btn_posts.setOnClickListener(this);
        btn_notification.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == btn_users.getId())
            startActivity(new Intent(getApplicationContext(), AdminAccounts.class));
        else if(view.getId() == btn_posts.getId())
            startActivity(new Intent(getApplicationContext(), BrowsePosts.class));
        else if(view.getId() == btn_notification.getId())
            startActivity(new Intent(getApplicationContext(), AdminNotifications.class));

        overridePendingTransition(R.anim.enter_left, R.anim.exit_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            SharedPreferences.Editor editor = getSharedPreferences(STORE.SP, MODE_PRIVATE).edit();
            editor.putString("exists", "NO");
            editor.apply();

            startActivity(new Intent(this, Login.class));
            finish();
            overridePendingTransition(R.anim.enter_right, R.anim.exit_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
