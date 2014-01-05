package com.example.esteban.bluetoothexample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {//,OnItemSelectedListener{

    private static final int REQUEST_ENABLE_BT = 55555;
    private static final int DISCOVERY_REQUEST = 11111;
    private static final String NAME = "NAME";
    private static final UUID MY_UUID = UUID.fromString("fa87c3d4-adac-11de-8a39-0800200c9a66");
    private static final int SOCKET_CONNECTED = 12345;
    private static final int MESSAGE_READ = 54321;
    BluetoothAdapter mBluetoothAdapter;
    String status;
    Button act_blu, des_blu, dis_blu, found_blu;
    ArrayList<DeviceData> foundDevice = new ArrayList<DeviceData>();
    ArrayAdapter<DeviceData> adapter1;
    ListView lista;
    int timeDiscoverable = 200;
    boolean search = false;
    TextView tv;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    BluetoothDevice bt ;
    boolean mServerMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FACER TOGGLE BUTTONS
        act_blu = (Button) findViewById(R.id.but_activar);
        des_blu = (Button) findViewById(R.id.but_desac);
        dis_blu = (Button) findViewById(R.id.but_dis);
        found_blu = (Button) findViewById(R.id.but_found);
        tv = (TextView) findViewById(R.id.textView3);

        mState = STATE_NONE;

        act_blu.setOnClickListener(this);
        des_blu.setOnClickListener(this);
        dis_blu.setOnClickListener(this);
        found_blu.setOnClickListener(this);

        lista = (ListView) findViewById(R.id.listView);
        adapter1 = new ArrayAdapter<DeviceData>(this,
                android.R.layout.simple_list_item_1, foundDevice);
        lista.setAdapter(adapter1);
        lista.setOnItemClickListener(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            status = "Device not support Bluetooth";
            Toast.makeText(this, status, Toast.LENGTH_LONG).show();
            finish();
        }

        if (mBluetoothAdapter.isEnabled()){
            dis_blu.setVisibility(View.VISIBLE);
            found_blu.setVisibility(View.VISIBLE);
        }


        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

    }



    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                //if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    if (foundDevice.indexOf(device) == -1)
                        foundDevice.add(new DeviceData(device.getName(), device.getAddress()));
                    adapter1.notifyDataSetChanged();
                //}
            }else if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
                        BluetoothAdapter.ERROR);
                String strMode = "";

                switch(mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        strMode = "mode changed: SCAN_MODE_CONNECTABLE_DISCOVERABLE";
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        strMode = "mode changed: SCAN_MODE_CONNECTABLE";
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        strMode = "mode changed: SCAN_MODE_NONE";
                        break;
                }
                Log.d("log123", strMode);
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                Log.d("log123","Discovery started");
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Discovery finished", Toast.LENGTH_SHORT).show();
                search = false;
                found_blu.setText("Buscar Dispositivos");
                Log.d("log123","Discovery finished");
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK ) {
                // if user pressed “YES”
                Log.d("log123","Result ok1");
                dis_blu.setVisibility(View.VISIBLE);
                found_blu.setVisibility(View.VISIBLE);
                Log.d("log123","Turn on Bluetooth");
            }
            if (resultCode == RESULT_CANCELED) {
                // if user pressed “NO”
                Log.d("log123","Result canceled1");
            }
        }else if (requestCode == DISCOVERY_REQUEST){
            if (resultCode == timeDiscoverable) {
                // if user pressed “YES”
                Log.d("log123","Visible");
                boolean isDiscoverable = resultCode > 0;
                if (isDiscoverable) {
                    mServerMode = true;
                    start();
                }
            }
            if (resultCode == RESULT_CANCELED) {
                // if user pressed “NO”
                Log.d("log123", "Result canceled2");
            }
        }
    }

    private void setState(int state) {
        Log.d("log123", "setState() " + mState + " -> " + state);
        mState = state;
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        Log.d("log123", "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d("log123", "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        //mHandler.obtainMessage(SOCKET_CONNECTED, mConnectedThread)
        //        .sendToTarget();
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d("log123", "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mHandler.obtainMessage(SOCKET_CONNECTED, mConnectedThread)
                .sendToTarget();
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
       /* Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/

        setState(STATE_CONNECTED);
    }






    //----------------------------------------------------------------------------------
    //MANAGECONNCECTED
    private void manageConnectedSocket(BluetoothSocket mSocket) {
        ConnectedThread conn = new ConnectedThread(mSocket);
        mHandler.obtainMessage(SOCKET_CONNECTED, conn)
                .sendToTarget();
        conn.start();
    }

    //----------------------------------------------------------------------------------


    //----------------------------------------------------------------------------------

    //SERVER SOCKET
    //Server must hold an open BluetoothServerSocket

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.d("log123","listen() failed");
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d("log123","Empezar mAcceptThread");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.d("log123", "Could not close unwanted socket");
                            }
                            break;
                    }
                }
            }
            Log.d("log123","mAcceptThread end");
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            Log.d("log123","Cancel server");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.d("log123","close() server failed");
            }
        }
    }

    //----------------------------------------------------------------------------------

    //CLIENT SOCKET
    //Must have “BluetoothDevice” of a remote device (discovered via startDiscovery())
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.d("log123", "create() failed");
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.d("log123","mConnectThread empieza");
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                conexionFallida();
                Log.d("log123","Conection failed");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.d("log123","cerrada conexion por un problema al conectar");
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            Log.d("log123","Cancel client");
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.d("log123","cancel() connect socket failed");
            }
        }
    }

    //----------------------------------------------------------------------------------

    //DATA TRANSFER
    //Each device now has a connected BluetoothSocket
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d("log123","ConectedThread empeza");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d("log123", "tmp sockets no creados");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.d("log123","empeza mConnectedThread");
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    Log.d("log123","try");
                    // Read from the InputStream
                    if (mmInStream.available() > 0){
                        Log.d("log123","available");
                        bytes = mmInStream.read(buffer);
                        Log.d("log123","leido");
                        // Send the obtained bytes to the UI activity
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
                    }
                } catch (IOException e) {
                    Log.d("log123","Lost connection");
                    conexionPerdida();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            Log.d("log123","write");
            try {
                mmOutStream.write(bytes);
                mmOutStream.flush();

            } catch (IOException e) {
                Log.e("log123","Exception durante a escritura",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.d("log123","close() connected socket failed");
            }
        }
    }

    //--------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------
    //HANDLER
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            ConnectedThread mBluetoothConnection = null;
            Log.d("log123","handler");
            switch (msg.what) {
                case SOCKET_CONNECTED: {
                    Log.d("log123","SOCKET CONNECTED");
                    mBluetoothConnection = (ConnectedThread) msg.obj;
                    if (!mServerMode){
                        Log.d("log123","write this is a message");
                        mBluetoothConnection.write("this is a message".getBytes());
                    }
                    break;
                }
                case MESSAGE_READ: {
                    Log.d("log123","MESSAGE READ");
                    String data = (String) msg.obj;
                    tv.setText(data);
                    if (mServerMode)
                    mBluetoothConnection.write(data.getBytes());
                }
            }
        }
    };

    private void conexionPerdida (){
        setState(STATE_LISTEN);
        //Toast.makeText(this,"Conexion perdida",Toast.LENGTH_SHORT).show();
        Log.d("log123","Conexion perdida");
    }

    private void conexionFallida (){
        setState(STATE_LISTEN);
        //Toast.makeText(this,"Conexion fallida",Toast.LENGTH_SHORT).show();
        Log.d("log123","Conexion fallida");
    }

    public void stop (){
        Log.d("log123","Stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(STATE_NONE);
    }

    //----------------------------------------------------------------------------------



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View v) {
        if (v == act_blu){
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }else{
                String mydeviceaddress = mBluetoothAdapter.getAddress();
                String mydevicename = mBluetoothAdapter.getName();
                status = mydevicename + " : " + mydeviceaddress;
                Toast.makeText(this, status, Toast.LENGTH_LONG).show();
            }
        }else if (v == des_blu){
            mBluetoothAdapter.disable();
            //deviceDataList.clear();
            foundDevice.clear();
            //adapter.notifyDataSetChanged();
            adapter1.notifyDataSetChanged();
            dis_blu.setVisibility(View.GONE);
            found_blu.setVisibility(View.GONE);
            Toast.makeText(this, "Turn off Bluetooth", Toast.LENGTH_SHORT).show();
            Log.d("log123","Turn off Bluetooth");
        }else if (v == dis_blu){
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, timeDiscoverable);
            startActivityForResult(discoverableIntent, DISCOVERY_REQUEST);
        }else if (v == found_blu){
            if (!search){
                if (!mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.startDiscovery();
                }
                foundDevice.clear();
                adapter1.notifyDataSetChanged();
                search = true;
                found_blu.setText("Cancelar Buscar Dispositivos");
            }else {
                mBluetoothAdapter.cancelDiscovery();
                search = false;
                found_blu.setText("Buscar Dispositivos");
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.listView){
            DeviceData deviceRemote;
            deviceRemote = foundDevice.get(position);
            bt = mBluetoothAdapter.getRemoteDevice(deviceRemote.getValue());
            try {
                if (bt.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d("log123","BOND NONE "+parent.getItemAtPosition(position).toString());
                    createBond(bt);
                    adapter1.notifyDataSetChanged();
                    //start();
                }else if (bt.getBondState() == BluetoothDevice.BOND_BONDED){
                    //mConnectThread = new ConnectThread(bt);
                    connect(bt);
                    Log.d("log123","BOND BONDED "+parent.getItemAtPosition(position).toString());
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean createBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }


    //CLASS DEVICEDATA
    class DeviceData {
        public DeviceData(String spinnerText, String value) {
            this.spinnerText = spinnerText;
            this.value = value;
        }

        public String getValue()
        {

            return value;
        }

        public String toString() {
            return spinnerText;
        }

        String spinnerText;
        String value;
    }

}
