package vis.ex.reg.mycredentialsapplication;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;


@Entity
public class CredentialDTO {

    @PrimaryKey
    private int credential_ID;
    private String serviceUrl;
    private String serviceName;
    private String username;
    private String email;
    private String encryptedPassword;
    private String dateLastModified;
    private String note;
//    private ArrayList<String> identifiers;
    private boolean active;


    public int getCredential_ID() {
        return this.credential_ID;
    }

    public String getServiceUrl() {
        return this.serviceUrl;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getEncryptedPassword() {
        return this.encryptedPassword;
    }

    public String getDateLastModified() {
        return this.dateLastModified;
    }

    public String getNote() {
        return this.note;
    }

//    public ArrayList<String> getIdentifiers() {
//        return this.identifiers;
//    }

    public boolean getActive() {
        return this.active;
    }

    public void setCredential_ID(int credential_ID) {
        this.credential_ID = credential_ID;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public void setDateLastModified(String dateLastModified) {
        this.dateLastModified = dateLastModified;
    }

    public void setNote(String note) {
        this.note = note;
    }

//    public void setIdentifiers(ArrayList<String> identifiers) {
//        this.identifiers = identifiers;
//    }
//
//    public void setIdentifiers(String commaSeparatedString) {
//        String[] elements = commaSeparatedString.split(",");
//        String[] trimmedElements = new String[elements.length];
//        for (int i = 0; i < elements.length; i++) {
//            trimmedElements[i] = elements[i].trim();
//        }
//        List<String> fixedLenghtList = Arrays.asList(trimmedElements);
//
//        this.identifiers = new ArrayList<>(fixedLenghtList);
//    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", this.credential_ID);
            jsonObject.put("serviceUrl", this.serviceUrl);
            jsonObject.put("serviceName", this.serviceName);
            jsonObject.put("username", this.username);
            jsonObject.put("email", this.email);
            jsonObject.put("encryptedPassword", this.encryptedPassword);
            jsonObject.put("dateLastModified", this.dateLastModified);
            jsonObject.put("note", this.note);
//            jsonObject.put("identifiers", new JSONArray(this.identifiers));
            jsonObject.put("active", this.active);
        } catch(JSONException e) {
            Log.e("CredentialsApp", e.getMessage());
        }
        return jsonObject;
    }
}
