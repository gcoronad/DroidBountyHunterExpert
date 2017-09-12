package training.edu.droidbountyhunter;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import training.edu.notifications.NotifyManager;

/**
 * Created by gcoronad on 07/09/2017.
 */

public class FirebaseNotificationService extends FirebaseMessagingService {

    private static final String TAG = FirebaseNotificationService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.v(TAG, "From: " + remoteMessage.getFrom());
        Log.v(TAG, "Notification message Body: " + remoteMessage.getNotification().getBody());

        NotifyManager manager = new NotifyManager();
        manager.enviarNotificacion(this, Home.class, remoteMessage.getNotification().getBody(), "Notificacion push", R.mipmap.ic_launcher, 0);
    }
}
