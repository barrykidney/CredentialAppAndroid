package vis.ex.reg.mycredentialsapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class CredentialActivity extends AppCompatActivity {

    private Credential credential;
    private TextView serviceNameTextView;
    private TextView serviceUrlTextView;
    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView passwordTextView;
    private TextView noteTextView;
    private AppDatabase database;
    private boolean internetConnection;
    private CheckConnectivity connectivityChecker;
    private boolean snackBarDisplayed;
    private Snackbar snackBar;
    private ConstraintLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential);

        coordinatorLayout = findViewById(R.id.credential_page);
        connectivityChecker = new CheckConnectivity();

        ConnectivityManager connectivityManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        internetConnection = (connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            .getState() == NetworkInfo.State.CONNECTED || connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            .getState() == NetworkInfo.State.CONNECTED);

        this.registerReceiver(this.connectivityChecker,
            new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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
            JSONObject obj = new JSONObject(intent.getStringExtra("credential"));
            Log.d("CredentialsApp", obj.toString());

            String serviceName = (obj.has("serviceName") ? obj.getString("serviceName") : "" );
            String serviceUrl = (obj.has("serviceUrl") ? obj.getString("serviceUrl") : "" );
            String username = (obj.has("username") ? obj.getString("username") : "" );
            String email = (obj.has("email") ? obj.getString("email") : "" );
            String password = (obj.has("password") ? obj.getString("password") : "" );
            String note = (obj.has("note") ? obj.getString("note") : "" );

            credential = new Credential(Integer.valueOf(obj.getString("id")), serviceName, serviceUrl, username,
                email, password, obj.getString("dateLastModified"), note, obj.getBoolean("active"));

            populateCredentialView(obj);
        } catch (JSONException error) {
            Log.e("CredentialsApp", error.getMessage());
        }

        FloatingActionButton editCredentialButton = findViewById(R.id.editCredential);
        editCredentialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
                editCredential();
            }
        });

        FloatingActionButton deleteCredentialButton = findViewById(R.id.deleteCredential);
        deleteCredentialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Deleting credential", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

                deleteCredential(String.valueOf(credential.Credential_ID));
            }
        });
    }

    private void populateCredentialView(JSONObject credential) {
        try {
            serviceNameTextView.setText(credential.getString("serviceName"));
            serviceUrlTextView.setText(credential.getString("serviceUrl"));
            usernameTextView.setText(credential.getString("username"));
            emailTextView.setText(credential.getString("email"));
            passwordTextView.setText(credential.getString("encodedPassword"));
            noteTextView.setText(credential.getString("note"));
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
        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
        finish();
    }

    private void editCredential() {
        Intent intent = new Intent(CredentialActivity.this, AddCredentialActivity.class);
        intent.putExtra("action", "viewCredential");
        intent.putExtra("credential", credential.toJSON().toString());
        // unregisterReceiver(this.connectivityChecker);
        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
    }

    private void deleteCredential(final String credentialId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getResources().getString(R.string.api_url) + "/credentials/" + credentialId;

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    CredentialActivity.UpdateDBAsync updateDBAsync = new CredentialActivity.UpdateDBAsync();
                    updateDBAsync.execute(Integer.valueOf(credentialId));

                    Intent intent = new Intent(CredentialActivity.this, MainActivity.class);
//                    unregisterReceiver(this.connectivityChecker);
                    unregisterReceiver(broadcastReceiver);
                    startActivity(intent);
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CredentialsApp", error.toString());
            }
        });
        queue.add(request);
    }

//    private void deleteCredentialFromLocalDB(final int credentialId) {
//        database.credentialDAO().deleteCredential(credentialId);
//    }


    class UpdateDBAsync extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {
            database.credentialDAO().deleteCredential(params[0]);
            return 1;
        }
    }
}
