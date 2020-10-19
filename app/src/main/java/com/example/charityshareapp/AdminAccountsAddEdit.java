package com.example.charityshareapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAccountsAddEdit extends AppCompatActivity {

    String logged_in_user = "", mode = "";
    ProgressDialog progressDialog;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.toolbar_title) TextView toolbar_title;
    @BindView(R.id.btn_add_account) Button btn_add_account;

    @BindView(R.id.input_name) EditText input_name;
    @BindView(R.id.input_email) EditText input_email;
    @BindView(R.id.input_password) EditText input_password;
    @BindView(R.id.input_mobile) EditText input_mobile;
    @BindView(R.id.input_name_container) TextInputLayout input_name_container;
    @BindView(R.id.input_email_container) TextInputLayout input_email_container;
    @BindView(R.id.input_password_container) TextInputLayout input_password_container;
    @BindView(R.id.input_mobile_container) TextInputLayout input_mobile_container;

    @BindView(R.id.chk_empAccount) CheckBox chk_empAccount;
    @BindView(R.id.chk_banned) CheckBox chk_banned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_add_edit);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        btn_add_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });
        mode = this.getSharedPreferences(STORE.SP, MODE_PRIVATE).getString("user_mode_add_edit", "");
        logged_in_user = this.getSharedPreferences(STORE.SP, MODE_PRIVATE).getString("email", "");

        if(mode.equals("edit")) {
            toolbar_title.setText("Edit Profile");
            btn_add_account.setText("Update");
            input_password_container.setHint("New Password");
            input_email.setEnabled(false);

            if(logged_in_user.equals("admin")) {
                input_email.setText(getIntent().getStringExtra("email"));
                input_name.setText(getIntent().getStringExtra("name"));
                input_mobile.setText(getIntent().getStringExtra("mobile"));

                chk_banned.setVisibility(View.VISIBLE);
                if(getIntent().getStringExtra("type").equals("employee")) chk_empAccount.setChecked(true);
                if(getIntent().getStringExtra("account_active").equals("0")) chk_banned.setChecked(true);
            } else { // user
                chk_empAccount.setVisibility(View.GONE);

                progressDialog.setMessage("Loading Profile Details");
                progressDialog.show();

                // get list of all users, find info for current user and show that
                RetrofitRequest.
                createService(STORE.API_Client.class).
                user_list().
                enqueue(new Callback<STORE.RM_Users>() {
                    @Override
                    public void onResponse(Call<STORE.RM_Users> call, Response<STORE.RM_Users> response) {
                        progressDialog.dismiss();

                        if (response.isSuccessful()) {
                            Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                            if (response.body().getStatus() == 1) {
                                for (STORE.User u: response.body().getUsers()) {
                                    if(u.email.equals(logged_in_user)) {
                                        input_email.setText(u.email);
                                        input_name.setText(u.name);
                                        input_mobile.setText(u.mobile);
                                        if(u.type.equals("employee")) chk_empAccount.setChecked(true);
                                        if(u.account_active.equals("0")) chk_banned.setChecked(true);
                                        break;
                                    }
                                }
                            }

                        } else {
                            Log.i("#####_failure", "" + response.code());
                            Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<STORE.RM_Users> call, Throwable t) {
                        progressDialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), "Cannot Connect to Server", Snackbar.LENGTH_LONG).show();
                        Log.i("#####_HARD_FAIL", "StackTrace:");
                        t.printStackTrace();
                    }
                });
            }
        }
    }

    public void validate() {
        boolean valid = true;

        String email = input_email.getText().toString().toLowerCase();
        String password = input_password.getText().toString();
        String name = input_name.getText().toString();
        String mobile = input_mobile.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email_container.setError("Enter a valid email address");
            valid = false;
        } else {
            input_email_container.setError(null);
        }

        if (password.isEmpty() && mode.equals("add")) {
            input_password_container.setError("Password can't be empty");
            valid = false;
        } else {
            input_password_container.setError(null);
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

        if(valid) {
            if(mode.equals("add"))  upload();
            if(mode.equals("edit")) update();
        }
    }

    public void upload() {
        progressDialog.setMessage("Creating Account");
        progressDialog.show();

        final String name = input_name.getText().toString();
        final String email = input_email.getText().toString().toLowerCase();
        final String password = input_password.getText().toString();
        final String mobile = input_mobile.getText().toString();
        final String type = chk_empAccount.isChecked() ? "employee" : "user";

        RetrofitRequest.
        createService(STORE.API_Client.class).
        register(email, password, name, mobile, type).
        enqueue(new Callback<STORE.ResponseModel>() {
            @Override
            public void onResponse(Call<STORE.ResponseModel> call, Response<STORE.ResponseModel> response) {
                if (response.isSuccessful()) {
                    Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                    if(response.body().getStatus() == -1 || response.body().getStatus() == 0) {
                        progressDialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                    } else if(response.body().getStatus() == 1) {
                        Toast.makeText(AdminAccountsAddEdit.this, "Account Created", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), AdminAccounts.class));
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

    public void update() {
        progressDialog.setMessage("Updating Account Details");
        progressDialog.show();

        final String email = input_email.getText().toString().toLowerCase();
        final String password = input_password.getText().toString();
        final String name = input_name.getText().toString();
        final String mobile = input_mobile.getText().toString();
        final String type = chk_empAccount.isChecked() ? "employee" : "user";
        final String account_active = chk_banned.isChecked() ? "0" : "1";

        RetrofitRequest.
        createService(STORE.API_Client.class).
        user_update(email, password, name, mobile, type, account_active).
        enqueue(new Callback<STORE.ResponseModel>() {
            @Override
            public void onResponse(Call<STORE.ResponseModel> call, Response<STORE.ResponseModel> response) {
                if (response.isSuccessful()) {
                    Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                    if(response.body().getStatus() == -1 || response.body().getStatus() == 0) {
                        progressDialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                    } else if(response.body().getStatus() == 1) {
                        Toast.makeText(AdminAccountsAddEdit.this, "Account Updated", Toast.LENGTH_SHORT).show();

                        if(logged_in_user.equals("admin"))
                            startActivity(new Intent(getApplicationContext(), AdminAccounts.class));
                        else
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.action_delete) return false;

        new AlertDialog
        .Builder(this)
        .setTitle("Delete Account")
        .setMessage("Are you sure you want to delete this account?")
        .setCancelable(false)
        .setNegativeButton("NO", null)
        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final ProgressDialog progressDialog;
                progressDialog = new ProgressDialog(AdminAccountsAddEdit.this, R.style.Theme_AppCompat_Light_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Processing");
                progressDialog.show();

                RetrofitRequest.
                createService(STORE.API_Client.class).
                user_delete(input_email.getText().toString()).
                enqueue(new Callback<STORE.ResponseModel>() {
                    @Override
                    public void onResponse(Call<STORE.ResponseModel> call, Response<STORE.ResponseModel> response) {
                        progressDialog.dismiss();

                        if (response.isSuccessful()) {
                            Log.i("#####_success", response.body().getStatus() + " / " + response.body().getMessage());

                            if(response.body().getStatus() == -1 || response.body().getStatus() == 0) {
                                Snackbar.make(findViewById(android.R.id.content), "Server Error", Snackbar.LENGTH_LONG).show();
                            } else if(response.body().getStatus() == 1) {
                                Toast.makeText(AdminAccountsAddEdit.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), AdminAccounts.class));
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
            }})
        .show();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mode.equals("edit") && logged_in_user.equals("admin"))
            getMenuInflater().inflate(R.menu.admin_user_edit_menu, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        if(logged_in_user.equals("admin"))
            startActivity(new Intent(getApplicationContext(), AdminAccounts.class));
        else
            startActivity(new Intent(getApplicationContext(), BrowsePosts.class));

        finish();
        overridePendingTransition(R.anim.enter_right, R.anim.exit_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
