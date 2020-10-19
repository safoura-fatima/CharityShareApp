package com.example.charityshareapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.charityshareapp.STORE.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetails extends AppCompatActivity {

    Post p;
    ArrayList<Comment> comments;
    Menu options_menu;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.postImage) ImageView postImage;
    @BindView(R.id.postTitle) TextView postTitle;
    @BindView(R.id.posterName) TextView posterName;
    @BindView(R.id.postDate) TextView postDate;
    @BindView(R.id.postDescription) TextView postDescription;

    @BindView(R.id.commentCount) TextView commentCount;
    @BindView(R.id.commentContainer) LinearLayout commentContainer;
    @BindView(R.id.loader) ProgressBar loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Uri data = getIntent().getData();

        if(data == null) {
            p = (Post) getIntent().getSerializableExtra("post");
            setup();
        } else {
            final String post_id = data.toString().split("post/")[1];

            RetrofitRequest.
            createService(STORE.API_Client.class).
            post_list().
            enqueue(new Callback<STORE.RM_Posts>() {
                @Override
                public void onResponse(Call<STORE.RM_Posts> call, Response<STORE.RM_Posts> response) {
                    if (response.isSuccessful()) {
                        Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                        if (response.body().getStatus() == 1) {
                            if(response.body().getPosts().size() > 0) {
                                for (Post pst: response.body().getPosts()) {
                                    if(pst.id.equals(post_id)) {
                                        p = pst;
                                        setup();
                                        return;
                                    }
                                }
                            }

                            if(p == null) { // id unmatched
                                Snackbar.make(findViewById(android.R.id.content), "Post Not Found", Snackbar.LENGTH_INDEFINITE).show();
                                options_menu.findItem(R.id.action_add_comment).setVisible(false);
                                options_menu.findItem(R.id.action_delete_post).setVisible(false);
                                options_menu.findItem(R.id.action_share_post).setVisible(false);
                                loader.setVisibility(View.GONE);
                            }
                        }

                    } else {
                        Log.i("#####_failure", "" + response.code());
                        Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<STORE.RM_Posts> call, Throwable t) {
                    Snackbar.make(findViewById(android.R.id.content), "Cannot Connect to Server", Snackbar.LENGTH_LONG).show();
                    Log.i("#####_HARD_FAIL", "StackTrace:");
                    t.printStackTrace();
                }
            });
        }
    }

    public void setup() {
        postTitle.setText(p.title);
        posterName.setText(p.poster_name);
        postDescription.setText(p.description);

        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = df.parse(p.created_at);
            df.setTimeZone(TimeZone.getDefault());

            String fomatted_date = android.text.format.DateFormat.format("hh:mm a, dd-MMM-yyyy", date).toString();
            postDate.setText(fomatted_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if(p.poster_name.equals("admin")) posterName.setTextColor(Color.RED);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.placeholder);

        Glide.
        with(this).
        setDefaultRequestOptions(requestOptions).
        load(STORE.BASE_URL_IMG + p.filename).
        into(postImage);


        // load comments
        RetrofitRequest.
        createService(STORE.API_Client.class).
        comment_list(p.id).
        enqueue(new Callback<RM_Comments>() {
            @Override
            public void onResponse(Call<RM_Comments> call, Response<RM_Comments> response) {
                loader.setVisibility(View.GONE);
                commentCount.setVisibility(View.VISIBLE);

                if (response.isSuccessful()) {
                    Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                    if (response.body().getStatus() == 1) {
                        if(response.body().getComments().size() == 0) {
                            commentCount.setText("No Comments");
                        } else {
                            comments = new ArrayList<>();
                            comments.addAll(response.body().getComments());
                            commentCount.setText(comments.size() + " Comments");

                            for (Comment c: comments) {
                                View card = ((LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_comment, null);

                                ((TextView)card.findViewById(R.id.commentContent)).setText(c.content);
                                ((TextView)card.findViewById(R.id.posterName)).setText(c.poster_name);

                                if(c.poster_name.equals("admin"))
                                    ((TextView)card.findViewById(R.id.posterName)).setTextColor(Color.RED);

                                try {
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    Date date = df.parse(c.created_at);
                                    df.setTimeZone(TimeZone.getDefault());

                                    String fomatted_date = android.text.format.DateFormat.format("hh:mm a, dd-MMM-yyyy", date).toString();
                                    ((TextView)card.findViewById(R.id.commentDate)).setText(fomatted_date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                commentContainer.addView(card);
                            }
                        }
                    }

                } else {
                    Log.i("#####_failure", "" + response.code());
                    Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RM_Comments> call, Throwable t) {
                loader.setVisibility(View.GONE);
                Snackbar.make(findViewById(android.R.id.content), "Cannot Connect to Server", Snackbar.LENGTH_LONG).show();
                Log.i("#####_HARD_FAIL", "StackTrace:");
                t.printStackTrace();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_comment) {
            final EditText input = new EditText(this);

            new AlertDialog
            .Builder(this)
            .setView(input)
            .setCancelable(false)
            .setTitle("Add Comment")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String post_id = p.id;
                    String poster = PostDetails.this.getSharedPreferences(STORE.SP, MODE_PRIVATE).getString("email", "");
                    String content = input.getText().toString().trim();

                    if(content.equals("")) {
                        Toast.makeText(PostDetails.this, "Empty Input. Nothing Posted.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final ProgressDialog progressDialog;
                    progressDialog = new ProgressDialog(PostDetails.this, R.style.Theme_AppCompat_Light_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Posting Comment");
                    progressDialog.show();

                    RetrofitRequest.
                    createService(API_Client.class).
                    comment_add(post_id, poster, content).
                    enqueue(new Callback<ResponseModel>() {
                        @Override
                        public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                            progressDialog.dismiss();

                            if (response.isSuccessful()) {
                                Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                                if(response.body().getStatus() == -1 || response.body().getStatus() == 0) {
                                    Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                                } else if(response.body().getStatus() == 1) {
                                    Toast.makeText(PostDetails.this, "Comment Posted", Toast.LENGTH_SHORT).show();

                                    Intent i = new Intent(getApplicationContext(), PostDetails.class);
                                    i.putExtra("post", p);
                                    startActivity(i);
                                    finish();
                                }
                            } else {
                                Log.i("#####_failure", "" + response.code());
                                Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseModel> call, Throwable t) {
                            progressDialog.dismiss();
                            Snackbar.make(findViewById(android.R.id.content), "Cannot Connect to Server", Snackbar.LENGTH_LONG).show();
                            Log.i("#####_HARD_FAIL", "StackTrace:");
                            t.printStackTrace();
                        }
                    });
                }})
            .show();
        }
        else if (item.getItemId() == R.id.action_delete_post) {
            new AlertDialog
            .Builder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setCancelable(false)
            .setNegativeButton("NO", null)
            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    final ProgressDialog progressDialog;
                    progressDialog = new ProgressDialog(PostDetails.this, R.style.Theme_AppCompat_Light_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Deleting");
                    progressDialog.show();

                    RetrofitRequest.
                    createService(STORE.API_Client.class).
                    post_delete(p.id).
                    enqueue(new Callback<ResponseModel>() {
                        @Override
                        public void onResponse(Call<STORE.ResponseModel> call, Response<ResponseModel> response) {
                            progressDialog.dismiss();

                            if (response.isSuccessful()) {
                                Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                                if(response.body().getStatus() == -1 || response.body().getStatus() == 0) {
                                    Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                                } else if(response.body().getStatus() == 1) {
                                    Toast.makeText(PostDetails.this, "Post Deleted", Toast.LENGTH_SHORT).show();

                                    Intent i = new Intent(getApplicationContext(), BrowsePosts.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();
                                    overridePendingTransition(R.anim.enter_right, R.anim.exit_right);
                                }
                            } else {
                                Log.i("#####_failure", "" + response.code());
                                Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseModel> call, Throwable t) {
                            progressDialog.dismiss();
                            Snackbar.make(findViewById(android.R.id.content), "Cannot Connect to Server", Snackbar.LENGTH_LONG).show();
                            Log.i("#####_HARD_FAIL", "StackTrace:");
                            t.printStackTrace();
                        }
                    });
                }})
            .show();
        } else if (item.getItemId() == R.id.action_share_post) {
            String share_text = p.title + "\n\n" + "https://charityapp.example.com/post/" + p.id;
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, share_text);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        options_menu = menu;
        getMenuInflater().inflate(R.menu.post_detail_menu, menu);

        if(this.getSharedPreferences(STORE.SP, MODE_PRIVATE).getString("exists", "").equals("NO")) {
            // hide add comment option if user not logged in
            menu.findItem(R.id.action_add_comment).setVisible(false);
        } else {
            // show delete post option if admin logged in
            if(this.getSharedPreferences(STORE.SP, MODE_PRIVATE).getString("email", "").equals("admin"))
                menu.findItem(R.id.action_delete_post).setVisible(true);
        }

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