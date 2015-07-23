package bit.hillcg2.bluetoothtesting;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.widget.Button;
import android.view.View.OnClickListener;


public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    public static final UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private ArrayList<BluetoothDevice> deviceList;
    private BluetoothAdapter BTAdapter;
    private ConnectedThread connectionThread;

    private Button btnTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    public void init(){
        btnTest = (Button)findViewById(R.id.button);
        btnTest.setOnClickListener(new buttonHanlder());

        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        if (BTAdapter == null)
        {
            Toast.makeText(this, "Device does not support bluetooth", Toast.LENGTH_LONG).show();
            finish();
        }

        if (!BTAdapter.isEnabled())
        {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
        }

        deviceList = new ArrayList<BluetoothDevice>();

        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device);
            }
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);// Don't forget to unregister during onDestroy

        BTAdapter.startDiscovery();
    }

    public class buttonHanlder implements OnClickListener{

        @Override
        public void onClick(View v) {
            connectionThread.write("Testing");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
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
                String deviceName = device.getName();
                if(deviceName.equals("Ardu"))
                {
                    ConnectThread connectThread = new ConnectThread(device);
                    connectThread.run();
                }

                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes; // bytes returned from read()

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);

                    //send bytes to UI
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            try {
                byte[] bytes = message.getBytes();
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try
            {
                mmSocket.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try
            {
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e)
            {
            }
            mmSocket = tmp;
        }

        public void run() {
            BTAdapter.cancelDiscovery();

            try
            {
                mmSocket.connect();
            }
            catch (IOException connectException)
            {
                // Unable to connect; close the socket and get out
                try
                {
                    mmSocket.close();
                }
                catch (IOException closeException)
                {
                }
                return;
            }
            connectionThread = new ConnectedThread(mmSocket);

            //Freezes program
            // connectionThread.run();

            //cancel();
        }

        public void cancel() {
            try
            {
                mmSocket.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "App needs bluetooth", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
