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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CredentialActivity extends AppCompatActivity {

    private TextView serviceNameView;
    private TextView noteView;
    private TextView usernameView;
    private String credentialId;
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

        // Get the transferred data from source activity.
        Intent intent = getIntent();
        credentialId = intent.getStringExtra("credentialId");

//        serviceName = findViewById(R.id.serviceName);
//        note = findViewById(R.id.note);
//        username = findViewById(R.id.username);

        database = AppDatabase.getDatabase(getApplicationContext());

        if (internetConnection) {
            getCredentialFromAPI();
        } else {
            UpdateViewAsync updateViewAsync = new UpdateViewAsync();
            updateViewAsync.execute(Integer.valueOf(credentialId));
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("state");
            if (state.equals("available") && snackBarDisplayed) {
                snackBar.dismiss();
                snackBarDisplayed = false;
                getCredentialFromAPI();
            } else if (state.equals("unavailable") && !snackBarDisplayed) {
                snackBar = Snackbar.make(coordinatorLayout, "Connection Lost", Snackbar.LENGTH_INDEFINITE);
                View sbView = snackBar.getView();
                sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbarBackgroundColor));
                snackBar.show();
                snackBarDisplayed = true;
            }
        }
    };

    private void getCredentialFromAPI() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getResources().getString(R.string.api_url) + "/credentials/" + credentialId;
        Log.d("CredentialsApp", url);

        StringRequest getRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("CredentialsApp", "onResponse()");
                try {
                    Log.d("CredentialsApp", "try");
                    JSONObject credential = new JSONObject(response);
                    methodToHoldUntilResponseArrived(credential);
                } catch (java.lang.Throwable e) {
                    Log.e("CredentialsApp", "unexpected JSON exception", e);
                }
            }

            private void methodToHoldUntilResponseArrived(JSONObject response) {
                Log.d("CredentialsApp", "methodToHoldUntilResponseArrived()");

//                        JSONObject obj = (JSONObject) response.get(0);
                Log.d("CredentialsApp", response.toString());
                Log.d("CredentialsApp", response.getClass().getName());
                try {
                    String serviceName = response.getString("serviceName");
                    String serviceUrl = response.getString("serviceUrl");
                    String username = response.getString("username");
                    String email = response.getString("email");
                    String encodedPassword = response.getString("encodedPassword");
                    String dateLastModified = response.getString("dateLastModified");
                    String note = response.getString("note");
                    String active = response.getString("active");

                    UpdateDBAsync updateDBAsync = new UpdateDBAsync();
                    updateDBAsync.execute(String.valueOf(credentialId), dateLastModified, serviceName,
                                            serviceUrl, username, email, encodedPassword, note, active);

                } catch (JSONException e) {
                    Log.e("MYAPP", "unexpected JSON exception", e);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error.Response", error.toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
//                params.put("Book_ID", bookId);
                return params;
            }
        };
        // add it to the RequestQueue
        queue.add(getRequest);
    }

//    private void populateBookView(Book book) {
//        authorId = book.Author_ID;
//        bookTitle.setText(book.BookName);
//        if (book.Info == null) {
//            bookInfoType.setText(getResources().getString(R.string.not_avail));
//            bookDescription.setText("");
//        } else {
//            bookInfoType.setText(book.InfoType);
//            bookDescription.setText(book.Info);
//        }
//    }

//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent(BookActivity.this, bookListActivity.class);
//        intent.putExtra("authorId", String.valueOf(authorId));
//        unregisterReceiver(this.connectivityChecker);
//        unregisterReceiver(broadcastReceiver);
//        startActivity(intent);
//        finish();
//    }

    class UpdateDBAsync extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPostExecute(Integer result){
            if (result != -1) {
                UpdateViewAsync updateViewAsync = new UpdateViewAsync();
                updateViewAsync.execute(result);
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            List<Credential> credentialList = database.credentialDAO().getCredentialById(Integer.valueOf(params[0]));
//            if (credentialList.size() > 0 && params[1]) {

//                Book book = bookList.get(0);
//                String infoType = params[1];
//                String info = params[2];
//                if (book.InfoType == null || book.Info == null) {
//                    book.InfoType = infoType;
//                    book.Info = info;
//                    database.bookDAO().updateBook(book);
//                }
//                return Integer.valueOf(params[0]);
//            } else {
                return -1;
//            }
        }
    }

    class UpdateViewAsync extends AsyncTask<Integer, Void, List<Credential>>{
        @Override
        protected void onPostExecute(List<Credential> result){
            if (result.size() < 1) {
                result.add(new Credential(-1, "x",
                    "Oops, No book found in database",
                    "",false));
            }
//            populateBookView(result.get(0));
        }
        @Override
        protected List<Credential> doInBackground(Integer... params) {
            return database.credentialDAO().getCredentialById(params[0]);
        }
    }
}
