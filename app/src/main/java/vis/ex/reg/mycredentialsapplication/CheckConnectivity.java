package vis.ex.reg.mycredentialsapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;


public class CheckConnectivity extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {

        boolean notConnected = arg1.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,
                false);

        if(notConnected) {
            context.sendBroadcast(new Intent("connection_change").putExtra("state", "unavailable"));
        } else {
            context.sendBroadcast(new Intent("connection_change").putExtra("state", "available"));
        }
    }
}