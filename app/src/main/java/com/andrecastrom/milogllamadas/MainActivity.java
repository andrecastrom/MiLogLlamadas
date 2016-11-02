package com.andrecastrom.milogllamadas;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int CODIGO_SOLICITUD_PERMISOS = 1;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
    }


    public void mostrarLlamadas(View view) {
        if (checkStatusPermiso()) {
            consultarCPLlamadas();
        } else {
            solicitarPermiso();
        }
    }


    public void solicitarPermiso() {
        //Read Call Log
        boolean solicitarPermisoRCL = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CALL_LOG);
        //Write Call Log
        boolean solicitarPermisoWCL = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_CALL_LOG);

        if (solicitarPermisoRCL && solicitarPermisoWCL) {
            Toast.makeText(MainActivity.this, "Los permisos fueron otorgados", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG},
                    CODIGO_SOLICITUD_PERMISOS);
        }
    }


    public boolean checkStatusPermiso() {
        boolean permisoRCL = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED);
        boolean permisoWCL = (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED);
        return (permisoRCL && permisoWCL);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CODIGO_SOLICITUD_PERMISOS:
                if (checkStatusPermiso()) {
                    Toast.makeText(MainActivity.this, "Ya está activo el permiso", Toast.LENGTH_SHORT).show();
                    consultarCPLlamadas();
                } else {
                    Toast.makeText(MainActivity.this, "No se ha activado el permiso", Toast.LENGTH_SHORT).show();
                }
        }
    }


    public void consultarCPLlamadas() {
        TextView tvLlamadas = (TextView) findViewById(R.id.tvLlamadas);
        tvLlamadas.setText("");

        Uri direccionUriLlamadas = CallLog.Calls.CONTENT_URI;

        //Numero de telefono, fecha, duracion
        String[] campos = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION
        };

        ContentResolver contentResolver = getContentResolver();
        Cursor registros = contentResolver.query(direccionUriLlamadas, campos, null, null, CallLog.Calls.DATE + " DESC");

        while (registros.moveToNext()) {
            //Obtenemos los datos a partir del indice de la columna
            String numero   = registros.getString(registros.getColumnIndex(campos[0]));
            Long fecha      = registros.getLong(registros.getColumnIndex(campos[1]));
            int tipo        = registros.getInt(registros.getColumnIndex(campos[2]));
            String duracion = registros.getString(registros.getColumnIndex(campos[3]));
            String tipoLlamada = "";

            //Validacion del tipo de llamada
            switch (tipo) {
                case CallLog.Calls.INCOMING_TYPE:
                    tipoLlamada = getResources().getString(R.string.entrada);
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    tipoLlamada = getResources().getString(R.string.perdida);
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    tipoLlamada = getResources().getString(R.string.salida);
                    break;
                default:
                    tipoLlamada = getResources().getString(R.string.desconocida);
            }

            String detalle = getResources().getString(R.string.etiqueta_numero) + numero + "\n" +
                    getResources().getString(R.string.etiqueta_fecha) + DateFormat.format("dd/mm/yy k:mm",fecha) + "\n" +
                    getResources().getString(R.string.etiqueta_tipo) + tipoLlamada + "\n" +
                    getResources().getString(R.string.etiqueta_duracion) + duracion + "s." + "\n\n";

            tvLlamadas.append(detalle);

        }
    }
}
