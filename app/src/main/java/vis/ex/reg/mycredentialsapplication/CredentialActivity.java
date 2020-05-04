package vis.ex.reg.mycredentialsapplication;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vis.ex.reg.mycredentialsapplication.encryption.DataEncryptionSystem;


public class CredentialActivity extends AppCompatActivity {

    private AppDatabase database;
    private Credential credential;
    private User user;
    private CheckConnectivity connectivityMonitor;
    private Snackbar connectivitySnackBar;
    private boolean connectionAvailable;
    private boolean connectivitySnackBarIsDisplayed = false;

    private TextView serviceNameTextView;
    private TextView serviceUrlTextView;
    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView passwordTextView;
    private TextView noteTextView;
    private EditText loginUsernameTextView;
    private EditText loginPasswordTextView;
    private DataEncryptionSystem dataEncryptionSystem = new DataEncryptionSystem();
    private RequestQueue queue;
    private long authenticatedTime;
    private ToggleButton toggleButton;
    private String masterPassword = "";
    private String keyGenPassword = "";
    Dialog dialog;
    ImageView closeBtn;
    Button authBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        serviceNameTextView = findViewById(R.id.serviceName);
        serviceUrlTextView = findViewById(R.id.serviceUrl);
        usernameTextView = findViewById(R.id.username);
        emailTextView = findViewById(R.id.email);
        passwordTextView = findViewById(R.id.password);
        noteTextView = findViewById(R.id.note);
        int credentialId;

        queue = VolleyController.getInstance(this.getApplicationContext()).getRequestQueue();
        database = AppDatabase.getDatabase(getApplicationContext());

        connectivitySnackBar = initializeConnectivitySnackBar();
        initializeConnectivityMonitor();
        connectionAvailable = internetConnectionIsAvailable();
        if (!connectionAvailable) {
            connectivitySnackBar.show();
            connectivitySnackBarIsDisplayed = true;
        }

        new SetUserAsync().execute();

        Intent intent = getIntent();
        credentialId = Integer.valueOf(intent.getStringExtra("credential"));
        authenticatedTime = Long.valueOf(intent.getStringExtra("authenticatedTime"));
        masterPassword = intent.getStringExtra("masterPassword");
        keyGenPassword = intent.getStringExtra("keyGenPassword");
        UpdateViewAsync updateViewAsync = new UpdateViewAsync();
        updateViewAsync.execute(credentialId);


