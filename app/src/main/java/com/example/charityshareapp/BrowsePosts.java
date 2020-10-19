package com.example.charityshareapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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

public class BrowsePosts extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ArrayList<STORE.Post> posts;
    PostListAdapter adapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.loader) ProgressBar loader;
    @BindView(R.id.nothingFound) TextView nothingFound;
    @BindView(R.id.listView) ListView listView;
    @BindView(R.id.pullToRefresh) SwipeRefreshLayout pullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_posts);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if(this.getSharedPreferences(STORE.SP, MODE_PRIVATE).getString("email", "").equals("admin")) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        listView.setOnItemClickListener(this);

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                get_posts();
                pullToRefresh.setRefreshing(false);
            }
        });

        get_posts();
    }

    public void get_posts() {
        posts = new ArrayList<>();
        adapter = new PostListAdapter(this, R.layout.list_item_post, posts);
        listView.setAdapter(adapter);

        nothingFound.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);

        RetrofitRequest.
        createService(STORE.API_Client.class).
        post_list().
        enqueue(new Callback<STORE.RM_Posts>() {
            @Override
            public void onResponse(Call<STORE.RM_Posts> call, Response<STORE.RM_Posts> response) {
                loader.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                    if (response.body().getStatus() == 1) {
                        if(response.body().getPosts().size() > 0) {
                            posts.addAll(response.body().getPosts());
                            adapter.notifyDataSetChanged();
                            listView.setVisibility(View.VISIBLE);
                        } else {
                            nothingFound.setVisibility(View.VISIBLE);
                        }
                    }

                } else {
                    Log.i("#####_failure", "" + response.code());
                    Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<STORE.RM_Posts> call, Throwable t) {
                loader.setVisibility(View.GONE);
                Snackbar.make(findViewById(android.R.id.content), "Cannot Connect to Server", Snackbar.LENGTH_LONG).show();
                Log.i("#####_HARD_FAIL", "StackTrace:");
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
        Intent i = new Intent(this, PostDetails.class);
        i.putExtra("post", posts.get(position));
        startActivity(i);
        overridePendingTransition(R.anim.enter_left, R.anim.exit_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_create_post) {
            startActivity(new Intent(this, CreatePost.class));
            finish();
            overridePendingTransition(R.anim.enter_left, R.anim.exit_left);
        } else if (item.getItemId() == R.id.action_edit) {
            getSharedPreferences(STORE.SP, MODE_PRIVATE).edit().putString("user_mode_add_edit", "edit").apply();
            startActivity(new Intent(this, AdminAccountsAddEdit.class));
            finish();
            overridePendingTransition(R.anim.enter_left, R.anim.exit_left);
        } else if (item.getItemId() == R.id.action_logout) {
            SharedPreferences.Editor editor = getSharedPreferences(STORE.SP, MODE_PRIVATE).edit();
            editor.putString("exists", "NO");
            editor.apply();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
            overridePendingTransition(R.anim.enter_right, R.anim.exit_right);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_home_menu, menu);

        if(this.getSharedPreferences(STORE.SP, MODE_PRIVATE).getString("email", "").equals("admin")) {
            menu.findItem(R.id.action_create_post).setShowAsAction(2);
            menu.findItem(R.id.action_logout).setVisible(false);
            menu.findItem(R.id.action_edit).setVisible(false);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        finish();

        if(this.getSharedPreferences(STORE.SP, MODE_PRIVATE).getString("email", "").equals("admin"))
            overridePendingTransition(R.anim.enter_right, R.anim.exit_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}