package com.example.akusei.paoapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener{
    Button btnOnOff,btnDiscover,btnSend;
    ListView listView;
    TextView read_msg_box,connectionStatus;
    EditText writeMsg;
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    List<WifiP2pDevice> peers= new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;
    ListView numerosEmergencias;
    DrawerLayout drawerLayout;
    public static String tvLongi;
    public static String tvLati;
    LocationManager locationManager;
    String[] opciones = { "Radiopatrullas", "Bomberos", "Emergencias Medicas", "Gestion de Riesgos" };

    static final int MESSAGE_READ=1;
    Server server;
    ClientClass client;
    SendRecieve sendRecieve;
    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_READ:
                    byte[] readBuff=(byte[])msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    read_msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    @Override
    public void onLocationChanged(Location location) {
        tvLongi = String.valueOf(location.getLongitude());
        tvLati = String.valueOf(location.getLatitude());

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(this, "Enabled new provider!" + s,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }
    public void CheckPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }
    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
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
    public void llamar(String nro){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+nro));

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }
    private void estadoTelefono() {
        MainActivity.PhoneCallListener phoneListener = new MainActivity.PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
                Snackbar.make(view, tvLati+" "+tvLongi, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                String location="Longitud: "+tvLongi+"\nLatitud: "+tvLati;
                sendRecieve.write(location.getBytes());


            }
        });
        CustomArrayAdapter<String> madapter=new CustomArrayAdapter<String>(this,android.R.layout.simple_list_item_1,opciones);
        numerosEmergencias.setAdapter(madapter);
       /* numerosEmergencias.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                opciones));*/
        numerosEmergencias.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int arg2, long arg3) {
                Toast.makeText(MainActivity.this, "Item: " + opciones[arg2],
                        Toast.LENGTH_SHORT).show();
                switch(arg2){
                    case 0:
                        llamar("110");
                        break;
                    case 1:
                        llamar("119");
                        break;
                    case 2:
                        llamar("160");
                        break;
                    case 3:
                        llamar("114");
                        break;
                }
                drawerLayout.closeDrawers();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device=deviceArray[i];
                WifiP2pConfig config=new WifiP2pConfig();
                config.deviceAddress=device.deviceAddress;
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connected To: "+device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Not Connected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    public class CustomArrayAdapter<T> extends ArrayAdapter<T> {
        private Context mContext;
        private T[] list;
        public CustomArrayAdapter(Context context,int layoutresourceid,T[] listt){
            super(context,layoutresourceid,listt);
            mContext=context;
            list=listt;
        }
        public CustomArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            // Here all your customization on the View
            view.setBackgroundColor(Color.WHITE);


            return view;
        }


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(numerosEmergencias)) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.openDrawer(numerosEmergencias);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void initialWork(){
        btnOnOff=(Button)findViewById(R.id.onOff);
        btnDiscover=(Button)findViewById(R.id.discover);
        btnSend=(Button)findViewById(R.id.sendButton);
        listView=(ListView)findViewById(R.id.peerListView);
        read_msg_box=(TextView)findViewById(R.id.readMsg);
        connectionStatus=(TextView)findViewById(R.id.connectionStatus);
        writeMsg=(EditText) findViewById(R.id.writeMsg);
        wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager=(WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel=mManager.initialize(this,getMainLooper(),null);
        mReceiver=new WiFiDirectBroadcastReceiver(mManager,mChannel,this);
        mIntentFilter=new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        numerosEmergencias=(ListView)findViewById(R.id.list_view);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        //WIFI
        if(wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
            //btnOnOff.setText("ON");
        }else{
            wifiManager.setWifiEnabled(true);
            //btnOnOff.setText("OFF");
        }

    }
    WifiP2pManager.PeerListListener peerListListener= new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
           if(!peerList.getDeviceList().equals(peers)){
               peers.clear();
               peers.addAll(peerList.getDeviceList());
               deviceNameArray= new String[peerList.getDeviceList().size()];
               deviceArray= new WifiP2pDevice[peerList.getDeviceList().size()];
               int index=0;
               for(WifiP2pDevice device: peerList.getDeviceList()){
                   deviceNameArray[index]=device.deviceName;
                   deviceArray[index]=device;
               }
               ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
               listView.setAdapter(adapter);
           }
           if(peers.size()==0){
               Toast.makeText(getApplicationContext(), "No device Found", Toast.LENGTH_SHORT).show();
               return;
           }
        }
    };
    WifiP2pManager.ConnectionInfoListener connectionInfoListener=new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final Inet4Address groupOwnerAddress= (Inet4Address) wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                connectionStatus.setText("Host");
                server=new Server();
                server.start();
            }else if(wifiP2pInfo.groupFormed){
                connectionStatus.setText("Client");
                client=new ClientClass(groupOwnerAddress);
                client.start();
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    //On Create
    public void exqListener(View v){
        if(wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
            btnOnOff.setText("ON");
        }else{
            wifiManager.setWifiEnabled(true);
            btnOnOff.setText("OFF");
        }
    }
    public void discover(View v){
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                connectionStatus.setText("DiscoveryStarted");
            }

            @Override
            public void onFailure(int i) {
                connectionStatus.setText("Discovery Starting Failed");

            }
        });
    }
    public void send(View v){
        String msg=writeMsg.getText().toString();
        sendRecieve.write(msg.getBytes());
    }
    public class Server extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket=new ServerSocket(8888);
                socket=serverSocket.accept();
                sendRecieve=new SendRecieve(socket);
                sendRecieve.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;
        public ClientClass(InetAddress hostAddress){
            hostAdd=hostAddress.getHostAddress();
            socket=new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),1000);
                sendRecieve=new SendRecieve(socket);
                sendRecieve.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public class SendRecieve extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        public SendRecieve(Socket skt){
            socket=skt;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            byte[] buffer=new byte[2048];
            int bytes;

                try {
                    while(socket!=null){
                    bytes=inputStream.read(buffer);
                        if(bytes>0){
                            handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
