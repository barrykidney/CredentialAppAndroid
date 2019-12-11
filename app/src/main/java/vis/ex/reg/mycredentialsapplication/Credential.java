package vis.ex.reg.mycredentialsapplication;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity
public class Credential {

    @PrimaryKey
    public int Credential_ID;
    public String ServiceName;
    public String EncodedPassword;
    public String DateLastModified;
    public boolean Active;

    public Credential(int Credential_ID, String ServiceName,
                      String EncodedPassword, String DateLastModified, boolean Active) {
        this.Credential_ID = Credential_ID;
        this.ServiceName = ServiceName;
        this.EncodedPassword = EncodedPassword;
        this.DateLastModified = DateLastModified;
        this.Active = Active;
    }
}
