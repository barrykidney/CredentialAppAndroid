package vis.ex.reg.mycredentialsapplication;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


@Entity
public class CredentialSummaryDTO {

    @PrimaryKey
    private int credential_ID;
    private String serviceName;
    private String dateLastModified;
    private String note;
    private boolean active;

    CredentialSummaryDTO(int credential_ID, String serviceName, String dateLastModified, String note, boolean active) {
        this.setCredential_ID(credential_ID);
        this.setServiceName(serviceName);
        this.setDateLastModified(dateLastModified);
        this.setNote(note);
        this.setActive(active);
    }

    CredentialSummaryDTO(Credential credentialObj) {
        this.setCredential_ID(credentialObj.getCredential_ID());
        this.setServiceName(credentialObj.getServiceName());
        this.setDateLastModified(credentialObj.getDateLastModified());
        this.setNote(credentialObj.getNote());
        this.setActive(credentialObj.getActive());
    }

    CredentialSummaryDTO(JSONObject obj) {
        try {
            this.setCredential_ID(Integer.valueOf(obj.getString("id")));
            this.setServiceName(obj.getString("serviceName"));
            this.setDateLastModified(obj.getString("dateLastModified"));
            this.setNote(obj.getString("note"));
            this.setActive(Boolean.valueOf(obj.getString("active")));
        } catch (JSONException e) {
            Log.e("CredentialsApp", e.toString());
        }
    }

    // Getters
    public int getCredential_ID() {
        return this.credential_ID;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getDateLastModified() {
        return this.dateLastModified;
    }

    public String getNote() {
        return this.note;
    }

    public boolean getActive() {
        return this.active;
    }

    // Setters
    public void setCredential_ID(int credential_ID) {
        this.credential_ID = credential_ID;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setDateLastModified(String dateLastModified) {
        this.dateLastModified = dateLastModified;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", this.getCredential_ID());
            jsonObject.put("serviceName", this.getServiceName());
            jsonObject.put("dateLastModified", this.getDateLastModified());
            jsonObject.put("note", this.getNote());
            jsonObject.put("active", this.getActive());
        } catch(JSONException e) {
            Log.e("CredentialsApp", e.toString());
        }
        return jsonObject;
    }
}
