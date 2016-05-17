package com.cs110.lit.adventour;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.cs110.lit.adventour.model.*;

import java.util.ArrayList;
import java.util.Random;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * To record that a user session has been registered.
     */
    SharedPreferences prefs;

    /**
     * The object in which we record the user's active session.
     */
    SharedPreferences.Editor editor;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;

    Integer [] login_bg_imgs = new Integer [] {
        R.drawable.login_bg_0,
        R.drawable.login_bg_1,
        R.drawable.login_bg_2,
        R.drawable.login_bg_3,
        R.drawable.login_bg_4,
        R.drawable.login_bg_5,
        R.drawable.login_bg_6,
        R.drawable.login_bg_7,
    };

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs =  getApplicationContext().getSharedPreferences("Login", 0);
        editor = prefs.edit();

        if (prefs.getBoolean("auth", false)) {
            showBrowseListView();
            return;
        }

        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        Random r = new Random(System.currentTimeMillis());
        int i = r.nextInt(login_bg_imgs.length);
        ImageView login_bg = (ImageView) findViewById(R.id.login_bg);
        TextView login_title = (TextView) findViewById(R.id.login_title);
        Typeface t = Typeface.createFromAsset(getAssets(), "fonts/BerninoSansCondensedEB.ttf");

        login_bg.setImageResource(login_bg_imgs[i]);
        login_title.setTypeface(t);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        Button mRegisterButton = (Button) findViewById(R.id.email_create_account_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mEmailSignInButton.setTypeface(t);
        mRegisterButton.setTypeface(t);

        mLoginFormView = findViewById(R.id.login_form);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin () {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            cancel = true;
        }

        if (!cancel) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();
            DB.authenticateUser(email, md5(password), this, new DB.Callback<User>() {
                @Override
                public void onSuccess (User u) {
                    progressDialog.dismiss();
                    System.out.println("logged " + u.getUser_email() + " in successfully");
                    editor.putBoolean("auth", true);
                    editor.putInt("uid", u.getUser_id());
                    editor.putString("uemail", u.getUser_email());
                    editor.putString("uname", u.getUser_name());
                    editor.commit();
                    showBrowseListView();
                }

                @Override
                public void onFailure (User u) {
                    progressDialog.dismiss();
                    System.out.println("authentication failed");
                    mPasswordView.setError("Incorrect username or password.");
                }
            });
        }
    }

    private boolean isEmailValid (String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid (String password) {
        return password.length() >= 8;
    }

    /**
     * Test if the list activity works properly
     * TODO: call this function when successfully logged in
     */
    public void showBrowseListView () {
        Intent intent = new Intent(this, BrowseListActivity.class);
        startActivity(intent);
        finish();
    }

    //Sean's map bullshit
    public void showBrowseMapView() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Test if the map activity works properly
     */
    public void showOverviewView() {
        Intent intent = new Intent(this, OverviewActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Return the md5 encryption of a given string. Used for password hashing.
     *
     * @param s the string to be encrypted
     * @return the md5 encryption of the string s
     */
    public static String md5 (String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
