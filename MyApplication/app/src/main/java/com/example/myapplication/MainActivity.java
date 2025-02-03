package com.example.myapplication;

import android.app.ComponentCaller;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;


/* Este proyecto servira de ejemplo de como llamar a nuevas activities de forma explicita (internas en la app) o implicitas (usando apps externas como la cámara, lector de documentos, etc...)
*  La App tendrá 4 botones que realizarán las llamadas a la nueva activity esperando un resultado.
*  Debajo de los botones se mostrará un log de los pasos que se han ido realizando con el nombre de los métodos visitados y variables relevantes.
* */

public class MainActivity extends AppCompatActivity {

    private static final int RES_CODE_SALUDO = 100;
    private static final int RES_CODE_CAMARA = 200;
    private String logMsg = "";
    private TextView tvLogger;


    /*Declaramos las variables que contienen el contrato*/
    ActivityResultLauncher<Intent> contratoActividadSaludo;
    ActivityResultLauncher<Uri> contratoActividadCamara;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvLogger = findViewById(R.id.tvLogger);

        //Inicializamos los botones con sus listeners

        findViewById(R.id.btnActivitySaludoDeprecated).setOnClickListener(saludoDeprecated());
        findViewById(R.id.btnActivitySaludoContract).setOnClickListener(saludoContract());
        findViewById(R.id.btnActivityCamaraDeprecated).setOnClickListener(camaraDeprecated());
        findViewById(R.id.btnActivityCamaraContract).setOnClickListener(camaraContract());

        //creamos los contratos de Saludo y Camara. es importante hacer esto en el onCreate o recibiremos un error de ejecución relacionado con el estado de la app

        contratoActividadSaludo = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        logMsg += "Gestionando respuesta saludo contrato\n";

                        //Si ha ocurrido algun error en la ejcucion de la activity llamada loggeo ERROR
                        if(result.getResultCode() != RESULT_OK) logMsg = "ERROR";
                        else {
                            assert result.getData() != null;
                            String log = result.getData().getStringExtra(SaludoActivity.ACTIVITY_RES_LOG);
                            logMsg += "\n" + log;
                        }
                    }
                });

        contratoActividadCamara = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                (result) -> {
                    logMsg += "Gestionando respuesta camara contrato\n";
                }
        );
    }

    private View.OnClickListener camaraContract() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLogger.setText("");
                logMsg = "Click camara contracts\n";
                String fileName = "photo_" + System.currentTimeMillis() + ".jpg";

                File imageFile = new File(v.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

                Uri uri = FileProvider.getUriForFile(v.getContext(), v.getContext().getPackageName() + ".provider", imageFile);
                contratoActividadCamara.launch(uri);
            }
        };
    }

    private View.OnClickListener camaraDeprecated() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLogger.setText("");
                logMsg = "Click camara deprecated\n";
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, RES_CODE_CAMARA);
            }
        };
    }

    private View.OnClickListener saludoContract() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLogger.setText("");
                logMsg = "Click saludo contracts\n";
                /*Se crea el intent con la actividad de destino*/
                Intent i = new Intent(v.getContext(), SaludoActivity.class);
                /*Añadimos un extra como valor de transmision a la nueva actividad.
                 * Fijemonos que usamos una constante para identificar esa entrada y la declaramos en la activity saludo
                 * */
                i.putExtra(SaludoActivity.ACTIVITY_PARAM_NOMBRE, "Ramon");
                /*Le pedimos al contrato que se ejecute usando el intent creado anteriormente*/
                contratoActividadSaludo.launch(i);
            }
        };
    }

    private View.OnClickListener saludoDeprecated() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLogger.setText("");
                logMsg = "Click saludo deprecated\n";
                /*Se crea el intent con la actividad de destino*/
                Intent i = new Intent(v.getContext(), SaludoActivity.class);
                /*Añadimos un extra como valor de transmision a la nueva actividad.
                * Fijemonos que usamos una constante para identificar esa entrada y la declaramos en la activity saludo
                * */
                i.putExtra(SaludoActivity.ACTIVITY_PARAM_NOMBRE, "PEDRO");
                /*
                * Finalmente lanzamos la activity esperando un resultado. Para ello añadimos el RES_CODE_SALUDO para poder identificar
                * esta llamada posteriormente.
                *
                * La constante esta declarada en esta misma clase pues es ella la encargada de gestionar las actividades llamadas
                * */
                startActivityForResult(i, RES_CODE_SALUDO);
            }
        };
    }



    /* Cuando una actividad llamada para obtener un resultado ha finalizado se lanza este método.
    * Lo usaremos para recoger la respuesta de la activity llamada y procesarla
    * Request code - Codigo que identifica que actividad creo el resultado
    * Result code - Determina si la actividad se ha ejecutado correctamente o no
    * data - Intent que contiene el mensaje de respuesta
    * */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        logMsg += "onActivityResult iniciado con requestCode: " + requestCode + " y resultCode " + resultCode+ "\n";

        /* Identificamos que la respuesta ha llegado desde la activitySaludo*/
        if(requestCode == RES_CODE_SALUDO){
            assert data != null;
            gestionaRespuestaSaludo(resultCode, data);
            return;
        }

        if(requestCode == RES_CODE_CAMARA){
            assert data != null;
            gestionaRespuestaCamara(resultCode, data);
            return;
        }

        tvLogger.setText(logMsg);

    }

    private void gestionaRespuestaCamara(int resultCode, Intent data) {
        logMsg += "Gestionando respuesta camara\n";

        //Si ha ocurrido algun error en la ejcucion de la activity llamada loggeo ERROR
        if(resultCode != RESULT_OK) logMsg = "ERROR";
        else {

            String log = "Imagen extraida correctamente";
            logMsg += "\n" + log;
        }
        tvLogger.setText(logMsg);
    }


    private void gestionaRespuestaSaludo(int resultCode, Intent data) {
        logMsg += "Gestionando respuesta saludo\n";

        //Si ha ocurrido algun error en la ejcucion de la activity llamada loggeo ERROR
        if(resultCode != RESULT_OK) logMsg = "ERROR";
        else {

            String log = data.getStringExtra(SaludoActivity.ACTIVITY_RES_LOG);
            logMsg += "\n" + log;
        }
    }
}