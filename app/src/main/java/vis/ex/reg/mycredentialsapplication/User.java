package vis.ex.reg.mycredentialsapplication;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

@Entity
public class User {

    @PrimaryKey
    private int user_ID;
    private String username;
    private String email;
    private String masterPassword;
    private String dateLastModified;
    private String role;

    User() {}

    User(JSONObject jsonObj) {
        try {
            this.setUser_ID(Integer.valueOf(jsonObj.getString("userId")));
            this.setUsername(jsonObj.getString("username"));
            this.setEmail(jsonObj.getString("email"));
            this.setMasterPassword(jsonObj.getString("password"));
            this.setDateLastModified(jsonObj.getString("dateLastModified"));
            this.setRole(jsonObj.getString("role"));

        } catch(JSONException e) {
            Log.e("CredentialsApp", "Credential object constructor : " + e.getMessage());
        }
    }

    void setUser_ID(int user_ID) {
        this.user_ID = user_ID;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setEmail(String email) {
        this.email = email;
    }

    void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    void setDateLastModified(String dateLastModified) {
        this.dateLastModified = dateLastModified;
    }

    void setRole(String role) {
        this.role = role;
    }

    int getUser_ID() {
        return this.user_ID;
    }

    String getUsername() {
        return this.username;
    }

    String getEmail() {
        return this.email;
    }

    String getMasterPassword() {
        return this.masterPassword;
    }

    String getDateLastModified() {
        return this.dateLastModified;
    }

    String getRole() {
        return role;
    }

    JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", this.getUser_ID());
            jsonObject.put("username", this.getUsername());
            jsonObject.put("email", this.getEmail());
            jsonObject.put("masterPassword", this.getMasterPassword());
            jsonObject.put("dateLastModified", this.getDateLastModified());
            jsonObject.put("role", this.getRole());
        } catch (JSONException e) {
            Log.e("CredentialsApp", e.getMessage());
        }
        return jsonObject;
    }

    public String toString() {
        return "userId: " + this.getUser_ID()
            + ", username: " + this.getUsername()
            + ", email: " + this.getEmail()
            + ", masterPassword: " + this.getMasterPassword()
            + ", dateLastModified: " + this.getDateLastModified()
            + ", role: " + this.getRole();
    }
}
