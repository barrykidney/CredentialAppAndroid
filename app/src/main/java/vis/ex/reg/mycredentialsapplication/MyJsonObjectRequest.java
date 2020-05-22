package vis.ex.reg.mycredentialsapplication;

import android.util.Base64;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyJsonObjectRequest extends JsonObjectRequest {

    private User user;

    public MyJsonObjectRequest(int method, String url, JSONObject data, Response.Listener<JSONObject> listener, User user) {
        super(
            method,
            url,
            data,
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
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        int statusCode = response.statusCode;
        Log.e("CredentialsApp", "postUserToAPI : Request status code: " + statusCode);
        return super.parseNetworkResponse(response);
    }
}
