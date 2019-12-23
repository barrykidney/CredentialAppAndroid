package vis.ex.reg.mycredentialsapplication;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


@Entity
public class CredentialDTO {

    @PrimaryKey
    public int Credential_ID;
    public String ServiceName;
    public String DateLastModified;
    public boolean Active;

    public CredentialDTO(int Credential_ID, String ServiceName,
                         String DateLastModified, boolean Active) {
        this.Credential_ID = Credential_ID;
        this.ServiceName = ServiceName;
        this.DateLastModified = DateLastModified;
        this.Active = Active;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", this.Credential_ID);
            jsonObject.put("serviceName", this.ServiceName);
            jsonObject.put("dateLastModified", this.DateLastModified);
            jsonObject.put("active", this.Active);
        } catch(JSONException e) {
            Log.e("CredentialsApp", e.getMessage());
        }
        return jsonObject;
    }
}

