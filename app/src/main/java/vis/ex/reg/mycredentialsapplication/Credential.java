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

    public Credential() {}

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

    public static class CredentialBuilder {

        private int nestedCredential_ID;
        private String nestedServiceName;
        private String nestedServiceUrl;
        private String nestedUsername;
        private String nestedEmail;
        private String nestedEncodedPassword;
        private String nestedDateLastModified;
        private String nestedNote;
        private boolean nestedActive;

        public CredentialBuilder(final int Credential_ID) {
            this.nestedCredential_ID = Credential_ID;
        }

        public CredentialBuilder ServiceName(String ServiceName) {
            this.nestedServiceName = ServiceName;
            return this;
        }

        public CredentialBuilder ServiceUrl(String ServiceUrl) {
            this.nestedServiceUrl = ServiceUrl;
            return this;
        }

        public CredentialBuilder Username(String Username) {
            this.nestedUsername = Username;
            return this;
        }

        public CredentialBuilder Email(String Email) {
            this.nestedEmail = Email;
            return this;
        }

        public CredentialBuilder EncodedPassword(String EncodedPassword) {
            this.nestedEncodedPassword = EncodedPassword;
            return this;
        }

        public CredentialBuilder DateLastModified(String DateLastModified) {
            this.nestedDateLastModified = DateLastModified;
            return this;
        }

        public CredentialBuilder Note(String Note) {
            this.nestedNote = Note;
            return this;
        }

        public CredentialBuilder Active(boolean Active) {
            this.nestedActive = Active;
            return this;
        }

        public Credential createCredential() {
            return new Credential(
                nestedCredential_ID, nestedServiceName, nestedServiceUrl, nestedUsername, nestedEmail,
                nestedEncodedPassword, nestedDateLastModified, nestedNote, nestedActive);
        }
    }
}


//        public Builder(int Credential_ID) {
//            this.Credential_ID = Credential_ID;
//        }
//
//        public Builder withServiceName(String ServiceName){
//            this.ServiceName = ServiceName;
//            return this;  //By returning the builder each time, we can create a fluent interface.
//        }
//
//        public Builder withServiceUrl(String ServiceUrl){
//            this.ServiceUrl = ServiceUrl;
//            return this;
//        }
//
//        public Builder withUsername(String Username){
//            this.Username = Username;
//            return this;
//        }
//
//        public Builder withEmail(String Email){
//            this.Email = Email;
//            return this;
//        }
//
//        public Builder withEncodedPassword(String EncodedPassword){
//            this.EncodedPassword = EncodedPassword;
//            return this;
//        }
//
//        public Builder withDateLastModified(String DateLastModified){
//            this.ServiceUrl = DateLastModified;
//            return this;
//        }
//
//        public Builder withNote(String Note){
//            this.Note = Note;
//            return this;
//        }
//
//        public Builder withActive(boolean Active){
//            this.Active = Active;
//            return this;
//        }
//
//        public Credential build() {
//            Credential credential = new Credential();  //Since the builder is in the BankAccount class, we can invoke its private constructor.
//            credential.Credential_ID = this.Credential_ID;
//            credential.ServiceName = this.ServiceName;
//            credential.ServiceUrl = this.ServiceUrl;
//            credential.Username = this.Username;
//            credential.Email = this.Email;
//            credential.EncodedPassword = this.EncodedPassword;
//            credential.DateLastModified = this.DateLastModified;
//            credential.Note = this.Note;
//            credential.Active = this.Active;
//            return credential;
//        }
//
//        private Credential() {
//            //Constructor is now private.
//        }
//
//        }
//
////    public void setCredential_ID(int credential_ID) {
////        Credential_ID = credential_ID;
////    }
////
////    public void setServiceName(String serviceName) {
////        ServiceName = serviceName;
////    }
////
////    public void setServiceUrl(String serviceUrl) {
////        ServiceUrl = serviceUrl;
////    }
////
////    public void setUsername(String username) {
////        Username = username;
////    }
////
////    public void setEmail(String email) {
////        Email = email;
////    }
////
////    public void setEncodedPassword(String encodedPassword) {
////        EncodedPassword = encodedPassword;
////    }
////
////    public void setDateLastModified(String dateLastModified) {
////        DateLastModified = dateLastModified;
////    }
////
////    public void setNote(String note) {
////        Note = note;
////    }
////
////    public void setActive(boolean active) {
////        Active = active;
////    }
//
//    public JSONObject toJSON() {
//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put("id", this.Credential_ID);
//            jsonObject.put("serviceName", this.ServiceName);
//            jsonObject.put("serviceUrl", this.ServiceUrl);
//            jsonObject.put("username", this.Username);
//            jsonObject.put("email", this.Email);
//            jsonObject.put("encodedPassword", this.EncodedPassword);
//            jsonObject.put("dateLastModified", this.DateLastModified);
//            jsonObject.put("note", this.Note);
//            jsonObject.put("active", this.Active);
//        } catch(JSONException e) {
//            Log.e("CredentialsApp", e.getMessage());
//        }
//        return jsonObject;
//    }
//}