        toggleButton = findViewById(R.id.encrypt_button);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if (System.currentTimeMillis() - authenticatedTime < 30000) {
                        List<String> keysList = generateKeys(masterPassword);
                        passwordTextView.setText(decryptPassword(credential.getEncryptedPassword(), keysList));
                        toggleButton.setBackgroundResource(R.drawable.visibility_off);
                    } else {
                        loginToViewPassword();
                    }
                } else {
                    passwordTextView.setText(R.string.hidden_password_text);
                    toggleButton.setBackgroundResource(R.drawable.visibility_on);
                }
            }
        });

        FloatingActionButton editCredentialButton = findViewById(R.id.editCredential);
        editCredentialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editCredential();
            }
        });

        FloatingActionButton deleteCredentialButton = findViewById(R.id.deleteCredential);
        deleteCredentialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCredential();
            }
        });
    }

    private void loginToViewPassword() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.login);

        closeBtn = dialog.findViewById(R.id.btnclose);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        authBtn = dialog.findViewById(R.id.authenticatebutton);
        authBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUsernameTextView = dialog.findViewById(R.id.loginTextUsername);
                loginPasswordTextView = dialog.findViewById(R.id.loginTextPassword);

                try {
                    String loginUsername = loginUsernameTextView.getText().toString();
                    String loginHashedPassword = Sha1Encryption.SHA1(loginPasswordTextView.getText().toString());

                    if (loginUsername.equals(user.getUsername()) && loginHashedPassword.equals(user.getMasterPassword())) {
                        authenticatedTime = System.currentTimeMillis();
                        masterPassword = loginHashedPassword;
                        keyGenPassword = Sha1Encryption.SHA1(Utils.toHex(loginPasswordTextView.getText().toString()));
                        List<String> keysList = generateKeys(keyGenPassword);
                        passwordTextView.setText(decryptPassword(credential.getEncryptedPassword(), keysList));
                        toggleButton.setBackgroundResource(R.drawable.visibility_off);
                        dialog.dismiss();
                    } else {
                        authenticatedTime = 0L;
                        masterPassword ="";
                        keyGenPassword = "";
                        dialog.dismiss();
                    }
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException error) {
                    Log.e("CredentialsApp", error.toString());
                }
            }
        });
        dialog.show();
    }

    private void loginToDeleteCredential() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.login);

        closeBtn = dialog.findViewById(R.id.btnclose);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        authBtn = dialog.findViewById(R.id.authenticatebutton);
        authBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUsernameTextView = dialog.findViewById(R.id.loginTextUsername);
                loginPasswordTextView = dialog.findViewById(R.id.loginTextPassword);

                try {
                    String loginUsername = loginUsernameTextView.getText().toString();
                    String loginHashedPassword = Sha1Encryption.SHA1(loginPasswordTextView.getText().toString());
                    if (loginUsername.equals(user.getUsername()) && loginHashedPassword.equals(user.getMasterPassword())) {
                        authenticatedTime = System.currentTimeMillis();
                        masterPassword = loginHashedPassword;
                        keyGenPassword = Sha1Encryption.SHA1(Utils.toHex(loginPasswordTextView.getText().toString()));
                        dialog.dismiss();
                        deleteCredential();
                    } else {
                        authenticatedTime = 0L;
                        masterPassword = "";
                        keyGenPassword = "";
                        dialog.dismiss();
                    }
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException error) {
                    Log.e("CredentialsApp", error.toString());
                }
            }
        });
        dialog.show();
    }

    public List<String> generateKeys(String pwd) {
        List<String> kList = new ArrayList<>();
        try {
            kList.add(Sha1Encryption.SHA1(pwd.substring(0,13)).substring(5,21));
            kList.add(Sha1Encryption.SHA1(pwd.substring(13,26)).substring(21,37));
            kList.add(Sha1Encryption.SHA1(pwd.substring(26,39)).substring(0,16));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException error) {
            Log.e("CredentialsApp", error.toString());
        }
        return kList;
    }

    private void populateCredentialView() {
        serviceNameTextView.setText(credential.getServiceName());
        serviceUrlTextView.setText(credential.getServiceUrl());
        usernameTextView.setText(credential.getUsername());
        emailTextView.setText(credential.getEmail());
        passwordTextView.setText(R.string.hidden_password_text);
        noteTextView.setText(credential.getNote());
    }

    private String decryptPassword(String clearText, List<String> keys) {
        String decryptedText = "";
        try {
            decryptedText = dataEncryptionSystem.DecryptTripleDES(clearText, keys.get(0), keys.get(1), keys.get(2));
        } catch (IOException e) {
            Log.e("DataEncryptionSystem", e.getMessage());
        }
        return decryptedText;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CredentialActivity.this, MainActivity.class);
        intent.putExtra("authenticatedTime", String.valueOf(authenticatedTime));
        intent.putExtra("masterPassword", String.valueOf(masterPassword));
        intent.putExtra("keyGenPassword", String.valueOf(keyGenPassword));
        unregisterReceiver(connectivityMonitor);
        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
        finish();
    }

    private void editCredential() {
        Intent intent = new Intent(CredentialActivity.this, EditCredentialActivity.class);
        intent.putExtra("action", "editCredential");
        intent.putExtra("authenticatedTime", String.valueOf(authenticatedTime));
        intent.putExtra("masterPassword", String.valueOf(masterPassword));
        intent.putExtra("keyGenPassword", String.valueOf(keyGenPassword));
        intent.putExtra("credential", credential);
        unregisterReceiver(connectivityMonitor);
        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
    }

    private void deleteCredential() {
        Log.e("CredentialsApp", "deleteCredential");
        if (System.currentTimeMillis() - authenticatedTime < 30000) {
            Log.e("CredentialsApp", "Authenticated");
            credential.setActive(false);
            if (connectionAvailable) {
                Log.e("CredentialsApp", "Deleting credential on remote");
                postCredentialToAPI(credential.toJSON());
            }
            Log.e("CredentialsApp", "Deleting credential on local");
            database.credentialDAO().addCredential(credential);
            navigateToMainActivity();
        } else {
            Log.e("CredentialsApp", "Unauthenticated");
            loginToDeleteCredential();
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(CredentialActivity.this, MainActivity.class);
        intent.putExtra("authenticatedTime", String.valueOf(authenticatedTime));
        intent.putExtra("masterPassword", String.valueOf(masterPassword));
        intent.putExtra("keyGenPassword", String.valueOf(keyGenPassword));
        unregisterReceiver(connectivityMonitor);
        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
    }

    private Snackbar initializeConnectivitySnackBar() {
        Snackbar snackBar = Snackbar.make(findViewById(R.id.credential_page),
            "Connection unavailable", Snackbar.LENGTH_INDEFINITE);
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

            } else if (state.equals("unavailable") && !connectivitySnackBarIsDisplayed) {
                connectivitySnackBar.show();
                connectivitySnackBarIsDisplayed = true;
                connectionAvailable = false;
            }
        }
    };

    class SetUserAsync extends AsyncTask<Void, Void, List<User>> {

        @Override
        protected void onPostExecute(List<User> result) {
            if (result.size() == 1) {
                user = result.get(0);
            } else {
                Log.e("CredentialsApp", "Users found in DB should be 1 but is: " + result.size());
            }
        }
        @Override
        protected List<User> doInBackground(Void... params) {
            return database.userDAO().getAllUsers();
        }
    }

    class UpdateViewAsync extends AsyncTask<Integer, Void, List<Credential>> {

        @Override
        protected void onPostExecute(List<Credential> result) {
            if (result.size() == 1) {
                credential = result.get(0);
                populateCredentialView();
            } else {
                Log.e("CredentialsApp", "MainActivity:UpdateViewAsync, No information available.");
            }
        }
        @Override
        protected List<Credential> doInBackground(Integer... params) {
            return database.credentialDAO().getCredentialById(params[0]);
        }
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
                String credentials = user.getUsername() + ":" + user.getMasterPassword();
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
}
