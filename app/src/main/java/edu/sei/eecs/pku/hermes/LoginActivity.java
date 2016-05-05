package edu.sei.eecs.pku.hermes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import edu.sei.eecs.pku.hermes.configs.Constants;
import edu.sei.eecs.pku.hermes.utils.network.GsonRequest;
import edu.sei.eecs.pku.hermes.utils.network.HttpClientRequest;
import edu.sei.eecs.pku.hermes.utils.network.OAuthResponseGson;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
@EActivity(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "jty2005@qq.com:635241"
    };


    public static LoginActivity instance;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private RequestQueue queue;

    private SharedPreferences loginInfo;
    private SharedPreferences.Editor editor;

    // UI references.
    @ViewById(R.id.email)
    public AutoCompleteTextView mUsernameView;

    @ViewById(R.id.password)
    public EditText mPasswordView;

    @ViewById(R.id.login_progress)
    public CircleProgressBar mProgressView;

    @ViewById(R.id.login_form)
    public View mLoginFormView;

    @ViewById(R.id.llHeaderProgress)
    LinearLayout llHeaderProgress;

    @Click
    void email_sign_in_button() {
        Log.d("login", "login!");
        attemptLogin();
    }

    @Click
    void normal_user_button() {
        CustomerLoginActivity_.intent(LoginActivity.this).start();
        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//        init();
        instance = this;
        loginInfo = getSharedPreferences("login", MODE_PRIVATE);
        queue = HttpClientRequest.getInstance(this.getApplicationContext()).getRequestQueue();
    }


    @AfterViews
    public void init() {
//        setupActionBar();
        // Set up the login form.

        populateAutoComplete();
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
        mProgressView.setColorSchemeResources(R.color.colorPrimary);
        boolean isLogged = loginInfo.getBoolean("isLogged", false);
        boolean isCourier = loginInfo.getBoolean("isCourier", true);
        String username = loginInfo.getString("courier_id", "");
        mUsernameView.setText(username);

        if (username.length() > 0) {
            mPasswordView.requestFocus();
        }

        if (isLogged) {
            if (isCourier) {
                String refresh_token = loginInfo.getString("refresh_token", "default");
                long expires_by = loginInfo.getLong("expires_by", 0);
                if (System.currentTimeMillis() / 1000 < expires_by) {
                    relogin(username, refresh_token);
                } else {
                    Toast.makeText(LoginActivity.this, "长期未使用，请重新登录", Toast.LENGTH_SHORT).show();
                }
            } else {
                TodayActivity_.intent(LoginActivity.this).start();
                finish();
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            }
        }
    }

    private void relogin(final String username, final String refresh_token) {
        showProgress(true);

        GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
                .post()
                .url(Constants.LOGIN_URL)
                .addParams("client_id", Constants.CLIENT_ID)
                .addParams("client_secret", Constants.CLIENT_SECRET)
                .addParams("grant_type", "refresh_token")
                .addParams("refresh_token", refresh_token)
                .clazz(OAuthResponseGson.class)
                .successListener(new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        completeLogin(username, ((OAuthResponseGson)response).getAccess_token(),
                                ((OAuthResponseGson)response).getRefresh_token(),
                                ((OAuthResponseGson)response).getExpires_in());

                    }
                })
                .errorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "自动登录失败，请重新登录", Toast.LENGTH_LONG).show();
                        showProgress(false);
                    }
                })
                .build();
        queue.add(gsonRequest);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mUsernameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String username = mUsernameView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }
//        else if (!isEmailValid(email)) {
//            mUsernameView.setError(getString(R.string.error_invalid_email));
//            focusView = mUsernameView;
//            cancel = true;
//        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
            GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
                    .post()
                    .url(Constants.LOGIN_URL)
                    .addParams("client_id", Constants.CLIENT_ID)
                    .addParams("client_secret", Constants.CLIENT_SECRET)
                    .addParams("grant_type", "password")
                    .addParams("username", username)
                    .addParams("password", password)
                    .clazz(OAuthResponseGson.class)
                    .successListener(new Response.Listener() {
                        @Override
                        public void onResponse(Object response) {
                            completeLogin(username, ((OAuthResponseGson)response).getAccess_token(),
                                    ((OAuthResponseGson)response).getRefresh_token(),
                                    ((OAuthResponseGson)response).getExpires_in());

                        }
                    })
                    .errorListener(new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(LoginActivity.this, "登录失败，请检查您的用户名密码", Toast.LENGTH_LONG).show();
                            showProgress(false);
                        }
                    })
                    .build();
            queue.add(gsonRequest);
        }
    }

    private void completeLogin(final String username, final String access_token,
                               final String refresh_token, final long expires_in) {
        GsonRequest gsonRequest = new GsonRequest.RequestBuilder()
                .post()
                .url(Constants.AUTH_URL)
                .addParams("courier_id", username)
                .addParams("access_token", access_token)
                .addParams("refresh_token", refresh_token)
                .addParams("expires_in", String.valueOf(expires_in))
                .clazz(OAuthResponseGson.class)
                .successListener(new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {

                        Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();

                        editor = loginInfo.edit();
                        editor.putBoolean("isLogged", true);
                        editor.putBoolean("isCourier", true);
                        editor.putString("courier_id", username);
                        editor.putString("access_token", access_token);
                        editor.putString("refresh_token", refresh_token);
                        editor.putLong("expires_by", System.currentTimeMillis()/1000 + expires_in);
                        editor.apply();
                        InitApplication.courier_id = username;
                        TodayActivity_.intent(LoginActivity.this).start();
                        finish();
                        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                    }
                })
                .errorListener(new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        showProgress(false);
                    }
                })
                .build();
        queue.add(gsonRequest);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            llHeaderProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            llHeaderProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    llHeaderProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            llHeaderProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUsernameView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                PlanResultActivity_.intent(LoginActivity.this).start();
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

