package training.edu.droidbountyhunter;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by gcoronad on 07/09/2017.
 */

public class FirebaseIdService extends FirebaseInstanceIdService {
    private static final String TAG = FirebaseNotificationService.class.getSimpleName();

    @Override
    public void onTokenRefresh(){
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Token: " + refreshedToken);
    }
}
