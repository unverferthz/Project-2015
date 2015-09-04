package bit.hillcg2.bluetoothtesting;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class BTManager {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ArrayList<BluetoothDevice> deviceList;
    private BluetoothAdapter BTAdapter;
    private ConnectedThread connectionThread;
    private ConnectThread connectThread;

    private Activity mainActivity;

    public BTManager(Activity activity){

        mainActivity = activity;
        init();
    }

    private void init(){
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = new ArrayList<BluetoothDevice>();
        connectionThread = null;
        connectThread = null;
    }

    //Disconnect from device if already connected. Reset everything and scan for devices
    public void reset(){
        if(connectionThread != null)
            connectionThread.cancel();

        if(connectThread != null)
            connectThread.cancel();

        BTAdapter.cancelDiscovery();
        BTAdapter = null;
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        init();
        startScanning();
    }

    //Check if bluetooth is enabled, if not then request that it be turned on
    public boolean CheckBTEnabled(){

        if (BTAdapter == null) {
            Toast.makeText(mainActivity , "Device does not support bluetooth", Toast.LENGTH_LONG).show();
            mainActivity.finish();
        }

        if (!BTAdapter.isEnabled())
        {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mainActivity.startActivityForResult(enableBT, REQUEST_ENABLE_BT);
            return false;
        }

        return true;
    }

    //Connect to the bluetooth device passed in
    public void ConnectToDevice(BluetoothDevice device)
    {
        connectThread = new ConnectThread(device);
        connectThread.run();
    }

    //Start scanning for bluetooth devices
    public void startScanning(){
        BTAdapter.startDiscovery();
    }

    //Send message passed in over bluetooth
    public void sendMessage(String message){
        connectionThread.write(message);
    }

    //Adds device into device list
    public void addFoundDevice(BluetoothDevice device){
        deviceList.add(device);
    }

    //Returns a list of all the bluetooth devices in the area
    public ArrayList<BluetoothDevice> getDeviceList(){
        return deviceList;
    }

    //Class for handling connection to bluetooth device
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private Thread thread;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            thread = null;

            try
            {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            }
            catch (IOException e)
            {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //Method for reading from bluetooth device
        public void run() {
            thread = new Thread() {
                @Override
                public void run() {
                    try
                    {
                        while(true)
                        {
                            try
                            {
                                final byte[] buffer = new byte[1024]; // buffer store for the stream

                                //Blocking call
                                int bytes = mmInStream.read(buffer); // bytes returned from read()

                                //byte[] test = buffer;

                                final String message = new String(buffer, 0, bytes);

                                //Give the received message to the main activity to be displayed
                                MainActivity temp = (MainActivity)mainActivity;
                                temp.displayMessage(message);
                            }
                            catch (IOException e)
                            {
                                break;
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();
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
                thread.interrupt();
                thread = null;

                mmInStream.close();
                mmOutStream.close();

                mmSocket.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    //Class for connecting to bluetooth device
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
            catch(IOException e)
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
                catch(IOException closeException)
                {
                }
                return;
            }

            //Bluetooth connected
            connectionThread = new ConnectedThread(mmSocket);

            //connectionThread.write("Connected");

            //Run method that listens for bluetooth messages
            connectionThread.run();

            //cancel();
        }

        /**
         Will cancel an in-progress connection, and close the socket
         **/
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
