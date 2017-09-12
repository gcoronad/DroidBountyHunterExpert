package training.edu.droidbountyhunter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import training.edu.data.DBProvider;
import training.edu.models.Fugitivo;
import training.edu.utils.PictureTools;
import training.edu.ws.NetServices;
import training.edu.ws.OnTaskListener;

/**
 * @author Giovani González
 *         Created by darkgeat on 09/08/2017.
 */

public class Detalle extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);
        // Se obtiene la información del intent
        titulo = getIntent().getStringExtra("title");
        mode = getIntent().getIntExtra("mode", 0);
        id = getIntent().getIntExtra("id", 0);
        // Se pone el nombre del fugitivo como titulo
        setTitle(titulo + " - [" + id + "]");
        TextView message = (TextView) findViewById(R.id.mensajeText);
        openGLButton = (Button) findViewById(R.id.btnOpenGL);
        checkDefault = (CheckBox) findViewById(R.id.checkDefault);
        txtDistorcion = (EditText) findViewById(R.id.txtDistorsion);
        // Se identifica si es Fugitivo o Capturado para el mensaje...
        if (mode == 0) {
            message.setText("El fugitivo sigue suelto...");
            openGLButton.setVisibility(View.GONE);
            checkDefault.setVisibility(View.GONE);
            txtDistorcion.setVisibility(View.GONE);
        } else {
            Button delete = (Button) findViewById(R.id.buttonEliminar);
            delete.setVisibility(View.GONE);
            Button capturar = (Button) findViewById(R.id.buttonCapturar);
            capturar.setVisibility(View.GONE);
            message.setText("Atrapado!!!");

            ImageView photoImageView = (ImageView) findViewById(R.id.pictureFugitive);
            String pathPhoto = getIntent().getStringExtra("photo");
            if (pathPhoto != null && pathPhoto.length() > 0) {
                Bitmap bitmap = PictureTools.decodeSampledBitmapFromUri(pathPhoto, 200, 200);
                photoImageView.setImageBitmap(bitmap);
                foto = pathPhoto;
            }

        }
    }


    private String titulo;
    private int mode;
    private int id;
    private String foto;
    private Button openGLButton;
    private CheckBox checkDefault;
    private EditText txtDistorcion;

    public void OnCaptureClick(View view) {
        DBProvider database = new DBProvider(this);
        String pathPhoto = PictureTools.currentPhotoPath;

        if (pathPhoto == null || pathPhoto.length() == 0) {
            Toast.makeText(this, "Es necesario tomar la foto antes de capturar al fugitivo", Toast.LENGTH_LONG).show();
            return;
        }
        database.UpdateFugitivo(new Fugitivo(id, titulo, "1", pathPhoto.length() == 0 ? "" : pathPhoto, 0));

        NetServices netServices = new NetServices(new OnTaskListener() {
            @Override
            public void OnTaskCompleted(String json) {
                String message = "";
                try {
                    JSONObject object = new JSONObject(json);
                    message = object.optString("mensaje", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MessageClose(message);
            }

            @Override
            public void OnTaskError(int code, String message, String error) {
                Toast.makeText(Detalle.this, "Ocurrió un problema en la comunicación con el webservice!!", Toast.LENGTH_LONG).show();
            }
        });

        netServices.execute("Atrapar", Home.UDID);
        openGLButton.setVisibility(View.VISIBLE);
        checkDefault.setVisibility(View.VISIBLE);
        txtDistorcion.setVisibility(View.VISIBLE);
        setResult(0);
        //finish();
    }

    public void OnDeleteClick(View view) {
        DBProvider database = new DBProvider(this);
        database.DeleteFugitivo(id);
        setResult(0);
        finish();
    }

    private Uri pathImage;
    private static final int REQUEST_CODE_PHOTO_IMAGE = 1787;

    public void OnFotoClick(View view) {
        if (PictureTools.permissionReadMemmory(this)) {
            dispatchPicture();
        }
    }

    private void dispatchPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        pathImage = PictureTools.with(this).getOutputMediaFileUri(PictureTools.MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pathImage);
        startActivityForResult(intent, REQUEST_CODE_PHOTO_IMAGE);
    }

    public void MessageClose(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.create();
        builder.setTitle("Alerta!!!");
        builder.setMessage(message);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                setResult(mode);
                finish();
            }
        });

        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PHOTO_IMAGE) {
            if (resultCode == RESULT_OK) {
                ImageView imageFugitive = (ImageView) findViewById(R.id.pictureFugitive);
                Bitmap bitmap = PictureTools.decodeSampledBitmapFromUri(PictureTools.currentPhotoPath, 200, 200);
                imageFugitive.setImageBitmap(bitmap);
            }
        }
    }


    public void OnOpenGL(View view) {
        String texto = txtDistorcion.getText().toString();
        String defaultValue = "0";
        if (checkDefault.isChecked()) {
            defaultValue = "1";
        }

        try {
            float textoFloat = Float.parseFloat(texto);
            if(textoFloat < 0.0f){
                Toast.makeText(this, "Es una entrada erronea, el número debe de ser flotante", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(this, ActivityOpenGLFugitivos.class);
            intent.putExtra("foto", foto);
            intent.putExtra("distorsion", texto);
            intent.putExtra("default", defaultValue);
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Se encontró un error al tratar de leer el número", Toast.LENGTH_LONG).show();
        }
    }
}
