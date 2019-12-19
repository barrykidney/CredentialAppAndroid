package vis.ex.reg.mycredentialsapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class AddCredentialActivity extends AppCompatActivity {

    private AppDatabase database;
    private boolean internetConnection;
    private CheckConnectivity connectivityChecker;
    private boolean snackBarDisplayed;
    private Snackbar snackBar;
    private ConstraintLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_credential);

        coordinatorLayout = findViewById(R.id.add_credential_page);
        connectivityChecker = new CheckConnectivity();

        ConnectivityManager connectivityManager =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        internetConnection = (connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            .getState() == NetworkInfo.State.CONNECTED || connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            .getState() == NetworkInfo.State.CONNECTED);

        this.registerReceiver(this.connectivityChecker, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        this.registerReceiver(broadcastReceiver, new IntentFilter("connection_change"));

        database = AppDatabase.getDatabase(getApplicationContext());

        FloatingActionButton addCredentialButton = findViewById(R.id.saveCredential);
        addCredentialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "New credential saved", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

                EditText serviceName = findViewById(R.id.addServiceName);
                EditText serviceUrl = findViewById(R.id.addServiceUrl);
                EditText username = findViewById(R.id.addUsername);
                EditText email = findViewById(R.id.addEmail);
                EditText password = findViewById(R.id.addPassword);
                EditText note = findViewById(R.id.addNotes);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("serviceName", serviceName.getText().toString());
                    jsonObject.put("serviceUrl", serviceUrl.getText().toString());
                    jsonObject.put("username", username.getText().toString());
                    jsonObject.put("email", email.getText().toString());
                    jsonObject.put("encodedPassword", email.getText().toString());
                    jsonObject.put("note", note.getText().toString());
                } catch(JSONException e) {
                    Log.e("CredentialsApp", e.getMessage());
                }
                postNewCredentialToAPI(jsonObject);
            }
        });
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
        Intent intent = new Intent(AddCredentialActivity.this, MainActivity.class);
        unregisterReceiver(this.connectivityChecker);
        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
        finish();
    }

    private void postNewCredentialToAPI(JSONObject jsonObject) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getResources().getString(R.string.api_url) + "/credentials/";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Intent intent = new Intent(AddCredentialActivity.this, CredentialActivity.class);
                    intent.putExtra("credential", response.toString());
//                    unregisterReceiver(this.connectivityChecker);
                    unregisterReceiver(broadcastReceiver);
                    startActivity(intent);
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CredentialsApp", error.getMessage());
            }
        });
        queue.add(request);
    }
}
