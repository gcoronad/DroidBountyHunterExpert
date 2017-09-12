package training.edu.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

import training.edu.models.Fugitivo;

/**
 * @author Giovani Gonzalez
 * Created by darkgeat on 27/08/2017.
 */

public class DBProvider {

    private static final String TAG = DBProvider.class.getSimpleName();
    /** --------------------------------- Nombre de Base de Datos -------------------------------------**/
    private static final String DataBaseName = "DroidBountyHunterDataBase";
    /** --------------------------------- Version de Base de Datos ---------------------------------**/
    private static final int version = 8;
    /** --------------------------------- Tablas y Campos ---------------------------------**/
    private static final String TABLE_NAME = "fugitivos";
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_NAME = "name";
    private static final String COLUMN_NAME_STATUS = "status";
    private static final String COLUMN_NAME_PHOTO = "photo";
    private static final String COLUMN_NAME_NOTIFICATION = "notification";

    private static final String TABLE_NAME_LOG = "Log";
    private static final String COLUMN_NAME_NOMBRE = "Nombre";
    private static final String COLUMN_NAME_FECHA = "Fecha";

    /** --------------------------------- Declaración de Tablas ----------------------------------**/
    private static final String TFugitivos = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_NAME_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            COLUMN_NAME_NAME + " TEXT NOT NULL, " +
            COLUMN_NAME_PHOTO + " TEXT, " +
            COLUMN_NAME_STATUS + " INTEGER, " +
            COLUMN_NAME_NOTIFICATION + " INTEGER, " +
            "UNIQUE (" + COLUMN_NAME_NAME + ") ON CONFLICT REPLACE);";

    private static final String TLog = "CREATE TABLE " + TABLE_NAME_LOG + " (" +
            COLUMN_NAME_NOMBRE + " TEXT," +
            COLUMN_NAME_STATUS + " TEXT," +
            COLUMN_NAME_NOTIFICATION + " INTEGER, " +
            COLUMN_NAME_FECHA + " DATE);";

    private static final String TLogTrigger = "CREATE TRIGGER logTrigger " +
            " BEFORE DELETE ON " + TABLE_NAME  +
            " FOR EACH ROW " +
            "BEGIN " +
            "INSERT INTO "+ TABLE_NAME_LOG +" (Nombre, Fecha, Status, notification) Values(old.name, datetime('now'), old.status, old.notification);" +
            " END";

    /** --------------------------------- Variables y Helpers ----------------------------------**/
    private DBHelper helper;
    private SQLiteDatabase database;
    private Context context;

    public DBProvider(Context c){
        context = c;
    }

    private DBProvider open(){
        helper = new DBHelper(context);
        database = helper.getWritableDatabase();
        return this;
    }

    private DBProvider open_read(){
        helper = new DBHelper(context);
        database = helper.getReadableDatabase();
        return this;
    }

    private void close(){
        helper.close();
        database.close();
    }

    private Cursor querySQL(String sql, String[] selectionArgs){
        Cursor regreso = null;
        open_read();
        regreso = database.rawQuery(sql, selectionArgs);
        return regreso;
    }

