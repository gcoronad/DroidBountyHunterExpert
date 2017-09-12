package training.edu.droidbountyhunter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import training.edu.data.DBProvider;
import training.edu.models.Fugitivo;
import training.edu.notifications.NotifyManager;

/**
 * Created by gcoronad on 07/09/2017.
 */

public class ServicioNotificaciones extends Service {
    private static ServicioNotificaciones instance = null;
    private Timer timer;

    @Override
    public void onCreate(){
        Toast.makeText(this, "Servicio creado", Toast.LENGTH_LONG).show();
        instance = this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isRunning(){
        return instance != null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this, "Servicio Arrancado" + startId, Toast.LENGTH_LONG).show();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EnviarNotificacion();
            }
        },0,1000*60);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Toast.makeText(this, "Servicio detenido", Toast.LENGTH_SHORT).show();
        instance = null;
    }


    public void EnviarNotificacion(){
        try{
            String mensaje = "";
            DBProvider database = new DBProvider(this);
            ArrayList<Fugitivo> fugitivosSinNotificar = database.ObtenerFugitivosNotificacion();
            ArrayList<String[]> logsSinNotificar = database.ObtenerLogsNotification();
            int added = fugitivosSinNotificar.size();
            int deleted = logsSinNotificar.size();

            if(added > 0){
                mensaje += "Añadiste " + added;
                if(deleted > 0){
                    mensaje += ", Eliminaste " + deleted;
                }
            }else if(deleted > 0){
                mensaje += "Eliminaste " + deleted;
            }else{
                mensaje = "";
            }

            if(mensaje.length() > 0){
                NotifyManager manager = new NotifyManager();
                manager.enviarNotificacion(this, Home.class, mensaje, "Notificación DroidBountyHunter", R.mipmap.ic_launcher, 0);
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
