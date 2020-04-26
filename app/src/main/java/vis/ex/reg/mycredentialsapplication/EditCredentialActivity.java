package vis.ex.reg.mycredentialsapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import vis.ex.reg.mycredentialsapplication.encryption.DataEncryptionSystem;


public class EditCredentialActivity extends AppCompatActivity {

    private int highestCredentialIndex;
    private int credentialID = -1;
    private CheckConnectivity connectivityMonitor;
    private Snackbar connectivitySnackBar;
    private boolean connectionAvailable;
    private boolean connectivitySnackBarIsDisplayed = false;

    private String key1 = "0123456789101112";
    private String key2 = "abecedghijklmnop";
    private String key3 = "zyxwvutsrqopnmlk";
    private Credential credential;
    private AppDatabase database;
    private EditText serviceNameEditText;
    private EditText serviceUrlEditText;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText noteEditText;
    private DataEncryptionSystem dataEncryptionSystem = new DataEncryptionSystem();
    private RequestQueue queue;

    private String username = "user";
    private String password = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_credential);

        serviceNameEditText = findViewById(R.id.editServiceName);
        serviceUrlEditText = findViewById(R.id.editServiceUrl);
        usernameEditText = findViewById(R.id.editUsername);
        emailEditText = findViewById(R.id.editEmail);
        passwordEditText = findViewById(R.id.editPassword);
        noteEditText = findViewById(R.id.editNotes);

        connectivitySnackBar = initializeConnectivitySnackBar();
        initializeConnectivityMonitor();
        connectionAvailable = internetConnectionIsAvailable();

        queue = VolleyController.getInstance(this.getApplicationContext()).getRequestQueue();
        database = AppDatabase.getDatabase(getApplicationContext());

        // Get the transferred data from source activity.
        Intent intent = getIntent();
        if (intent.getStringExtra("action").equals("addCredential")) {
            highestCredentialIndex = Integer.valueOf(intent.getStringExtra("highestCredentialIndex"));
        } else if (intent.getStringExtra("action").equals("editCredential")) {
            credential = (Credential) intent.getSerializableExtra("credential");
            credentialID = credential.getCredential_ID();
            populateCredentialView(credential);
        }

        if (!connectionAvailable) {
            connectivitySnackBar.show();
            connectivitySnackBarIsDisplayed = true;
        }

        final ToggleButton toggleButton = findViewById(R.id.encrypt_button2);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordEditText.setText(decryptPassword(credential.getEncryptedPassword()));
                    toggleButton.setBackgroundResource(R.drawable.visibility_off);
                } else {
                    passwordEditText.setText(R.string.hidden_password_text);
                    toggleButton.setBackgroundResource(R.drawable.visibility_on);
                }
            }
        });

        FloatingActionButton saveCredentialButton = findViewById(R.id.saveCredential);
        saveCredentialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCredential();
            }
        });
    }

    private void saveCredential() {
        Credential newCredential = new Credential();

        if (credentialID == -1) {
            credentialID = highestCredentialIndex + 1;
//          credentialID = (highestCredentialIndex - (highestCredentialIndex % 10)) + 10 + Integer.valueOf(getResources().getString(R.string.device_number));
            newCredential.setEncryptedPassword(encryptPassword(passwordEditText.getText().toString()));
        } else {
            if (passwordEditText.getText().toString().equals(getResources().getString(R.string.hidden_password_text))) {
                newCredential.setEncryptedPassword(credential.getEncryptedPassword());
            } else {
                newCredential.setEncryptedPassword(encryptPassword(passwordEditText.getText().toString()));
            }
        }
        newCredential.setCredential_ID(credentialID);
        newCredential.setServiceUrl(serviceUrlEditText.getText().toString());
        newCredential.setServiceName(serviceNameEditText.getText().toString());
        newCredential.setUsername(usernameEditText.getText().toString());
        newCredential.setEmail(emailEditText.getText().toString());
        newCredential.setDateLastModified(String.valueOf(System.currentTimeMillis()));
        newCredential.setNote(noteEditText.getText().toString());
        newCredential.setActive(true);

        if (connectionAvailable) {
            postCredentialToAPI(newCredential.toJSON());
        }
        database.credentialDAO().addCredential(newCredential);
        navigateToCredentialActivity();
    }

    private void navigateToCredentialActivity() {
        Intent intent = new Intent(EditCredentialActivity.this, CredentialActivity.class);
        intent.putExtra("action", "viewCredential");
        intent.putExtra("credential", String.valueOf(credentialID));
        unregisterReceiver(connectivityMonitor);
        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
    }

    private String encryptPassword(String clearText) {
        String encryptedText = "";
        try {
            encryptedText = dataEncryptionSystem.EncryptTripleDES(clearText, key1, key2, key3)[1];
        } catch (IOException e) {
            Log.e("DataEncryptionSystem", e.getMessage());
        }
        return encryptedText;
    }

    private String decryptPassword(String clearText) {
        String decryptedText = "";
        try {
            decryptedText = dataEncryptionSystem.DecryptTripleDES(clearText, key1, key2, key3);
        } catch (IOException e) {
            Log.e("DataEncryptionSystem", e.getMessage());
        }
        return decryptedText;
    }

    private void populateCredentialView(Credential credential) {

        serviceNameEditText.setText(credential.getServiceName());
        serviceUrlEditText.setText(credential.getServiceUrl());
        usernameEditText.setText(credential.getUsername());
        emailEditText.setText(credential.getEmail());
        passwordEditText.setText(R.string.hidden_password_text);
        noteEditText.setText(credential.getNote());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EditCredentialActivity.this, MainActivity.class);
        unregisterReceiver(connectivityMonitor);
        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
        finish();
    }

    private void postCredentialToAPI(JSONObject jsonObject) {
        String url = getResources().getString(R.string.api_url) + "/credentials/";

        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("CredentialsApp", "Request sent" + response);
//                Credential updatedCredential = convertJsonStringToCredentialObj(response);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CredentialsApp", "EditCredentialActivity, updateCredentialInLocalDB: " + error.getMessage());
            }
        };

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject, responseListener, errorListener) {

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String credentials = username + ":" + password;
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                int statusCode = response.statusCode;
                Log.d("CredentialsApp", "Request status code: " + statusCode);
                return super.parseNetworkResponse(response);
            }
        };
        queue.add(request);
    }

    private boolean internetConnectionIsAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
            || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    private Snackbar initializeConnectivitySnackBar() {
        Snackbar snackBar = Snackbar.make(findViewById(R.id.edit_credential_page), "Connection unavailable", Snackbar.LENGTH_INDEFINITE);
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

            } else if (state.equals("unavailable") && !connectivitySnackBarIsDisplayed) {
                connectivitySnackBar.show();
                connectivitySnackBarIsDisplayed = true;
                connectionAvailable = false;
            }
            Log.d("CredentialsApp", "connectionAvailable: " + connectionAvailable);
        }
    };
}
