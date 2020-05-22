package vis.ex.reg.mycredentialsapplication;

import android.app.Dialog;
import android.app.SearchManager;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements MyAdapter.ItemClickListener {

    private List<CredentialSummaryDTO> allCredentials = new ArrayList<>();
    private List<CredentialSummaryDTO> credentialsToBeDisplayed = new ArrayList<>();
    private CheckConnectivity connectivityMonitor;
    private AppDatabase database;
    private User user;
    private RecyclerView recyclerView;
    private Snackbar connectivitySnackBar;
    private int highestCredentialIndex = 0;
    private boolean connectivitySnackBarIsDisplayed;
    private boolean reducedList = false;
    private boolean connectionAvailable;
    private RequestQueue queue;
    private long authenticatedTime;
    private EditText loginUsernameTextView;
    private EditText loginPasswordTextView;
    private String masterPassword = "";
    private String keyGenPassword = "";
    ImageView closeBtn;
    Button authBtn;
    MyAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        queue = VolleyController.getInstance(this.getApplicationContext()).getRequestQueue();
        database = AppDatabase.getDatabase(getApplicationContext());

        recyclerView = findViewById(R.id.credentials_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        connectivitySnackBar = initializeConnectivitySnackBar();
        initializeConnectivityMonitor();
        connectionAvailable = internetConnectionIsAvailable();

        authenticatedTime = 0L;
        Intent intent = getIntent();
        if (intent.hasExtra("authenticatedTime") && intent.hasExtra("masterPassword")) {
            authenticatedTime = Long.valueOf(intent.getStringExtra("authenticatedTime"));
            masterPassword = intent.getStringExtra("masterPassword");
            keyGenPassword = intent.getStringExtra("keyGenPassword");
        }

        FloatingActionButton addCredentialButton = findViewById(R.id.addCredential);
        addCredentialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditCredentialActivity.class);
                intent.putExtra("action", "addCredential");
                if (authenticatedTime == 0 || (System.currentTimeMillis() - authenticatedTime) > 30000) {
                    authenticatedTime = 0L;
                    masterPassword = "";
                    keyGenPassword = "";
                }
                intent.putExtra("authenticatedTime", String.valueOf(authenticatedTime));
                intent.putExtra("masterPassword", masterPassword);
                intent.putExtra("keyGenPassword", keyGenPassword);
                intent.putExtra("highestCredentialIndex", String.valueOf(highestCredentialIndex));
                unregisterReceiver(connectivityMonitor);
                unregisterReceiver(broadcastReceiver);
                startActivity(intent);
            }
        });

        populateRecyclerView(credentialsToBeDisplayed);
        new GetUserFromLocal().execute();
    }

    private void populateRecyclerView(List<CredentialSummaryDTO> list) {
        adapter = new MyAdapter(this, list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            credentialsToBeDisplayed.clear();
            credentialsToBeDisplayed = searchCredentials(query);
            reducedList = true;
            adapter.setNewData(credentialsToBeDisplayed);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater settingsInflater = getMenuInflater();
        settingsInflater.inflate(R.menu.menu_main, menu);

        MenuInflater searchInflater = getMenuInflater();
        searchInflater.inflate(R.menu.options_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.e("CredentialsApp", "Settings selected");
//                navigateToSettingsUserActivity();
                return true;
            case R.id.action_profile:
                Log.e("CredentialsApp", "Profile selected");
                navigateToProfileUserActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void navigateToProfileUserActivity() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        // intent.putExtra("user", String.valueOf(user.getUser_ID()));
        if (authenticatedTime == 0 || (System.currentTimeMillis() - authenticatedTime) > 30000) {
            authenticatedTime = 0L;
            masterPassword = "";
        }
        intent.putExtra("authenticatedTime", String.valueOf(authenticatedTime));
        intent.putExtra("masterPassword", String.valueOf(masterPassword));
        unregisterReceiver(connectivityMonitor);
        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
    }

    class GetUserFromLocal extends AsyncTask<Void, Void, List<User>> {

        @Override
        protected void onPostExecute(List<User> result) {
            if (result.size() >= 1) {
                user = result.get(0);
                if (connectionAvailable) {
                    getUserFromRemote(user.getUsername(), user.getMasterPassword());
                }
                new GetAllCredentialsFromLocal().execute();
            } else {
                if (connectionAvailable) {
                    login();
                }
            }
        }

        @Override
        protected List<User> doInBackground(Void... params) {
            return database.userDAO().getAllUsers();
        }
    }

    private void getAllCredentialsFromRemote() {
        String url = getResources().getString(R.string.api_url) + "/credentials/user/" + user.getUser_ID();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray credentialArray = new JSONArray(response);
                    methodToHoldUntilResponseArrived(credentialArray);
                } catch (java.lang.Throwable e) {
                    Log.e("CredentialsApp", "MainActivity:getAllCredentialsFromRemote, " + e.getMessage());
                }
            }

            private void methodToHoldUntilResponseArrived(JSONArray response) {
                List<CredentialSummaryDTO> allRemoteCredentials = new ArrayList<>();
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jsonObj = (JSONObject) response.get(i);
                        allRemoteCredentials.add(new CredentialSummaryDTO(jsonObj));
                    }
                } catch (JSONException e) {
                    Log.e("CredentialsApp", "MainActivity:getAllCredentialsFromRemote, " + e.toString());
                }
                new SynchronizeCredentials().execute(allRemoteCredentials.toArray(new CredentialSummaryDTO[0]));
            }
        };

        StringRequest stringRequest = new MyStringRequest(Request.Method.GET, url, responseListener, user);
        queue.add(stringRequest);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(MainActivity.this, CredentialActivity.class);
        int credentialId = credentialsToBeDisplayed.get(position).getCredential_ID(); //
        intent.putExtra("credential", String.valueOf(credentialId));
        if (authenticatedTime == 0 || (System.currentTimeMillis() - authenticatedTime) > 30000) {
            authenticatedTime = 0L;
            masterPassword = "";
        }
        intent.putExtra("authenticatedTime", String.valueOf(authenticatedTime));
        intent.putExtra("masterPassword", masterPassword);
        intent.putExtra("keyGenPassword", keyGenPassword);
        unregisterReceiver(connectivityMonitor);
        unregisterReceiver(broadcastReceiver);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (reducedList) {
            credentialsToBeDisplayed.clear();
            credentialsToBeDisplayed.addAll(allCredentials);
            reducedList = false;
            adapter.setNewData(credentialsToBeDisplayed);
        } else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }

    private List<CredentialSummaryDTO> searchCredentials(String searchString) {
        List<CredentialSummaryDTO> searchResults = new ArrayList<>();

        for (CredentialSummaryDTO credentialSummary : allCredentials) {
            String serviceName = credentialSummary.getServiceName().toLowerCase();
            String note = credentialSummary.getNote().toLowerCase();
            if (serviceName.contains(searchString.toLowerCase()) | note.contains(searchString.toLowerCase())) {
                searchResults.add(credentialSummary);
            }
        }
        return searchResults;
    }

    private Snackbar initializeConnectivitySnackBar() {
        Snackbar snackBar = Snackbar.make(findViewById(R.id.credentials_page), "Connection unavailable", Snackbar.LENGTH_INDEFINITE);
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
                getAllCredentialsFromRemote(); ////////////////////////////////////////////////////////////// check this shouldn't be from local

            } else if (state.equals("unavailable") && !connectivitySnackBarIsDisplayed) {
                connectivitySnackBar.show();
                connectivitySnackBarIsDisplayed = true;
                connectionAvailable = false;
            }
        }
    };

    private ArrayList<Integer> createIdListFromSparseArray(SparseArray<CredentialSummaryDTO> list) {
        ArrayList<Integer> idList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            idList.add(list.keyAt(i));
        }
        return idList;
    }

    private SparseArray<CredentialSummaryDTO> createSparseArrayFromArrayList(List<CredentialSummaryDTO> list) {
        SparseArray<CredentialSummaryDTO> sa = new SparseArray<>();
        for (CredentialSummaryDTO c : list) {
            sa.put(c.getCredential_ID(), c);
        }
        return sa;
    }

    private List<Integer> getIntersectionOfAB(List<Integer> a, List<Integer> b) {
        ArrayList<Integer> c = new ArrayList<>(a);
        c.retainAll(b);
        return c;
    }

    private List<Integer> getExclusiveToA(List<Integer> a, List<Integer> b) {
        List<Integer> c = new ArrayList<>(a);
        c.removeAll(b);
        return c;
    }

    private void addAbsentCredentialsToLocalDb(List<Integer> indxList) {
        for (Integer id : indxList) {
            getRemoteCredAndAddToLocalDb(id);
        }
    }

    private void addAbsentCredentialsToRemoteDb(List<Integer> indxList) {
        for (Integer id : indxList) {
            new GetLocalCredentialAsync().execute(id);
        }
    }

    private void addCredentialToDisplayList(List<Integer> indxList, SparseArray<CredentialSummaryDTO> arr) {
        for (int id : indxList) {
            String newCredServiceName = arr.get(id).getServiceName().toLowerCase();
            if (credentialsToBeDisplayed.size() == 0) {
                credentialsToBeDisplayed.add(arr.get(id));
            } else {
                for (int i = 0; i < credentialsToBeDisplayed.size(); i++) {
                    if (credentialsToBeDisplayed.get(i).getServiceName().toLowerCase().compareTo(newCredServiceName) > 0) {
                        credentialsToBeDisplayed.add(i, arr.get(id));
                        break;
                    } else if (i == credentialsToBeDisplayed.size() - 1) {
                        credentialsToBeDisplayed.add(arr.get(id));
                        break;
                    }
                }
            }
        }
    }

    private void substituteCredentialInDisplayList(CredentialSummaryDTO cred) {
        int id = cred.getCredential_ID();
        for (int i = 0; i < credentialsToBeDisplayed.size(); i++) {
            if (credentialsToBeDisplayed.get(i).getCredential_ID() == id) {
                credentialsToBeDisplayed.set(i, cred);
            }
        }
    }

    private void syncIntersectingCredentials(List<Integer> indxList, SparseArray<CredentialSummaryDTO> localArr,
                                             SparseArray<CredentialSummaryDTO> remoteArr) {
        for (Integer id : indxList) {
            long localDateModified = Long.valueOf(localArr.get(id).getDateLastModified());
            long remoteDateModified = Long.valueOf(remoteArr.get(id).getDateLastModified());

            if (localDateModified < remoteDateModified) {
                getRemoteCredAndAddToLocalDb(id);
                substituteCredentialInDisplayList(remoteArr.get(id));
            } else if (localDateModified > remoteDateModified) {
                new GetLocalCredentialAsync().execute(id);
            }
        }
    }

    private void setHighestCredential(List<CredentialSummaryDTO> creds) {
        for (CredentialSummaryDTO cred : creds) {
            Integer id = cred.getCredential_ID();
            if (id > highestCredentialIndex) {
                highestCredentialIndex = id;
            }
        }
    }

    class GetAllCredentialsFromLocal extends AsyncTask<Integer, Void, List<CredentialSummaryDTO>> {

        @Override
        protected void onPostExecute(List<CredentialSummaryDTO> result) {
            credentialsToBeDisplayed.clear();
            if (result.size() > 0) {
                allCredentials = result;
                for (CredentialSummaryDTO cred : allCredentials) {
                    if (cred.getActive()) {
                        credentialsToBeDisplayed.add(cred);
                    }
                }
            } else {
                Log.e("CredentialsApp", "No credentials found in local");
            }

            adapter.setNewData(credentialsToBeDisplayed);

            if (connectionAvailable) {
                getAllCredentialsFromRemote();
            }
        }

        @Override
        protected List<CredentialSummaryDTO> doInBackground(Integer... params) {
            return database.credentialDAO().getAllCredentialsSummary();
        }
    }

    class SynchronizeCredentials extends AsyncTask<CredentialSummaryDTO, Void, Integer> {

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                adapter.setNewData(credentialsToBeDisplayed);
            }
        }

        @Override
        protected Integer doInBackground(CredentialSummaryDTO... params) {
            SparseArray<CredentialSummaryDTO> remoteArray = new SparseArray<>();
            for (CredentialSummaryDTO c : params) {
                remoteArray.put(c.getCredential_ID(), c);
            }
            SparseArray<CredentialSummaryDTO> localArray = createSparseArrayFromArrayList(allCredentials);

            List<Integer> remoteIds = createIdListFromSparseArray(remoteArray);
            List<Integer> localIds = createIdListFromSparseArray(localArray);
            addAbsentCredentialsToLocalDb(getExclusiveToA(remoteIds, localIds));
            addAbsentCredentialsToRemoteDb(getExclusiveToA(localIds, remoteIds));
            addCredentialToDisplayList(getExclusiveToA(remoteIds, localIds), remoteArray);

            syncIntersectingCredentials(getIntersectionOfAB(remoteIds, localIds), localArray, remoteArray);
            // TODO: try to do this without a call to the database
            setHighestCredential(database.credentialDAO().getAllCredentialsSummary());

            return 0; // maybe return the synced credential list and the get the highest index
        }
    }

    private void postCredentialToAPI(Credential credential) {
        String url = getResources().getString(R.string.api_url) + "/credentials/";
        int method = Request.Method.POST;

        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("CredentialsApp", "Post credential to API response: " + response);
            }
        };

        JsonObjectRequest request = new MyJsonObjectRequest(method, url, credential.toJSON(), responseListener, user) {

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
                Log.d("CredentialsApp", "Post credential to API status code: " + statusCode);
                return super.parseNetworkResponse(response);
            }
        };
        queue.add(request);
    }

    private void getRemoteCredAndAddToLocalDb(Integer id) {
        String url = getResources().getString(R.string.api_url) + "/credentials/" + id;

        Response.Listener<String> responseListener = new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject JSONobj = new JSONObject(response);
                    methodToHoldUntilResponseArrived(new Credential(JSONobj));
                } catch (java.lang.Throwable e) {
                    Log.e("CredentialsApp", "MainActivity:updateLocalCredential, " + e.toString());
                }
            }

            private void methodToHoldUntilResponseArrived(Credential updatedCredential) {
                new SaveCredentialToLocalDBAsync().execute(updatedCredential);
            }
        };

        StringRequest stringRequest = new MyStringRequest(Request.Method.GET, url, responseListener, user);
        queue.add(stringRequest);
    }

    class SaveCredentialToLocalDBAsync extends AsyncTask<Credential, Void, Integer> {

        @Override
        protected Integer doInBackground(Credential... params) {
            database.credentialDAO().addCredential(params[0]);
            return 1;
        }
    }

    class GetLocalCredentialAsync extends AsyncTask<Integer, Void, List<Credential>> {

        @Override
        protected void onPostExecute(List<Credential> result) {
            postCredentialToAPI(result.get(0));
        }

        @Override
        protected List<Credential> doInBackground(Integer... params) {
            return database.credentialDAO().getCredentialById(params[0]);
        }
    }

    private void login() {
        // TODO: if no connection available inform user
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
                    Log.e("CredentialsApp", "595 loginHashedPassword: " + loginHashedPassword);
                    getUserFromRemote(loginUsername, loginHashedPassword);
                    dialog.dismiss();
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException error) {
                    Log.e("CredentialsApp", error.toString());
                }
            }
        });
        dialog.show();
    }

    private void getUserFromRemote(final String username, final String hashedPassword) {
        String url = getResources().getString(R.string.api_url) + "/user/username/" + username;

        User tempUser = new User();
        tempUser.setUsername(username);
        tempUser.setMasterPassword(hashedPassword);
        Response.Listener<String> responseListener = new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject JSONobj = new JSONObject(response);
                    methodToHoldUntilResponseArrived(new User(JSONobj));
                } catch (java.lang.Throwable e) {
                    Log.e("CredentialsApp", "MainActivity:updateLocalCredential, " + e.toString());
                }
            }

            private void methodToHoldUntilResponseArrived(User userFromRemote) {
                user = userFromRemote;
                masterPassword = user.getMasterPassword();
                new SyncUser().execute(userFromRemote);
                new GetAllCredentialsFromLocal().execute();
            }
        };

        StringRequest stringRequest = new MyStringRequest(Request.Method.GET, url, responseListener, tempUser);
        queue.add(stringRequest);
    }

    // TODO: Make static, pass in a list of 2 users local and remote
    class SyncUser extends AsyncTask<User, Void, Integer> {

        @Override
        protected Integer doInBackground(User... params) {
            User userFromRemoteDb = params[0];

            long localDateModified = Long.valueOf(user.getDateLastModified());
            long remoteDateModified = Long.valueOf(userFromRemoteDb.getDateLastModified());

            // both dates are the same if no change occurred (unnecessary saves here) or no user in local (necessary saves here)
            if (localDateModified <= remoteDateModified) {
                database.userDAO().addUser(userFromRemoteDb);
            } else {
                postUserToAPI(user);
            }
            return 0; // maybe return the synced credential list and the get the highest index
        }
    }

    private void postUserToAPI(User localUser) {
        String url = getResources().getString(R.string.api_url) + "/user/";
        int method = Request.Method.POST;

        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("CredentialsApp", "Request sent" + response);
            }
        };

        JsonObjectRequest request = new MyJsonObjectRequest(method, url, localUser.toJSON(), responseListener, user);
        queue.add(request);
    }
}
