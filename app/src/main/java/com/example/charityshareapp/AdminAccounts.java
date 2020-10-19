package com.example.charityshareapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.charityshareapp.STORE.User;

public class AdminAccounts extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ArrayList<User> users;
    UserListAdapter adapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.loader) ProgressBar loader;
    @BindView(R.id.nothingFound) TextView nothingFound;
    @BindView(R.id.listView) ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_accounts);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        users = new ArrayList<>();
        adapter = new UserListAdapter(this, R.layout.list_item_user, users);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        RetrofitRequest.
        createService(STORE.API_Client.class).
        user_list().
        enqueue(new Callback<STORE.RM_Users>() {
            @Override
            public void onResponse(Call<STORE.RM_Users> call, Response<STORE.RM_Users> response) {
                loader.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                    if (response.body().getStatus() == 1) {
                        users.addAll(response.body().getUsers());
                        adapter.notifyDataSetChanged();
                        listView.setVisibility(View.VISIBLE);
                    }

                } else {
                    Log.i("#####_failure", "" + response.code());
                    Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<STORE.RM_Users> call, Throwable t) {
                loader.setVisibility(View.GONE);
                Snackbar.make(findViewById(android.R.id.content), "Cannot Connect to Server", Snackbar.LENGTH_LONG).show();
                Log.i("#####_HARD_FAIL", "StackTrace:");
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
        getSharedPreferences(STORE.SP, MODE_PRIVATE).edit().putString("user_mode_add_edit", "edit").apply();
        Intent i = new Intent(this, AdminAccountsAddEdit.class);
        i.putExtra("id", users.get(position).id);
        i.putExtra("type", users.get(position).type);
        i.putExtra("email", users.get(position).email);
        i.putExtra("name", users.get(position).name);
        i.putExtra("mobile", users.get(position).mobile);
        i.putExtra("account_active", users.get(position).account_active);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.enter_left, R.anim.exit_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            getSharedPreferences(STORE.SP, MODE_PRIVATE).edit().putString("user_mode_add_edit", "add").apply();
            startActivity(new Intent(getApplicationContext(), AdminAccountsAddEdit.class));
            finish();
            overridePendingTransition(R.anim.enter_left, R.anim.exit_left);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_user_list_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.enter_right, R.anim.exit_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
