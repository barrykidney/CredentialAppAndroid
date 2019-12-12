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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;


public class MainActivity extends AppCompatActivity implements MyAdapter.ItemClickListener {

    private List<Credential> credentialList  = new ArrayList<>();
    private AppDatabase database;
    private RecyclerView recyclerView;
//    private CheckConnectivity connectivityChecker;
    private boolean dataAvailable;
    private boolean snackBarDisplayed;
    private Snackbar snackBar;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate: ", "1");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.credentials_page);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Log.d("onCreate: ", "2");
        recyclerView = findViewById(R.id.credentials_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d("onCreate: ", "3");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean internetConnection = (connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            .getState() == NetworkInfo.State.CONNECTED || connectivityManager
            .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            .getState() == NetworkInfo.State.CONNECTED);
        Log.d("onCreate: ", "4");
//        this.registerReceiver(this.connectivityChecker, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//        this.registerReceiver(broadcastReceiver, new IntentFilter("connection_change"));
//
//        // Get the transferred data from source activity.
//        Intent intent = getIntent();
//        bookId = intent.getStringExtra("bookId");
//
//        bookTitle = findViewById(R.id.bookTitle);
//        bookDescription = findViewById(R.id.bookDescription);
//        bookInfoType = findViewById(R.id.bookInfoType);
        Log.d("onCreate: ", "5");
        // get instance of database
        database = AppDatabase.getDatabase(getApplicationContext());

        // if connected get data from API and save to DB.
        // if not connected get data from DB.
        UpdateViewAsync updateViewAsync = new UpdateViewAsync();
        updateViewAsync.execute();

        if (internetConnection) {
            getCredentialsFromAPI();
        } else {
            Log.e("onCreate: ", "no connection");
//            UpdateViewAsync updateViewAsync = new UpdateViewAsync();
//            updateViewAsync.execute();
        }
        Log.d("onCreate: ", "6");
    }


//    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//        String state = intent.getStringExtra("state");
//        if (state.equals("available") && snackBarDisplayed) {
//            snackBar.dismiss();
//            snackBarDisplayed = false;
//            getCredentialsFromAPI();
//        } else if (state.equals("unavailable") && !snackBarDisplayed) {
//            snackBar = Snackbar.make(coordinatorLayout, "Connection Lost", Snackbar.LENGTH_INDEFINITE);
//            View sbView = snackBar.getView();
//            sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbarBackgroundColor));
//            snackBar.show();
//            snackBarDisplayed = true;
//        }
//        }
//    };


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

        // Request a string response from the provided URL.
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
//                        JSONArray authors = response.getJSONArray("authors");
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = (JSONObject) response.get(i);

                            credentialList.add(new Credential(Integer.valueOf(obj.getString("id")),
                                                                obj.getString("serviceName"),
                                                                obj.getString("serviceUrl"),
                                                                obj.getString("username"),
                                                                obj.getString("email"),
                                                                obj.getString("encodedPassword"),
                                                                obj.getString("dateLastModified"),
                                                                obj.getString("note"),
                                                                Boolean.valueOf(obj.getString("active"))));
                        }
                    } catch(JSONException e) {
                        Log.e("CredentialsApp", "unexpected JSON exception", e);
                    }
                    UpdateDBAsync updateDBAsync = new UpdateDBAsync();
                    updateDBAsync.execute(credentialList.toArray(new Credential[0]));
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("CredentialsApp", "onErrorResponse()");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void populateRecyclerView(List<Credential> list) {
        MyAdapter adapter = new MyAdapter(this, list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (dataAvailable) {
//            Log.d("CredentialsApp", "onItemClick()");
            Intent intent = new Intent(MainActivity.this, CredentialActivity.class);
//            Log.d("CredentialsApp", credentialList.get(position).toJSON().toString());
//            Log.d("CredentialsApp", "onItemClick()");
            intent.putExtra("credential", credentialList.get(position).toJSON().toString());
//            unregisterReceiver(this.connectivityChecker);
//            unregisterReceiver(broadcastReceiver);
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

    class UpdateDBAsync extends AsyncTask<Credential, Void, Integer> {

        @Override
        protected void onPostExecute(Integer result){
            Log.d("CredentialsApp", "UpdateDBAsync : onPostExecute()");
            if (result == 1) {
                UpdateViewAsync updateViewAsync = new UpdateViewAsync();
                updateViewAsync.execute();
            }
        }

        @Override
        protected Integer doInBackground(Credential... params) {
            for(Credential a : params) {
                database.credentialDAO().addCredential(a);
            }
            return 1;
        }
    }


    class UpdateViewAsync extends AsyncTask<Void, Void, List<Credential>> {

        @Override
        protected void onPostExecute(List<Credential> result) {
            Log.d("CredentialsApp", "UpdateViewAsync : onPostExecute()");
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
}
