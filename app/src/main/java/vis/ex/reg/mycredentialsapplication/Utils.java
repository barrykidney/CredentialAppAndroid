package vis.ex.reg.mycredentialsapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


class Utils {

    static String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }

    static int setHighestCredential(List<CredentialSummaryDTO> creds) {
        int highestIndex = 0;
        for (CredentialSummaryDTO cred : creds) {
            Integer id = cred.getCredential_ID();
            if (id > highestIndex) {
                highestIndex = id;
            }
        }
        return highestIndex;
    }

    static List<CredentialSummaryDTO> searchCredentials(String searchString, List<CredentialSummaryDTO> list) {
        List<CredentialSummaryDTO> searchResults = new ArrayList<>();

        for (CredentialSummaryDTO credentialSummary : list) {
            String serviceName = credentialSummary.getServiceName().toLowerCase();
            String note = credentialSummary.getNote().toLowerCase();
            if (serviceName.contains(searchString.toLowerCase()) | note.contains(searchString.toLowerCase())) {
                searchResults.add(credentialSummary);
            }
        }
        return searchResults;
    }

    static Snackbar initializeConnectivitySnackBar(Context context, View view) {
        Snackbar snackBar = Snackbar.make(view, "Connection unavailable", Snackbar.LENGTH_INDEFINITE);
        View snackBarView = snackBar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbarBackgroundColor));
        TextView snackBarTextView = snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        if (Build.VERSION.SDK_INT >= 21) {
            snackBarTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            snackBarTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        return snackBar;
    }

    static boolean internetConnectionIsAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
            || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    static ArrayList<Integer> createIdListFromSparseArray(SparseArray<CredentialSummaryDTO> list) {
        ArrayList<Integer> idList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            idList.add(list.keyAt(i));
        }
        return idList;
    }

    static SparseArray<CredentialSummaryDTO> createSparseArrayFromArrayList(List<CredentialSummaryDTO> list) {
        SparseArray<CredentialSummaryDTO> sa = new SparseArray<>();
        for (CredentialSummaryDTO c : list) {
            sa.put(c.getCredential_ID(), c);
        }
        return sa;
    }


    static List<CredentialSummaryDTO> getAllActiveCredentials(List<CredentialSummaryDTO> list) {
        List<CredentialSummaryDTO> newList = new ArrayList<>();
        for (CredentialSummaryDTO cred : list) {
            if (cred.getActive()) {
                newList.add(cred);
            }
        }
        return newList;
    }

    static List<Integer> getIntersectionOfAB(List<Integer> a, List<Integer> b) {
        ArrayList<Integer> c = new ArrayList<>(a);
        c.retainAll(b);
        return c;
    }

    static List<Integer> getExclusiveToA(List<Integer> a, List<Integer> b) {
        List<Integer> c = new ArrayList<>(a);
        c.removeAll(b);
        return c;
    }
}
