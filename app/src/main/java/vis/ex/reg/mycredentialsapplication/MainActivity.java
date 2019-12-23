package vis.ex.reg.mycredentialsapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;


public class MainActivity extends AppCompatActivity implements MyAdapter.ItemClickListener {

    private List<CredentialDTO> credentialDTOList  = new ArrayList<>();
    private List<Credential> credentialList  = new ArrayList<>();
    private AppDatabase database;
    private RecyclerView recyclerView;
    private CheckConnectivity connectivityChecker;
    private boolean dataAvailable;
    private boolean snackBarDisplayed;
    private Snackbar snackBar;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.credentials_page);

        FloatingActionButton searchButton = findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();

                Intent intent = new Intent(MainActivity.this, AddCredentialActivity.class);
//                unregisterReceiver(this.connectivityChecker);
                unregisterReceiver(broadcastReceiver);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.credentials_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean internetConnection = (connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            .getState() == NetworkInfo.State.CONNECTED || connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            .getState() == NetworkInfo.State.CONNECTED);

//        this.registerReceiver(this.connectivityChecker, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        this.registerReceiver(broadcastReceiver, new IntentFilter("connection_change"));

        // get instance of database
        database = AppDatabase.getDatabase(getApplicationContext());

        // if connected get data from API, compare dateLastModified with date from local DB and
        // update relevant data. If not connected get data from DB.


        if (internetConnection) {
            getCredentialsFromAPI();
        } else {
            // Load date from local DB and display
            // UpdateViewAsync updateViewAsync = new UpdateViewAsync();
            // updateViewAsync.execute();
        }
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra("state");
        if (state.equals("available") && snackBarDisplayed) {
            snackBar.dismiss();
            snackBarDisplayed = false;
            getCredentialsFromAPI();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getCredentialsFromAPI() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getResources().getString(R.string.api_url) + "/credentials/";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray credentialArray = new JSONArray(response);
                        methodToHoldUntilResponseArrived(credentialArray);
                    } catch (java.lang.Throwable e) {
                        Log.e("CredentialsApp", "unexpected JSON exception", e);
                    }
                }

                private void methodToHoldUntilResponseArrived(JSONArray response){
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = (JSONObject) response.get(i);
                            credentialDTOList.add(new CredentialDTO(Integer.valueOf(obj.getString("id")),
                                                                obj.getString("serviceName"),
                                                                obj.getString("dateLastModified"),
                                                                Boolean.valueOf(obj.getString("active"))));
                        }
                    } catch(JSONException e) {
                        Log.e("CredentialsApp", "unexpected JSON exception", e);
                    }
                    UpdateDBAsync updateDBAsync = new UpdateDBAsync();
                    updateDBAsync.execute(credentialDTOList.toArray(new CredentialDTO[0]));
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CredentialsApp", "onErrorResponse()");
            }
        });
        queue.add(stringRequest);
    }



    @Override
    public void onItemClick(View view, int position) {
        if (dataAvailable) {
            Intent intent = new Intent(MainActivity.this, CredentialActivity.class);
            intent.putExtra("credential", credentialList.get(position).toJSON().toString());
//            unregisterReceiver(this.connectivityChecker);
            unregisterReceiver(broadcastReceiver);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    class UpdateDBAsync extends AsyncTask<CredentialDTO, Void, Integer> {

        @Override
        protected void onPostExecute(Integer result){
            if (result == 1) {
                UpdateViewAsync updateViewAsync = new UpdateViewAsync();
                updateViewAsync.execute();
            }
        }

        @Override
        protected Integer doInBackground(CredentialDTO... params) {
            List<Credential> localCredentialObjs = database.credentialDAO().getAllCredentials();
            List<Integer> remoteIds = new ArrayList<>();

            for(CredentialDTO credentialDTO : params) {
                Integer id = credentialDTO.Credential_ID;
                remoteIds.add(id);
                List<Credential> localCredentials = database.credentialDAO().getCredentialById(id);

                if (localCredentials.isEmpty()) {
                    updateLocalCredential(id); // add this credential to database
                } else {
                    Credential localCredential = localCredentials.get(0);

                    long remoteDateModified = Long.valueOf(credentialDTO.DateLastModified);
                    long localDateModified = Long.valueOf(localCredential.DateLastModified);

                    if (remoteDateModified > localDateModified) {
                        updateLocalCredential(id); // remote is newer
                    } else if (localDateModified > remoteDateModified) {
                        updateRemoteCredential(localCredential.toJSON()); // local is newer update remote
                    }
                }
            }
            for(Credential localCredential : localCredentialObjs) {
                Integer id = localCredential.Credential_ID;
                if (!remoteIds.contains(id)) {
                    updateRemoteCredential(localCredential.toJSON());
                }
            }
            return 1;
        }
    }


    class UpdateViewAsync extends AsyncTask<Void, Void, List<Credential>> {

        @Override
        protected void onPostExecute(List<Credential> result) {
            if (result.size() > 0) {
                credentialList = result;
                dataAvailable = true;
            } else {
                Log.e("CredentialsApp", "No information available.");
                dataAvailable = false;
            }
            populateRecyclerView(result);
        }
        @Override
        protected List<Credential> doInBackground(Void... params) {
            return database.credentialDAO().getAllCredentials();
        }
    }

    private void updateLocalCredential(Integer id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getResources().getString(R.string.api_url) + "/credentials/" + id;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        methodToHoldUntilResponseArrived(new JSONObject(response));
                    } catch (java.lang.Throwable e) {
                        Log.e("CredentialsApp", "unexpected JSON exception", e);
                    }
                }

                private void methodToHoldUntilResponseArrived(JSONObject obj){
                    try {
                        String serviceName = (obj.has("serviceName") ? obj.getString("serviceName") : "" );
                        String serviceUrl = (obj.has("serviceUrl") ? obj.getString("serviceUrl") : "" );
                        String username = (obj.has("username") ? obj.getString("username") : "" );
                        String email = (obj.has("email") ? obj.getString("email") : "" );
                        String password = (obj.has("password") ? obj.getString("password") : "" );
                        String note = (obj.has("note") ? obj.getString("note") : "" );

                        Credential updatedCredential = new Credential(
                            Integer.valueOf(obj.getString("id")), serviceName, serviceUrl, username, email,
                            password, obj.getString("dateLastModified"), note, obj.getBoolean("active"));

                        database.credentialDAO().addCredential(updatedCredential);
                    } catch(JSONException e) {
                        Log.e("CredentialsApp", "unexpected JSON exception", e);
                    }
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CredentialsApp", "getCredentialById : onErrorResponse()");
            }
        });
        queue.add(stringRequest);
    }

    private void updateRemoteCredential(JSONObject jsonObject) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getResources().getString(R.string.api_url) + "/credentials/";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("CredentialsApp", "Credential on Remote has been updated");
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CredentialsApp", error.getMessage());
            }
        });
        queue.add(request);
    }

    private void populateRecyclerView(List<Credential> list) {
        MyAdapter adapter = new MyAdapter(this, list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }
}
