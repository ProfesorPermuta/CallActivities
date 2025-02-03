package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SaludoActivity extends AppCompatActivity {

    public static final String ACTIVITY_PARAM_NOMBRE = "nombre";
    public static final String ACTIVITY_RES_LOG = "log";

    private String log = "";

    Button btnRet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.saludo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnRet = findViewById(R.id.btnRet);

        btnRet.setOnClickListener((v) -> {
           /*Encapsulamos la respuesta en un Intent
           * Fijemonos que para devolver el log, el intent no tiene activity.class porque no pretendo iniciar ninguna actividad con este intent
           * */
            log = "Iniciada Activity Saludo y pulsado el boton de volver\n";
            Intent i = new Intent();
            i.putExtra(ACTIVITY_RES_LOG, log);
            /*añadimos el intent al resultado y le asinamos el codigo OK*/
            setResult(RESULT_OK, i);
            finish();
        });
    }
}