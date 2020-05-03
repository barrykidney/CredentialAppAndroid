package vis.ex.reg.mycredentialsapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import vis.ex.reg.mycredentialsapplication.encryption.DataEncryptionSystem;

public class ProfileActivity extends AppCompatActivity {

    private AppDatabase database;
    private CheckConnectivity connectivityMonitor;
    private Snackbar connectivitySnackBar;
    private boolean connectionAvailable;
    private boolean connectivitySnackBarIsDisplayed = false;

    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView passwordTextView;

    private DataEncryptionSystem dataEncryptionSystem = new DataEncryptionSystem();
    private boolean authenticatedTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        usernameTextView = findViewById(R.id.profile_username);
        emailTextView = findViewById(R.id.profile_email);
        passwordTextView = findViewById(R.id.profile_password);

        database = AppDatabase.getDatabase(getApplicationContext());

        connectivitySnackBar = initializeConnectivitySnackBar();
        initializeConnectivityMonitor();
        connectionAvailable = internetConnectionIsAvailable();
        if (!connectionAvailable) {
            connectivitySnackBar.show();
            connectivitySnackBarIsDisplayed = true;
        }

        Intent intent = getIntent();
//        userId = Integer.valueOf(intent.getStringExtra("user"));
//        Log.e("CredentialsApp", "User: " + userId);
        new ProfileActivity.UpdateViewAsync().execute();

        try {
            Log.e("CredentialsApp", "This is saoirses password");
            Log.e("CredentialsApp", Sha1Encryption.SHA1("This is saoirses password"));

            Log.e("CredentialsApp", "This is julies password");
            Log.e("CredentialsApp", Sha1Encryption.SHA1("This is julies password"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException error) {
            Log.e("CredentialsApp", error.toString());
        }

        FloatingActionButton editUserButton = findViewById(R.id.editUser);
        editUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editUser();
            }
        });
    }

    private void populateProfileView(User user) {
        usernameTextView.setText(user.getUsername());
        emailTextView.setText(user.getEmail());
        passwordTextView.setText(R.string.hidden_password_text);
    }

    private void editUser() {
        Log.e("CredentialsApp", "To be implemented");
    }

    class UpdateViewAsync extends AsyncTask<Void, Void, List<User>> {

        @Override
        protected void onPostExecute(List<User> result) {
            if (result.size() == 1) {
                User user = result.get(0);
                populateProfileView(user);
            } else {
                Log.e("CredentialsApp", "MainActivity:UpdateViewAsync, No information available.");
            }
        }
        @Override
        protected List<User> doInBackground(Void... params) {
            return database.userDAO().getAllUsers();
        }
    }











    private Snackbar initializeConnectivitySnackBar() {
        Snackbar snackBar = Snackbar.make(findViewById(R.id.profile_page), "Connection unavailable", Snackbar.LENGTH_INDEFINITE);
        View snackBarView = snackBar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbarBackgroundColor));
        TextView snackBarTextView = snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        if (Build.VERSION.SDK_INT >= 21) {
            snackBarTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            snackBarTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        return snackBar;
    }

    private boolean internetConnectionIsAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
            || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    private void initializeConnectivityMonitor() {
        connectivityMonitor = new CheckConnectivity();
        this.registerReceiver(this.connectivityMonitor, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        this.registerReceiver(broadcastReceiver, new IntentFilter("connection_change"));
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("state");

            if (state.equals("available") && connectivitySnackBarIsDisplayed) {
                connectivitySnackBar.dismiss();
                connectivitySnackBarIsDisplayed = false;
                connectionAvailable = true;
//                getAllCredentialsFromAPI();

            } else if (state.equals("unavailable") && !connectivitySnackBarIsDisplayed) {
                connectivitySnackBar.show();
                connectivitySnackBarIsDisplayed = true;
                connectionAvailable = false;
            }
        }
    };
}
