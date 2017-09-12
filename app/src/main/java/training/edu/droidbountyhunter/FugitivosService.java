package training.edu.droidbountyhunter;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import java.util.ArrayList;

import training.edu.data.DBProvider;
import training.edu.models.Fugitivo;

/**
 * Created by gcoronad on 12/09/2017.
 */

public class FugitivosService extends Service {
    static final int MSG_OBTENER_FUGITIVOS = 1;
    DBProvider db;


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_OBTENER_FUGITIVOS:

                    Messenger messenger = msg.replyTo;
                    Message msgFugitivos = Message.obtain(null, 2);
                    Bundle bundle = new Bundle();
                    ArrayList<Fugitivo> fugitivos = db.ObtenerFugitivos();
                    ArrayList<Integer> ids = new ArrayList<>();
                    ArrayList<CharSequence> nombres = new ArrayList<>();
                    ArrayList<CharSequence> status = new ArrayList<>();
                    ArrayList<CharSequence> fotos = new ArrayList<>();

                    for (Fugitivo fugitivo : fugitivos) {
                        ids.add(fugitivo.getId());
                        nombres.add(fugitivo.getName());
                        status.add(fugitivo.getStatus());
                        fotos.add(fugitivo.getPhoto());
                    }

                    bundle.putIntegerArrayList("ids", ids);
                    bundle.putCharSequenceArrayList("nombres", nombres);
                    bundle.putCharSequenceArrayList("status", status);
                    bundle.putCharSequenceArrayList("fotos", fotos);

                    msgFugitivos.setData(bundle);
                    try {
                        messenger.send(msgFugitivos);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        db = new DBProvider(getApplicationContext());
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }
}
