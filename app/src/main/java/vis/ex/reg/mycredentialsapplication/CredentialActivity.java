package vis.ex.reg.mycredentialsapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class CredentialActivity extends AppCompatActivity {

    private TextView serviceNameTextView;
    private TextView serviceUrlTextView;
    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView passwordTextView;
    private TextView noteTextView;

//    private String credentialId;
//    private Credential credential;
    private AppDatabase database;
//    private CheckConnectivity connectivityChecker;
    private boolean snackBarDisplayed;
    private Snackbar snackBar;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential);

        coordinatorLayout = findViewById(R.id.credential_page);
//        connectivityChecker = new CheckConnectivity();

        ConnectivityManager connectivityManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean internetConnection = (connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            .getState() == NetworkInfo.State.CONNECTED || connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            .getState() == NetworkInfo.State.CONNECTED);

//        this.registerReceiver(this.connectivityChecker,
//            new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        this.registerReceiver(broadcastReceiver, new IntentFilter("connection_change"));

        database = AppDatabase.getDatabase(getApplicationContext());
        serviceNameTextView = findViewById(R.id.serviceName);
        serviceUrlTextView = findViewById(R.id.serviceUrl);
        usernameTextView = findViewById(R.id.username);
        emailTextView = findViewById(R.id.email);
        passwordTextView = findViewById(R.id.password);
        noteTextView = findViewById(R.id.note);

        // Get the transferred data from source activity.
        Intent intent = getIntent();
        try {
            JSONObject credentialJson = new JSONObject(intent.getStringExtra("credential"));
            populateCredentialView(credentialJson);
        } catch (JSONException error) {
            Log.e("CredentialsApp", error.getMessage());
        }


//
//        UpdateViewAsync updateViewAsync = new UpdateViewAsync();
//        updateViewAsync.execute(Integer.valueOf(credentialId));

//        if (internetConnection) {
//            getCredentialFromAPI();
//        } else {
//            UpdateViewAsync updateViewAsync = new UpdateViewAsync();
//            updateViewAsync.execute(Integer.valueOf(credentialId));
//        }
    }

    private void populateCredentialView(JSONObject credential) {
        try {
            serviceNameTextView.setText(credential.getString("serviceName"));
            serviceUrlTextView.setText(credential.getString("serviceUrl"));
            usernameTextView.setText(credential.getString("username"));
            emailTextView.setText(credential.getString("email"));
            passwordTextView.setText(credential.getString("password"));
            noteTextView.setText(credential.getString("username"));
        } catch (JSONException error) {
            Log.e("CredentialsApp", error.getMessage());
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { String state = intent.getStringExtra("state");
        if (state.equals("available") && snackBarDisplayed) {
            snackBar.dismiss();
            snackBarDisplayed = false;
//            getCredentialFromAPI();
        } else if (state.equals("unavailable") && !snackBarDisplayed) {
            snackBar = Snackbar.make(coordinatorLayout, "Connection Lost", Snackbar.LENGTH_INDEFINITE);
            View sbView = snackBar.getView();
            sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbarBackgroundColor));
            snackBar.show();
            snackBarDisplayed = true;
        }
        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CredentialActivity.this, MainActivity.class);
//        unregisterReceiver(this.connectivityChecker);
//        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
        finish();
    }
}
