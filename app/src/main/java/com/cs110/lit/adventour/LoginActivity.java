package com.cs110.lit.adventour;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cs110.lit.adventour.model.User;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements
        LoginFragment.LoginFragmentListener, RegisterFragment.RegisterFragmentListener {

    // TODO: Improve the naming scheme of the fragment interface methods

    /**
     * To record that a user session has been registered.
     */
    SharedPreferences prefs;

    /**
     * The object in which we record the user's active session.
     */
    SharedPreferences.Editor editor;

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        prefs = getApplicationContext().getSharedPreferences("Login", 0);
        editor = prefs.edit();

        if (prefs.getBoolean("auth", false)) {
            showBrowseListView();
            return;
        }

        setContentView(R.layout.activity_login);

        final FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction defTransaction = fragmentManager.beginTransaction();
        defTransaction.replace(R.id.login_activity_form, LoginFragment.newInstance(this));
        defTransaction.addToBackStack(null);
        defTransaction.commit();

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        Random r = new Random(System.currentTimeMillis());
        int i = r.nextInt(login_bg_imgs.length);
        ImageView login_bg = (ImageView) findViewById(R.id.login_bg);
        TextView login_title = (TextView) findViewById(R.id.login_title);
        login_bg.setImageResource(login_bg_imgs[i]);
        Typeface t = Typeface.createFromAsset(getAssets(), "fonts/BerninoSansCondensedEB.ttf");
        login_title.setTypeface(t);
    }

    public void onSignInClicked (EditText mEmailView, EditText mPasswordView) {
        attemptLogin(mEmailView, mPasswordView);
    }

    public void onRegisterClicked () {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        System.out.println("Clicked Register");
        ft.replace(R.id.login_activity_form, RegisterFragment.newInstance(this));
        ft.addToBackStack(null);
        ft.commit();
    }

    public void onSignUpClicked (EditText emailInput, EditText usernameInput, EditText passwordInput) {
        System.out.println("user tried to sign up!");
        attemptRegister(emailInput, usernameInput, passwordInput);
    }

    public void onLogInClicked () {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        System.out.println("Clicked Log In");
        ft.replace(R.id.login_activity_form, LoginFragment.newInstance(this));
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister (final EditText mEmailView, final EditText mUsernameView, final EditText mPasswordView) {
        String email = mEmailView.getText().toString();
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (validateInput(mEmailView, mUsernameView, mPasswordView)) {
            DB.registerUser(username, email, md5(password), this, new DB.Callback<User>() {
                @Override
                public void onSuccess (User u) {
                    System.out.println("registered " + u.getUser_email() + " successfully");
                    login(u);
                }

                @Override
                public void onFailure (User u) {
                    mEmailView.setError("This email address is taken.");
                }
            });
        }
    }

    /**
     * TODO: PUT THIS IN A SINGLETON "SESSION" CLASS
     *
     * @param u
     */
    private void login(User u) {
        editor.putBoolean("auth", true);
        editor.putInt("uid", u.getUser_id());
        editor.putString("uemail", u.getUser_email());
        editor.putString("uname", u.getUser_name());
        editor.commit();
        showBrowseListView();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin (final EditText mEmailView, final EditText mPasswordView) {
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (validateInput(mEmailView, null, mPasswordView)) {
            DB.authenticateUser(email, md5(password), this, new DB.Callback<User>() {
                @Override
                public void onSuccess (User u) {
                    System.out.println("logged " + u.getUser_email() + " in successfully");
                    login(u);
                }

                @Override
                public void onFailure (User u) {
                    System.out.println("authentication failed");
                    mPasswordView.setError("Incorrect username or password.");
                }
            });
        }
    }

    /**
     * TODO
     * @param mEmailView
     * @param mUsernameView
     * @param mPasswordView
     * @return
     */
    private boolean validateInput(EditText mEmailView, EditText mUsernameView, EditText mPasswordView) {
        // Reset errors.
        mEmailView.setError(null);
        String email = mEmailView.getText().toString();

        String username = null;
        if (mUsernameView != null) {
            mUsernameView.setError(null);
            username = mUsernameView.getText().toString();
        }

        String password = mPasswordView.getText().toString();
        mPasswordView.setError(null);

        // Keep track of any validation errors that occurred.
        boolean valid = true;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        // Check for a valid username (non-empty)
        if (username != null) {
            if (TextUtils.isEmpty(username)) {
                mUsernameView.setError(getString(R.string.error_field_required));
                valid = false;
            }
        }

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            valid = false;
        }

        return valid;
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
        Intent intent = new Intent(this, BrowseViewActivity.class);
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
     * TODO: Should this go here in LoginActivity?
     * Return the md5 encryption of a given string. Used for password hashing.
     *
     * @param s the string to be encrypted
     * @return the md5 encryption of the string s
     */
    public static String md5 (String s) {
        try {
            // Create MD5 Hash
            MessageDigest m = java.security.MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());
            String hash = (new BigInteger(1, m.digest())).toString(16);
            int len = hash.length();
            for (int i = len; i < 32; i++) {
                hash = "0" + hash;
            }
            return hash;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onBackPressed()
    {
        // code here to show dialog
        super.onBackPressed();
        finish();// optional depending on your needs
    }
}
