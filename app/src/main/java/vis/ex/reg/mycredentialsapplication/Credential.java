package vis.ex.reg.mycredentialsapplication;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


@Entity
public class Credential {

    @PrimaryKey
    public int Credential_ID;
    public String ServiceName;
    public String ServiceUrl;
    public String Username;
    public String Email;
    public String EncodedPassword;
    public String DateLastModified;
    public String Note;
    public boolean Active;

    public Credential(int Credential_ID, String ServiceName, String ServiceUrl, String Username, String Email,
                      String EncodedPassword, String DateLastModified, String Note, boolean Active) {
        this.Credential_ID = Credential_ID;
        this.ServiceName = ServiceName;
        this.ServiceUrl = ServiceUrl;
        this.Username = Username;
        this.Email = Email;
        this.EncodedPassword = EncodedPassword;
        this.DateLastModified = DateLastModified;
        this.Note = Note;
        this.Active = Active;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", this.Credential_ID);
            jsonObject.put("serviceName", this.ServiceName);
            jsonObject.put("serviceUrl", this.ServiceUrl);
            jsonObject.put("username", this.Username);
            jsonObject.put("email", this.Email);
            jsonObject.put("encodedPassword", this.EncodedPassword);
            jsonObject.put("dateLastModified", this.DateLastModified);
            jsonObject.put("note", this.Note);
            jsonObject.put("active", this.Active);
        } catch(JSONException e) {
            Log.e("CredentialsApp", e.getMessage());
        }
        return jsonObject;
    }
}
