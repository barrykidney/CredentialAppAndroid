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

    private int highestCredentialIndex;
    private AppDatabase database;
    private boolean internetConnection;
    private CheckConnectivity connectivityChecker;

    private int credentialID = -1;
    private String dateLastModified;

    private boolean snackBarDisplayed;
    private Snackbar snackBar;
    private ConstraintLayout coordinatorLayout;
    private EditText serviceNameEditText;
    private EditText serviceUrlEditText;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText noteEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_credential);

        coordinatorLayout = findViewById(R.id.add_credential_page);
        connectivityChecker = new CheckConnectivity();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        internetConnection = (connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            .getState() == NetworkInfo.State.CONNECTED || connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            .getState() == NetworkInfo.State.CONNECTED);

        this.registerReceiver(this.connectivityChecker, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        this.registerReceiver(broadcastReceiver, new IntentFilter("connection_change"));

        database = AppDatabase.getDatabase(getApplicationContext());

        serviceNameEditText = findViewById(R.id.addServiceName);
        serviceUrlEditText = findViewById(R.id.addServiceUrl);
        usernameEditText = findViewById(R.id.addUsername);
        emailEditText = findViewById(R.id.addEmail);
        passwordEditText = findViewById(R.id.addPassword);
        noteEditText = findViewById(R.id.addNotes);

        Intent intent = getIntent();
        if (intent.getStringExtra("action").equals("viewCredential")) {
            try {
                JSONObject obj = new JSONObject(intent.getStringExtra("credential"));
                credentialID = Integer.valueOf(obj.getString("id"));
                dateLastModified = obj.getString("dateLastModified");

                if (obj.has("serviceName")) {
                    serviceNameEditText.setText(obj.getString("serviceName"));
                }
                if (obj.has("serviceUrl")) {
                    serviceUrlEditText.setText(obj.getString("serviceUrl"));
                }
                if (obj.has("username")) {
                    usernameEditText.setText(obj.getString("username"));
                }
                if (obj.has("email")) {
                    emailEditText.setText(obj.getString("email"));
                }
                if (obj.has("password")) {
                    passwordEditText.setText(obj.getString("password"));
                }
                if (obj.has("note")) {
                    noteEditText.setText(obj.getString("note"));
                }
                // populateCredentialView(obj); // make the above code into a method and pass a credential object
            } catch (JSONException error) {
                Log.e("CredentialsApp", "AddCredentialActivity, onCreate, JSONObj: "  + error.getMessage());
            }
        } else {
            highestCredentialIndex = Integer.valueOf(intent.getStringExtra("highestCredentialIndex"));
        }

        FloatingActionButton addCredentialButton = findViewById(R.id.saveCredential);
        addCredentialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "New credential saved", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

                JSONObject jsonObject = new JSONObject();
                try {
                    if (credentialID == -1) {
                        credentialID = highestCredentialIndex + 1;
//                        credentialID =
//                            (highestCredentialIndex - (highestCredentialIndex % 10)) + 10
//                                + Integer.valueOf(getResources().getString(R.string.device_number));
                    }
                    jsonObject.put("id", credentialID);
                    jsonObject.put("id", credentialID);
                    jsonObject.put("serviceName", serviceNameEditText.getText().toString());
                    jsonObject.put("serviceUrl", serviceUrlEditText.getText().toString());
                    jsonObject.put("username", usernameEditText.getText().toString());
                    jsonObject.put("email", emailEditText.getText().toString());
                    jsonObject.put("encodedPassword", passwordEditText.getText().toString());
                    jsonObject.put("note", noteEditText.getText().toString());
                    jsonObject.put("dateLastModified", String.valueOf(System.currentTimeMillis()));
                    jsonObject.put("active", "true");
                } catch(JSONException e) {
                    Log.e("CredentialsApp", "AddCredentialActivity, onCreate FloatingAction: " + e.getMessage());
                }

                postCredentialToAPI(jsonObject);
//                Credential updatedCredential = convertJsonStringToCredentialObj(jsonObject);
//                updateCredentialInLocalDB(updatedCredential);

                Intent intent = new Intent(AddCredentialActivity.this, CredentialActivity.class);
                intent.putExtra("action", "viewCredential");
                intent.putExtra("credential", jsonObject.toString());
//                unregisterReceiver(this.connectivityChecker);
                unregisterReceiver(broadcastReceiver);
                startActivity(intent);
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
//        unregisterReceiver(this.connectivityChecker);
        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
        finish();
    }

    private Credential convertJsonStringToCredentialObj(JSONObject obj) {
        Credential credential = new Credential();
        try {
            String serviceName = (obj.has("serviceName") ? obj.getString("serviceName") : "" );
            String serviceUrl = (obj.has("serviceUrl") ? obj.getString("serviceUrl") : "" );
            String username = (obj.has("username") ? obj.getString("username") : "" );
            String email = (obj.has("email") ? obj.getString("email") : "" );
            String password = (obj.has("password") ? obj.getString("password") : "" );
            String note = (obj.has("note") ? obj.getString("note") : "" );

            credential = new Credential(Integer.valueOf(obj.getString("id")),
                serviceName, serviceUrl, username, email, password, obj.getString("dateLastModified"),
                note, obj.getBoolean("active"));

        } catch (JSONException error) {
            Log.e("CredentialsApp", "AddCredentialActivity, convertJsonStringToCredentialObj: " + error.getMessage());
        }
        return credential;
    }

    private void updateCredentialInLocalDB(Credential updatedCredential) {
        database.credentialDAO().addCredential(updatedCredential);
    }

    private void postCredentialToAPI(JSONObject jsonObject) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getResources().getString(R.string.api_url) + "/credentials/";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("CredentialsApp", "Request sent" + response);
                    Credential updatedCredential = convertJsonStringToCredentialObj(response);
                    updateCredentialInLocalDB(updatedCredential);
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CredentialsApp", "AddCredentialActivity, updateCredentialInLocalDB" + error.getMessage());
            }
        });
        queue.add(request);
    }
}
