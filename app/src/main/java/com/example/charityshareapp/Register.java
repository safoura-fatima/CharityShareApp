package com.example.charityshareapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.contentContainer) ScrollView contentContainer;
    @BindView(R.id.btn_register) Button btn_register;
    @BindView(R.id.link_login) TextView link_login;

    @BindView(R.id.input_name) EditText input_name;
    @BindView(R.id.input_email) EditText input_email;
    @BindView(R.id.input_password) EditText input_password;
    @BindView(R.id.input_repassword) EditText input_repassword;
    @BindView(R.id.input_mobile) EditText input_mobile;

    @BindView(R.id.input_name_container) TextInputLayout input_name_container;
    @BindView(R.id.input_email_container) TextInputLayout input_email_container;
    @BindView(R.id.input_password_container) TextInputLayout input_password_container;
    @BindView(R.id.input_repassword_container) TextInputLayout input_repassword_container;
    @BindView(R.id.input_mobile_container) TextInputLayout input_mobile_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ButterKnife.bind(this);
        btn_register.setOnClickListener(this);
        link_login.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == link_login.getId()) {
            onBackPressed();
        } else if (view.getId() == btn_register.getId())
            validate();
    }

    public void validate() {
        boolean valid = true;

        String email = input_email.getText().toString().toLowerCase();
        String password = input_password.getText().toString();
        String reEnterPassword = input_repassword.getText().toString();
        String name = input_name.getText().toString();
        String mobile = input_mobile.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email_container.setError("Enter a valid email address");
            focusOnView(contentContainer, input_email);
            valid = false;
        } else {
            input_email_container.setError(null);
        }

        if (password.isEmpty()) {
            input_password_container.setError("Password can't be empty");
            focusOnView(contentContainer, input_password);
            valid = false;
        } else {
            input_password_container.setError(null);
        }

        if (!(reEnterPassword.equals(password))) {
            input_repassword_container.setError("Passwords do not match");
            valid = false;
        } else {
            input_repassword_container.setError(null);
        }

        if (name.isEmpty()) {
            input_name_container.setError("Name can't be empty");
            valid = false;
        } else {
            input_name_container.setError(null);
        }

        if (mobile.isEmpty()) {
            input_mobile_container.setError("Mobile number can't be empty");
            valid = false;
        } else {
            input_mobile_container.setError(null);
        }

        if(valid) register();
    }

    public void register() {
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Creating Account");
        progressDialog.show();

        final String name = input_name.getText().toString();
        final String email = input_email.getText().toString().toLowerCase();
        final String password = input_password.getText().toString();
        final String mobile = input_mobile.getText().toString();
        final String type = "user";

        RetrofitRequest.
        createService(STORE.API_Client.class).
        register(email, password, name, mobile, type).
        enqueue(new Callback<STORE.ResponseModel>() {
            @Override
            public void onResponse(Call<STORE.ResponseModel> call, Response<STORE.ResponseModel> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                    if(response.body().getStatus() == -2) {
                        input_email_container.setError("This email address is already registered");
                        focusOnView(contentContainer, input_email);
                    } else if(response.body().getStatus() == -1 || response.body().getStatus() == 0) {
                        Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                    } else if(response.body().getStatus() == 1) {
                        logInUser(email);
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

    public void logInUser(String email) {
        SharedPreferences.Editor editor = getSharedPreferences(STORE.SP, MODE_PRIVATE).edit();
        editor.putString("exists", "YES");
        editor.putString("email", email);
        editor.apply();

        startActivity(new Intent(getApplicationContext(), BrowsePosts.class));
        finish();
        overridePendingTransition(R.anim.enter_left, R.anim.exit_left);
    }

    private void focusOnView(ScrollView scroll, EditText et) {
        scroll.smoothScrollTo(((et.getLeft() + et.getRight() - scroll.getWidth()) / 2), 0);
        et.requestFocus();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, Login.class));
        finish();
        overridePendingTransition(R.anim.enter_right, R.anim.exit_right);
    }
}
