package com.example.akusei.paoapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CallActivity extends AppCompatActivity {
    Button btnPatrullas, btnFelcc, btnBomberos, btnDiprove, btnDefensoria, btnEmergencias, btnReten, btnBusqueda, btnvoluntariado, btnPac, btnTransito;
    PhoneCallListener phoneListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        btnBomberos = (Button) findViewById(R.id.btnBomberos);
        btnBusqueda = (Button) findViewById(R.id.btnSisBusqSalvto);
        btnDefensoria = (Button) findViewById(R.id.btnDefensoria);
        btnDiprove = (Button) findViewById(R.id.btnDiprove);
        btnEmergencias = (Button) findViewById(R.id.btnEmergencias);
        btnFelcc = (Button) findViewById(R.id.btnFelcc);
        btnPac = (Button) findViewById(R.id.btnPac);
        btnPatrullas = (Button) findViewById(R.id.btnRadioPatrulla);
        btnReten = (Button) findViewById(R.id.btnRetenEmergencias);
        btnTransito = (Button) findViewById(R.id.btnOrgOpTransito);
        btnvoluntariado = (Button) findViewById(R.id.btnVolSalvtoResc);
        estadoTelefono();


    }
    public void llamabomberos(View v){
        llamar("119");
    }
    public void llamadefensoria(View v){
        llamar("156");
    }
    public void llamafelcc(View v){
        llamar("116");
    }
    public void llamadiprove(View v){
        llamar("115");
    }
    public void llamatransito(View v){
        llamar("111");
    }
    public void llamaemergencias(View v){
        llamar("160");
    }
    //unidad integral gestion de riesgos
    public void llamareten(View v){
        llamar("114");
    }
    public void llamapac(View v){
        llamar("120");
    }
    public void llamavoluntarios(View v){
        llamar("132");
    }
    public void llamaradiopatrulla(View v){
        llamar("110");
    }
    public void llamabusquedaysalvamento(View v){
        llamar("128");
    }
    public void llamar(String nro){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+nro));

        if (ActivityCompat.checkSelfPermission(CallActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }
    private void estadoTelefono() {
        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private class PhoneCallListener extends PhoneStateListener {

        private boolean isPhoneCalling = false;

        String LOG_TAG = "LOGGING 123";

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
                Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                Log.i(LOG_TAG, "OFFHOOK");

                isPhoneCalling = true;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {
                // run when class initial and phone call ended,
                // need detect flag from CALL_STATE_OFFHOOK
                Log.i(LOG_TAG, "IDLE");

                if (isPhoneCalling) {

                    Log.i(LOG_TAG, "restart app");

                    // restart app
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(
                                    getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                    isPhoneCalling = false;
                }

            }
        }
    }
}