    public ArrayList<Fugitivo> GetFugitivos(boolean fueCapturado){
        ArrayList<Fugitivo> fugitivos = new ArrayList<>();
        String isCapturado = fueCapturado ? "1" : "0";
        Cursor dataCursor = querySQL("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_STATUS
                + "= ? ORDER BY " + COLUMN_NAME_NAME, new String[]{isCapturado});
        if (dataCursor != null && dataCursor.getCount() > 0){
            for (dataCursor.moveToFirst() ; !dataCursor.isAfterLast() ; dataCursor.moveToNext()){
                int id = dataCursor.getInt(dataCursor.getColumnIndex(COLUMN_NAME_ID));
                String name = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_NAME));
                String status = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_STATUS));
                String photo = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_PHOTO));
                int notification = dataCursor.getInt(dataCursor.getColumnIndex(COLUMN_NAME_NOTIFICATION));
                fugitivos.add(new Fugitivo(id,name,status, photo, notification));
            }
        }
        close();
        return fugitivos;
    }

    public ArrayList<Fugitivo> ObtenerFugitivos(){
        ArrayList<Fugitivo> fugitivos = new ArrayList<>();
        Cursor dataCursor = querySQL("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_NAME_NAME, null);
        if (dataCursor != null && dataCursor.getCount() > 0){
            for (dataCursor.moveToFirst() ; !dataCursor.isAfterLast() ; dataCursor.moveToNext()){
                int id = dataCursor.getInt(dataCursor.getColumnIndex(COLUMN_NAME_ID));
                String name = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_NAME));
                String status = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_STATUS));
                String photo = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_PHOTO));
                fugitivos.add(new Fugitivo(id,name,status, photo));
            }
        }
        close();
        return fugitivos;
    }

    public int ContarFugitivos(){
        Cursor dataCursor = querySQL("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_STATUS + " = 0;", null);
        int cuenta = dataCursor.getCount();
        close();
        return cuenta;
    }

    public void InsertFugitivo(Fugitivo fugitivo){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NAME, fugitivo.getName());
        values.put(COLUMN_NAME_STATUS, fugitivo.getStatus());
        values.put(COLUMN_NAME_PHOTO, fugitivo.getPhoto());
        values.put(COLUMN_NAME_NOTIFICATION, fugitivo.getNotification());
        open();
        database.insert(TABLE_NAME,null,values);
        close();
    }

    public void UpdateFugitivo(Fugitivo fugitivo){
        open();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NAME, fugitivo.getName());
        values.put(COLUMN_NAME_STATUS, fugitivo.getStatus());
        values.put(COLUMN_NAME_PHOTO, fugitivo.getPhoto());
        database.update(TABLE_NAME,values,COLUMN_NAME_ID + "=?",new String[]{String.valueOf(fugitivo.getId())});
        close();
    }

    public void DeleteFugitivo(int idFugitivo){
        open();
        database.delete(TABLE_NAME, COLUMN_NAME_ID + "=?",new String[]{String.valueOf(idFugitivo)});
        close();
    }

    public ArrayList<String[]> ObtenerLogsEliminacion(){
        ArrayList<String[]> arrayList = new ArrayList<>();
        Cursor dataCursor = querySQL("SELECT * FROM " + TABLE_NAME_LOG, null);
        if(dataCursor !=null && dataCursor.getCount() > 0){
            for(dataCursor.moveToFirst(); !dataCursor.isAfterLast(); dataCursor.moveToNext()){
                String name = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_NOMBRE));
                String date = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_FECHA));
                String status = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_STATUS));
                arrayList.add(new String[]{name, date, status});
            }
        }
        close();
        return arrayList;
    }

    public ArrayList<Fugitivo> ObtenerFugitivosNotificacion(){
        ArrayList<Fugitivo> fugitivos = new ArrayList<>();
        String isNotNotificated = "0";
        Cursor dataCursor = querySQL("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_NOTIFICATION + "=? ORDER BY " + COLUMN_NAME_NAME, new String[]{isNotNotificated});

        if(dataCursor != null && dataCursor.getCount() > 0){
            for(dataCursor.moveToFirst(); !dataCursor.isAfterLast(); dataCursor.moveToNext()){
                int id = dataCursor.getInt(dataCursor.getColumnIndex(COLUMN_NAME_ID));
                String name = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_NAME));
                String status = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_STATUS));
                String photo = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_PHOTO));
                int notification = dataCursor.getInt(dataCursor.getColumnIndex(COLUMN_NAME_NOTIFICATION));
                fugitivos.add(new Fugitivo(id, name, status, photo, notification));
            }
        }



        UpdateFugitivosNotificacion();
        close();

        return fugitivos;
    }

    public ArrayList<String[]> ObtenerLogsNotification(){
        ArrayList<String[]> arrayList = new ArrayList<>();
        Cursor dataCursor = querySQL("SELECT * FROM " + TABLE_NAME_LOG + " WHERE " + COLUMN_NAME_NOTIFICATION + " = ? ORDER BY " + COLUMN_NAME_NOMBRE, new String[]{"0"});
        if(dataCursor != null && dataCursor.getCount() > 0){
            for(dataCursor.moveToFirst(); !dataCursor.isAfterLast(); dataCursor.moveToNext()){
                String name = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_NOMBRE));
                String date = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_FECHA));
                String status = dataCursor.getString(dataCursor.getColumnIndex(COLUMN_NAME_STATUS));
                int notification = dataCursor.getInt(dataCursor.getColumnIndex(COLUMN_NAME_NOTIFICATION));
                arrayList.add(new String[]{name, date, status, String.valueOf(notification)});
            }
        }

        UpdateLogNotificacion();
        close();
        return arrayList;
    }


    private void UpdateFugitivosNotificacion(){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NOTIFICATION, "1");
        database.update(TABLE_NAME, values, COLUMN_NAME_NOTIFICATION + "=?",new String[]{"0"});
    }

    private void UpdateLogNotificacion(){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NOTIFICATION, "1");
        database.update(TABLE_NAME_LOG, values, COLUMN_NAME_NOTIFICATION + "=?",new String[]{"0"});
    }

    public Cursor ObtenerUltimoFugitivo(String selection) {
        String[] filter = {selection};

        Cursor aRS = querySQL("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_STATUS + " = ? ORDER BY " + COLUMN_NAME_ID + " DESC", filter);
        return aRS;
    }


    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DataBaseName, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TFugitivos);
            db.execSQL(TLog);
            db.execSQL(TLogTrigger);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Actualización de la BDD de la versión " + oldVersion + "a la " +
                    + newVersion + ", de la que se destruirá la información anterior");

            // Destruir BDD anterior y crearla nuevamente las tablas actualizadas
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LOG);
            db.execSQL("DROP TRIGGER IF EXISTS logTrigger");
            // Re-creando nuevamente la BDD actualizada
            onCreate(db);
        }
    }
}
