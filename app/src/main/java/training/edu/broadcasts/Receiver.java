package training.edu.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import training.edu.droidbountyhunter.ServicioNotificaciones;

/**
 * Created by gcoronad on 07/09/2017.
 */

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, ServicioNotificaciones.class);
        if(!ServicioNotificaciones.isRunning())context.startService(serviceIntent);
    }
}
