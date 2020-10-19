package com.example.charityshareapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.Calendar;
import java.util.UUID;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePost extends AppCompatActivity implements View.OnClickListener {

    Uri uri;
    String imageFileName = "";
    boolean imageFlag = false;

    ProgressDialog progressDialog;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.toolbar_title) TextView toolbar_title;
    @BindView(R.id.btn_add_picture) Button btn_add_picture;
    @BindView(R.id.btn_add_post) Button btn_add_post;

    @BindView(R.id.input_title) EditText input_title;
    @BindView(R.id.input_description) EditText input_description;
    @BindView(R.id.input_title_container) TextInputLayout input_title_container;
    @BindView(R.id.input_description_container) TextInputLayout input_description_container;

    @BindView(R.id.img_post) ImageView img_post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        btn_add_post.setOnClickListener(this);
        btn_add_picture.setOnClickListener(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == btn_add_post.getId()) {
            validate();
        } else if(view.getId() == btn_add_picture.getId()) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 200);
            }
            else {
                openImageIntent();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openImageIntent();
            else
                Toast.makeText(this, "Storage Permission Required", Toast.LENGTH_SHORT).show();
        }
    }

    public void openImageIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            try {
                img_post.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
                uri = data.getData();
                imageFlag = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void validate() {
        boolean valid = true;

        String title = input_title.getText().toString();
        String description = input_description.getText().toString();

        if (title.isEmpty()) {
            input_title_container.setError("Title can't be empty");
            valid = false;
        } else {
            input_title_container.setError(null);
        }

        if (description.isEmpty()) {
            input_description_container.setError("Description number can't be empty");
            valid = false;
        } else {
            input_description_container.setError(null);
        }

        if(valid) {
            if(imageFlag) upload();
            else Toast.makeText(this, "Post Image Required", Toast.LENGTH_SHORT).show();
        }
    }

    public void upload() {
        progressDialog = new ProgressDialog(this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Creating Post");
        progressDialog.show();

        imageFileName = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase() + "_" + Calendar.getInstance().getTimeInMillis();

        RequestBody title = RequestBody.create(MediaType.parse("text/plain"), input_title.getText().toString());
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), input_description.getText().toString());
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), imageFileName);
        RequestBody poster = RequestBody.create(MediaType.parse("text/plain"), this.getSharedPreferences(STORE.SP, MODE_PRIVATE).getString("email", ""));

        File file = new File(URIPathLib.getPath(this, uri));
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part image = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

        RetrofitRequest.
        createService(STORE.API_Client.class).
        post_add(poster, title, description, filename, image).
        enqueue(new Callback<STORE.ResponseModel>() {
            @Override
            public void onResponse(Call<STORE.ResponseModel> call, Response<STORE.ResponseModel> response) {
                if (response.isSuccessful()) {
                    Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                    if(response.body().getStatus() == -1 || response.body().getStatus() == 0) {
                        progressDialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                    } else if(response.body().getStatus() == 1) {
                        Toast.makeText(CreatePost.this, "Post Created", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), BrowsePosts.class));
                        finish();
                        overridePendingTransition(R.anim.enter_right, R.anim.exit_right);
                    }
                } else {
                    Log.i("#####_failure", "" + response.code());
                    Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<STORE.ResponseModel> call, Throwable t) {
                progressDialog.dismiss();
                Snackbar.make(findViewById(android.R.id.content), "Cannot Connect to Server", Snackbar.LENGTH_LONG).show();
                Log.i("#####_HARD_FAIL", "StackTrace:");
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, BrowsePosts.class));
        finish();
        overridePendingTransition(R.anim.enter_right, R.anim.exit_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}