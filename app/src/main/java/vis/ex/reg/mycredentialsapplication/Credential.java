package vis.ex.reg.mycredentialsapplication;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;


@Entity
class Credential implements Serializable {

    @PrimaryKey
    private int credential_ID;
    private String serviceName;
    private String serviceUrl;
    private String username;
    private String email;
    private String encryptedPassword;
    private String dateLastModified;
    private String note;
    private boolean active;

    Credential() {}

    Credential(JSONObject jsonObj) {
        try {
            this.setCredential_ID(Integer.valueOf(jsonObj.getString("id")));
            this.setServiceUrl(jsonObj.getString("serviceUrl"));
            this.setServiceName(jsonObj.getString("serviceName"));
            this.setUsername(jsonObj.getString("username"));
            this.setEmail(jsonObj.getString("email"));
            this.setEncryptedPassword(jsonObj.getString("encryptedPassword"));
            this.setDateLastModified(jsonObj.getString("dateLastModified"));
            this.setNote(jsonObj.getString("note"));
            this.setActive(jsonObj.getBoolean("active"));

        } catch(JSONException e) {
            Log.e("CredentialsApp", "Credential object constructor : " + e.getMessage());
        }
    }

    void setCredential_ID(int credential_ID) {
        this.credential_ID = credential_ID;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setEmail(String email) {
        this.email = email;
    }

    void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    void setDateLastModified(String dateLastModified) {
        this.dateLastModified = dateLastModified;
    }

    void setNote(String note) {
        this.note = note;
    }

    void setActive(boolean active) {
        this.active = active;
    }

    int getCredential_ID() {
        return this.credential_ID;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    String getServiceUrl() {
        return this.serviceUrl;
    }

    String getUsername() {
        return this.username;
    }

    String getEmail() {
        return this.email;
    }

    String getEncryptedPassword() {
        return this.encryptedPassword;
    }

    String getDateLastModified() {
        return this.dateLastModified;
    }

    String getNote() {
        return note;
    }

    boolean getActive() {
        return this.active;
    }

    JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", this.getCredential_ID());
            jsonObject.put("serviceName", this.getServiceName());
            jsonObject.put("serviceUrl", this.getServiceUrl());
            jsonObject.put("username", this.getUsername());
            jsonObject.put("email", this.getEmail());
            jsonObject.put("encryptedPassword", this.getEncryptedPassword());
            jsonObject.put("dateLastModified", this.getDateLastModified());
            jsonObject.put("note", this.getNote());
            jsonObject.put("active", this.getActive());
        } catch (JSONException e) {
            Log.e("CredentialsApp", e.getMessage());
        }
        return jsonObject;
    }
}
