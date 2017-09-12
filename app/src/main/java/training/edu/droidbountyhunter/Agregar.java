package training.edu.droidbountyhunter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import training.edu.data.DBProvider;
import training.edu.models.Fugitivo;
import training.edu.ws.NetServices;
import training.edu.ws.OnTaskListener;

/**
 * @author Giovani González
 * Created by darkgeat on 09/08/2017.
 */

public class Agregar extends AppCompatActivity{

    DocumentBuilderFactory factory;
    DocumentBuilder builder;
    Document dom;
    Element root;
    NodeList items;
    String valor;
    int contadorPorcentaje;

    void importarXML(){
        try{
            InputStream inputStream = getResources().openRawResource(R.raw.fugitivos);
            dom = builder.parse(inputStream);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void retardo(){
        try{
            Thread.sleep(500);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    void insertarFugitivo(String nameFugitivo){
        DBProvider database = new DBProvider(this);
        database.InsertFugitivo(new Fugitivo(0, nameFugitivo, "0", "", 0));
    }

    public void OnXMLClick(View view){
        DBProvider database = new DBProvider(this);

        if(database.ContarFugitivos() <= 0){
            Button botonXML = (Button) findViewById(R.id.buttonAddXML);
            botonXML.setVisibility(View.GONE);
            Button botonSave = (Button) findViewById(R.id.buttonSave);
            botonSave.setVisibility(View.GONE);
            Button botonWebService = (Button) findViewById(R.id.buttonAddWebService);
            botonWebService.setVisibility(View.GONE);

            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            final TextView statusProgress = (TextView) findViewById(R.id.txtProgreso);
            progressBar.setMax(100);

            try{
                factory = DocumentBuilderFactory.newInstance();
                builder = factory.newDocumentBuilder();
                importarXML();
                root = dom.getDocumentElement();
                items = root.getElementsByTagName("fugitivo");
            }catch(Exception e){
                e.printStackTrace();
            }

            new Thread(){

                @Override
                public void run(){
                    progressBar.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(0);
                        }
                    });

                    for(int i = 0; i < items.getLength(); i++){
                        valor = items.item(i).getFirstChild().getNodeValue();
                        contadorPorcentaje = (i+1)*10;
                        retardo();
                        insertarFugitivo(valor);
                        progressBar.post(new Runnable() {
                            @Override
                            public void run() {
                                statusProgress.setText("Progreso " + contadorPorcentaje + "%");
                                progressBar.incrementProgressBy(10);
                            }
                        });
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Importación de fugitivos finalizada", Toast.LENGTH_LONG).show();
                            setResult(0);
                            finish();
                        }
                    });
                }
            }.start();

        }else{
            Toast.makeText(this, "No es posible solicitar la carga vía XML ya que se tiene al menos un fugitivo en la base de datos", Toast.LENGTH_LONG).show();
        }

    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar);
    }

    public void OnSaveClick(View view){
        TextView name = (TextView) findViewById(R.id.editTextName);
        String sName = name.getText().toString();
        if(sName.length() > 0){
            DBProvider database = new DBProvider(this);
            database.InsertFugitivo(new Fugitivo(0, sName,"0", "", 0));
            setResult(0);
            finish();
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("Alerta")
                    .setMessage("Favor de capturar el nombre del fugitivo")
                    .show();
        }
    }

    public void OnWebServiceClick(View view){
        final DBProvider database = new DBProvider(this);
        if(database.ContarFugitivos()==0){
            NetServices apiCall = new NetServices(new OnTaskListener() {
                @Override
                public void OnTaskCompleted(String json) {
                    try{
                        JSONArray array = new JSONArray(json);
                        for(int i = 0; i < array.length(); i++){
                            JSONObject object = array.getJSONObject(i);
                            String nameFugitive = object.optString("name", "");
                            database.InsertFugitivo(new Fugitivo(0, nameFugitive, "0","", 0));
                        }
                    }catch(JSONException e){
                        e.printStackTrace();
                    }finally {
                        setResult(0);
                        finish();
                    }
                }

                @Override
                public void OnTaskError(int code, String message, String error) {
                    Toast.makeText(Agregar.this, "Ocurrió un problema con el web service!!", Toast.LENGTH_LONG).show();
                }
            });
            apiCall.execute("Fugitivos");
        }else{
            Toast.makeText(this, "No se puede hacer la carga remota ya que se tiene al menos un fugitivo en la base de datos", Toast.LENGTH_LONG).show();
        }

    }

}
