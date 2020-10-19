package com.example.charityshareapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

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

public class AdminNotifications extends AppCompatActivity implements View.OnClickListener {

    ProgressDialog progressDialog;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.toolbar_title) TextView toolbar_title;
    @BindView(R.id.btn_send_notification) Button btn_send_notification;

    @BindView(R.id.input_title) EditText input_title;
    @BindView(R.id.input_message) EditText input_message;
    @BindView(R.id.input_title_container) TextInputLayout input_title_container;
    @BindView(R.id.input_message_container) TextInputLayout input_message_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        btn_send_notification.setOnClickListener(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == btn_send_notification.getId()) {
            boolean valid = true;

            String title = input_title.getText().toString();
            String message = input_message.getText().toString();

            if (title.isEmpty()) {
                input_title_container.setError("Title Required");
                valid = false;
            } else {
                input_title_container.setError(null);
            }

            if (message.isEmpty()) {
                input_message_container.setError("Message Required");
                valid = false;
            } else {
                input_message_container.setError(null);
            }

            if(valid) upload();
        }
    }

    public void upload() {
        progressDialog = new ProgressDialog(this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Sending Notification");
        progressDialog.show();

        String title = input_title.getText().toString();
        String message = input_message.getText().toString();

        RetrofitRequest.
        createService(STORE.API_Client.class).
        notification_send(title, message).
        enqueue(new Callback<STORE.ResponseModel>() {
            @Override
            public void onResponse(Call<STORE.ResponseModel> call, Response<STORE.ResponseModel> response) {
                if (response.isSuccessful()) {
                    progressDialog.dismiss();
                    Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                    if(response.body().getStatus() == -1 || response.body().getStatus() == 0) {
                        Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                    } else if(response.body().getStatus() == 1) {
                        Toast.makeText(AdminNotifications.this, "Notification Sent", Toast.LENGTH_SHORT).show();
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
        finish();
        overridePendingTransition(R.anim.enter_right, R.anim.exit_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}