package vis.ex.reg.mycredentialsapplication;

import android.util.Base64;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class MyStringRequest extends StringRequest {

    private User user;

    public MyStringRequest(int method, String url, Response.Listener<String> listener, User user) {
        super(
            method,
            url,
            listener,
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("CredentialsApp", "MainActivity:" + error.getMessage());
                }
            }
        );
        this.user = user;
    }

    @Override
    public Map<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        // TODO: potential bug here posting updated user using old user credentials
        String credentials = user.getUsername() + ":" + user.getMasterPassword();
        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headers.put("Authorization", auth);
        return headers;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        int statusCode = response.statusCode;
        Log.i("CredentialsApp", "Request status code: " + statusCode);
        return super.parseNetworkResponse(response);
    }
}
