package vis.ex.reg.mycredentialsapplication;

import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

import java.util.List;


@Dao
public interface CredentialDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addCredential(Credential credential);

    @Query("SELECT * FROM Credential")
    public List<Credential> getAllCredentials();

    @Query("SELECT Credential_ID, DateLastModified, ServiceName, Note, Active FROM Credential")
    public List<CredentialDTO> getAllCredentialsSummary();

    @Query("SELECT * FROM Credential WHERE Credential_ID = :credential_id")
    public List<Credential> getCredentialById(int credential_id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCredential(Credential credential);

    @Query("DELETE FROM Credential")
    void deleteAllCredentials();

    @Query("DELETE FROM Credential WHERE Credential_ID = :credential_id")
    void deleteCredential(int credential_id);
}
